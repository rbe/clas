(ns com.bensmann.clas.json
  (:use (com.bensmann.clas log))
  (require [org.danlarkin [json :as json]]))

(defn
  ^{
    :doc "Documentation for parse-json."
    :user/comment "A comment."
   }
  parse-from-stream
  [req body]
  (let [b (slurp body)]
    (with-post-log-debug req (str "parse-from-stream: " b) (json/decode-from-str b))))

(defn
  ^{
    :doc "Documentation for make-json."
    :added ""
    :user/comment "A comment."
   }
  make-json
  [req data]
  (let [json (json/encode-to-str data)]
    (with-post-log-debug req (str "make-json: json=" json) json)))
