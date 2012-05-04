(ns com.bensmann.fastms.fastsql
  (:use [clojure.contrib.sql :as sql]))

(defn
  get
  [^String table ^String where ^List fields]
  (sql/with-connection db
       (sql/with-query-results res
         ["SELECT * FROM user"]
         (doseq [rec res]
           (println rec)))))
