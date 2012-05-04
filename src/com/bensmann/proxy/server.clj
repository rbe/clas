;; 
;; /Users/rbe/project/clas/src/com/bensmann/proxy/server.clj
;; 
;; Copyright (C) 2009-2010 Informationssysteme Ralf Bensmann.
;; Alle Rechte vorbehalten. Nutzungslizenz siehe http://www.bensmann.com/license_de.html
;; All Rights Reserved. Use is subject to license terms, see http://www.bensmann.com/license_en.html
;; 
;; Created by: rbe
;; 
(ns com.bensmann.proxy.server
  (:use (ring.adapter jetty)
        (ring.middleware cookies keyword-params params reload session stacktrace)
        (ring.handler dump)
        (ring.util response)
        (com.bensmann.clas config core log stats url)
        (com.bensmann.proxy proxymodule))
  (:require [clojure.contrib [str-utils :as str-utils]]
            [clojure.contrib.http [agent :as http]]
            [com.bensmann.clas [request :as request]
                               [response :as response]])
  (:gen-class))

;; Standard timeout for requests
(def *timeout* 20000)

(defn
  ^{
    :doc "Match request URI against mappings."
    :added ""
    :user/comment "A comment."
   }
  get-uri-config
  [uri mappings]
  (loop [n mappings]
    (when-not (empty? n)
      (let [m (ffirst n)
            r (re-find (re-pattern (key m)) uri)]
        (if (not (nil? r))
          (val m)
          (recur (rest n)))))))

(defn
  ^{
    :doc "Get configuration for server in request."
    :added ""
    :user/comment "A comment."
   }
  get-server-config
  [req]
  (get (get-cv :proxy-map) (:server-name req)))

(defn
  ^{
    :doc "Modify the original request depending on its destination with values from configuration."
    :added ""
    :user/comment "A comment."
   }
  modify-request
  [req]
  (let [req (assoc req
                   :orig-server-name (:server-name req)
                   :req-id (request/next-req-id)
                   :req-method-name (.toUpperCase (name (:request-method req))))
        req (if (not (get-in req [:session :counter]))
              (assoc-in req [:session :counter] (ref 0))
              req)
        server-config (get-server-config req)]
    (if (not (nil? server-config))
      (let [req-config (get-uri-config (:uri req) server-config)
            req2 (:request req-config)]
        (merge req req2))
      req)))

(defn
  ^{
    :doc "Convert a header map with keywords as keys to keys as strings"
    :added ""
    :user/comment "A comment."
   }
  convert-headers
  [m]
  (loop [z (dissoc m nil)  ; remove nil header, contains HTTP status code
         r {}]
    (let [e (first z)]
      (if (nil? e)
        r
        (recur (rest z)
	       (assoc r (name (key e)) (val e)))))))

(defn
  ^{
    :doc "Make URL for destination from request."
    :added ""
    :user/comment "A comment."
   }
  make-dest-url
  [req]
  (let [scheme (name (:scheme req))
        server (:server-name req)
        port (:server-port req)
        uri (:uri req)
        param-string (if-let [p (:query-string req)]
                       (format "?%s" p)
                       "")]
    (format "%s://%s:%s%s%s" scheme server port uri param-string)))

(defn
  ^{
    :doc "Make body for request to destination server."
    :added ""
    :user/comment "A comment."
   }
  make-request-body
  [req]
  (let [req-method-name (:req-method-name req)
        post-req? (= req-method-name "POST")
        slurped-req-body (slurp (:body req))]
    (cond
      post-req? (encode-url-params (:form-params req))
      (= 0 (.length slurped-req-body)) nil
      :else slurped-req-body)))

(defn
  ^{
    :doc "Make headers for request to destination server."
    :added ""
    :user/comment "A comment."
   }
  make-request-headers
  [req]
  (let [rh (:headers req)]
        ;; req-headers (assoc-in rh [:headers "host"] (:server-name req))]
    ;; (println "rh" (get-in rh [:headers "host"]))
    ;; (println "req-headers" (get-in req-headers [:headers "host"]))
    rh))

