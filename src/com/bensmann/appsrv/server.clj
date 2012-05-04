;; 
;; /Users/rbe/project/clas/src/com/bensmann/clas/server.clj
;; 
;; Copyright (C) 2009-2010 Informationssysteme Ralf Bensmann.
;; Alle Rechte vorbehalten. Nutzungslizenz siehe http://www.bensmann.com/license_de.html
;; All Rights Reserved. Use is subject to license terms, see http://www.bensmann.com/license_en.html
;; 
;; Created by: rbe
;; 
(ns com.bensmann.appsrv.server
  (:use
        (ring.adapter jetty)
        (ring.middleware keyword-params params reload stacktrace)
        (ring.handler dump)
        (com.bensmann.clas config core log stats)
        (com.bensmann.appsrv appmodule))
  (:require [clojure.contrib [str-utils :as str-utils]]
            [com.bensmann.clas [request :as request]
                               [response :as response]])
  (:gen-class))

(defn
  ^{
    :doc "Calls function for request method (doXxx) in controller and returns result."
    :user/comment "A comment."
   }
  perform-action
  [req action-ns]
  (let [n (symbol (request/request-method->fun (:request-method req)))]
    (if-let [f (ns-resolve action-ns n)]
      (f req))))

(defn
  ^{
    :doc "Process a MVC request: /module/version/controller/action[/more/path]?params
  Load namespace <module>.<version>.controllers.<controller>.<action> and
  call function depending on request method: doGet, doPost etc."
    :user/comment "A comment."
   }
  dispatch-mvc
  [req]
  (if-let [parsed-request (request/parse-mvc-request (:uri req))]
    (let [[module version controller action additional-path] parsed-request
         req (assoc req :additional-path additional-path)
         response (or
                   (if-let [action-ns (find-appmodule req module version "controllers" controller action)]
                     (perform-action req action-ns))
                   (response/by-name 'not-found {}))]
      (with-post-log-debug req (str parsed-request " --> " response) response))
    (response/by-name 'not-found {})))

(defn app-router [req]
  (let [req (assoc req :req-id (request/next-req-id))
        response (with-stats req (dispatch-mvc req))]
    (with-log-stats (time-stats->string))
    response))

(def app
  (-> #'app-router
    ;; (wrap-reload (reloadable-appmodules))
    (wrap-keyword-params)
    (wrap-params)
    (wrap-stacktrace)))

;; Map for app servers: port -> Ring
(defonce *app-server* (ref {}))

(defn
  ^{
    :doc "Start a new instance of app server."
    :added ""
    :user/comment "A comment."
   }
  startup
  [port]
  (when port
    (if-let [p (get @*app-server* port)]
      (do
        (println "Application server on port" port "seems to be started already.")
        p)
      (try*
        (if-let [p (run-jetty #'app {:port port :join? false})]
          (do
            (println "Starting application server on port" port)
            (dosync
              (commute *app-server* assoc port p)
              (.start p)))
        (do
          (println "Could not start application server on port" port)))))))

(defn
  ^{
    :doc "Shutdown a running instance  of app server."
    :added ""
    :user/comment "A comment."
   }
  shutdown
  [port]
  (when port
    (if-let [p (get @*app-server* port)]
      (do
        (println "Stopping application server on port" port)
        (.stop p))
      (do
        (println "Could not start application server on port" port)))))

(defn
  ^{
    :doc "Main."
    :added ""
    :user/comment "A comment."
   }
  -main
  [& args]
  (load-cv)
  (startup (get-cv :http-port)))

;;    (with-log-info (str "classpath: " (doall (map #(System/getProperty %) ["java.class.path" "java.library.path"]))))
