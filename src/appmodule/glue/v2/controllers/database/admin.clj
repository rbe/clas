(ns glue.v2.controllers.database.admin
    (require [com.bensmann.clas [request :as request]
                                [json :as json]
                                [response :as response]]))

(defn
  ^{
    :doc "Read administrative settings."
    :added ""
    :user/comment "A comment."
   }
  doGet
  [req]
  (response/by-name 'ok {:body (str "your resource" (request/params->str req))}))

(defn
  ^{
    :doc "Administrate."
    :added ""
    :user/comment "A comment."
   }
  doPost
  [req]
  (response/by-name 'ok {:body "your resource"}))