(defn
  ^{
    :doc "Pre-process request through handler."
    :added ""
    :user/comment "A comment."
   }
  pre-process-request
  [req]
  (let [server-ns (apply str (replace {\. \-} (:orig-server-name req)))]
    (if-let [handler-ns (find-proxymodule req server-ns "handler")]
      (let [f (ns-resolve handler-ns 'pre-process-request)]
        (f req))
      req)))

(defn
  ^{
    :doc "Fetch data from an URL and return map with keys: status, headers and body."
    :added ""
    :user/comment "A comment."
   }
  fetch-url
  [req]
  (let [rid (:req-id req)
        url (make-dest-url req)
        req-body (make-request-body req)
        req-headers (make-request-headers req)
        req-method (:req-method-name req)]
    (let [dest-agt (http/http-agent url
                                    :connect-timeout *timeout*
                                    :read-timeout *timeout*
                                    :follow-redirects false
                                    :method req-method
                                    :headers req-headers
                                    :body req-body)
          agt-res (await-for *timeout* dest-agt)]
      (try*
        (do
          (if (nil? agt-res)
            {:status 500 :headers {"Content-Type" "text/html"} :body "agt-res is nil."}
            (let [status (http/status dest-agt)
                  resp-headers (convert-headers (http/headers dest-agt))
                  resp-body (http/stream dest-agt)]
              {:status status :headers resp-headers :body resp-body})))
        {:status 500 :headers {"Content-Type" "text/html"} :body "Could not fetch page."}))))

(defn
  ^{
    :doc "Post-process response through handler."
    :added ""
    :user/comment "A comment."
   }
  post-process-request
  [req response]
  (let [server-ns (apply str (replace {\. \-} (:orig-server-name req)))]
    (if-let [handler-ns (find-proxymodule req server-ns "handler")]
      (let [f (ns-resolve handler-ns 'post-process-request)]
        (f req response))
      [req response])))

(defn app-router [req]
  (let [req (pre-process-request (modify-request req))] ; modify request and pre-process it
    (if-let [dest (:proxy-redirect req)]
      ;; (redirect dest)
      (response/by-code 302
                        {:headers (merge {"Location" dest} (:headers req))
                         :body (:body req)
                         :session (:session req)})
      (let [[req resp] (post-process-request req (fetch-url req))] ; post-process response
        (if (empty? resp)
          (response/by-code 500 {:headers {"Content-Type" "text/html"} :body "<html><body><h1>Status 500</h1><p>Oops.</p></body></html>" :session (:session req)})
          (response/by-code (:status resp)
                            {:headers (merge {} (:headers resp))
                             :body (:body resp)
                             :session (:session req)}))))))

(def app
  (-> #'app-router
    ;; (wrap-reload (reloadable-proxymodules))
    (wrap-session)
    (wrap-cookies)
    (wrap-keyword-params)
    (wrap-params)
    (wrap-stacktrace)))

;; Map for proxy servers: port -> Ring
(defonce *proxy-server* (ref {}))

(defn
  ^{
    :doc "Start a new instance of proxy server."
    :added ""
    :user/comment "A comment."
   }
  startup
  [port]
  (when port
    (System/setProperty "http.nonProxyHosts" "*") ; Don't use myself
    (if-let [p (get @*proxy-server* port)]
      (do
        (println "Proxy server on port" port "seems to be started already.")
        p)
      (try*
        (if-let [p (run-jetty #'app {:port port :join? false})]
          (do
            (println "Starting proxy server on port" port)
            (dosync
              (commute *proxy-server* assoc port p)
              (.start p)))
        (do
          (println "Could not start proxy server on port" port)))))))

(defn
  ^{
    :doc "Shutdown a running instance of proxy server."
    :added ""
    :user/comment "A comment."
   }
  shutdown
  [port]
  (when port
    (if-let [p (get @*proxy-server* port)]
      (do
        (println "Stopping proxy server on port" port)
        (.stop p)
        (dosync
          (commute *proxy-server* dissoc port)))
      (do
        (println "Could not stop proxy server on port; there's none." port)))))

(defn
  ^{
    :doc "Main."
    :added ""
    :user/comment "A comment."
   }
  -main
  [& args]
  (load-cv)
  (startup (get-cv :proxy-port)))
