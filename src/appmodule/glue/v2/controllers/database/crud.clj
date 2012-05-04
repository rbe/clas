(ns glue.v2.controllers.database.crud
    (require [com.bensmann.clas [request :as request]
                                [json :as json]
                                [response :as response]]))

(defn
  ^{
    :doc "Get a document."
    :added ""
    :user/comment "A comment."
   }
  doGet
  [req]
  (response/by-name 'ok {:body "I proudly present: your resource!"}))

(defn
  ^{
    :doc "Create or modify a document."
    :added ""
    :user/comment "A comment."
   }
  doPut
  [req]
  (let [[user db & path] (:additional-path req)
        json (json/parse-from-stream req (:body req))
        answer (str "hey-ho, action=" (:action json) " with user=" user " db=" db " path=" path)]
      (response/by-name 'created {:body answer})))

(defn
  ^{
    :doc "Delete content in document or the document itself."
    :added ""
    :user/comment "A comment."
   }
  doDelete
  [req]
  (response/by-name 'ok {:body "Resource deleted."}))
