(ns flattr.v1.controllers.my.thing
    (:require [com.bensmann.clas [request :as request]
                                 [json :as json]
                                 [response :as response]
                                 [url :as url]]
              [http.async [client :as c]]))

(defn login
  []
  (let [resp (c/GET "https://flattr.com/")
        _ (c/await resp)
        cookies (c/cookies resp)]
    (let [resp (c/POST "https://flattr.com/login"
                       :body {:account "rbe" :password "xxx" :remember 1})
          _ (c/await resp)]
      (if (and (c/done? resp)
               (= "/dashboard" (:location (c/headers resp))))
        {:logged-in true  :cookies cookies :status (c/status resp) :headers (c/headers resp)}
        {:logged-in false :cookies cookies :status (c/status resp) :headers (c/headers resp)}))))

(defn submit-thing
  [flattr-req req]
  (if (:logged-in flattr-req)
    (let [enc #(java.net.URLEncoder/encode (str %) "UTF-8")
          url (enc "http://blog.bensmann.com/athing")
          title (enc "A title")
          category (enc "text")
          description (enc "A desc")
          tags (enc "java")
          lang (enc "en_GB")
          cookies (:cookies flattr-req)
          resp (c/POST "https://flattr.com/submit"
                       :cookies cookies
                       :body {:uid "41071" :url url :title title :category category :descr description :tags tags :lang lang})
          _ (c/await resp)]
      (if (c/done? resp)
        {:cookies cookies :status (c/status resp) :headers (c/headers resp)}
        false))
    flattr-req))

(defn
  ^{
    :doc "Flattr a thing."
    :added ""
    :user/comment "A comment."
   }
  doGet
  [req]
  (when-let [cookies (login)]
    (submit-thing cookies req)))
