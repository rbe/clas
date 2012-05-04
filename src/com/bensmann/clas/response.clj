;; 
;; /Users/rbe/project/clas/src/com/bensmann/clas/response.clj
;; 
;; Copyright (C) 1996-2010 Informationssysteme Ralf Bensmann.
;; Alle Rechte vorbehalten. Nutzungslizenz siehe http://www.bensmann.com/license_de.html
;; All Rights Reserved. Use is subject to license terms, see http://www.bensmann.com/license_en.html
;; 
;; Created by: rbe
;; 
(ns com.bensmann.clas.response)

;; http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
(def codes {:ok           {:status 200}
            :created      {:status 201}
            :no-content   {:status 204}
	    :found        {:status 302}
            :not-modified {:status 304}
            :bad-request  {:status 400 :body "Don't repeat this request without modification."}
            :unauthorized {:status 401 :body "Unauthorized access prohibited."}
            :forbidden    {:status 403 :body "Access to this resource forbidden."}
            :not-found    {:status 404 :body "Resource not found."}
            :conflict     {:status 409 :body "Resource conflict."}
            :gone         {:status 410 :body "The resource does not exist anymore."}})

(defn
  ^{
    :doc "Generate a HTTP response by name for use with Ring."
    :added ""
    :user/comment "A comment."
   }
  by-name
  [n {:keys [content-type content-encoding body]}]
  (if-let [m ((keyword n) codes)]
    (let [status (:status m)
          content-type (or content-type (:content-type m) "text/html")
          body (or body (:body m) "")]
      {:status status :headers {"Content-Type" content-type "Content-Encoding" content-encoding} :body body})
    {:status 500 :headers {"Content-Type" "text/html"} :body "The server made a boo-boo."}))

(defn
  ^{
    :doc "Generate a HTTP response by code for use with Ring."
    :added ""
    :user/comment "A comment."
   }
  by-code
  [c {:keys [headers body session]}]
  (let [b (or body nil)
        h (merge {} headers)
        s (merge {} session)]
    {:status c :headers h :body b :session s}))
