;; 
;; /Users/rbe/project/clas/src/com/bensmann/clas/request.clj
;; 
;; Copyright (C) 2009-2010 Informationssysteme Ralf Bensmann.
;; Alle Rechte vorbehalten. Nutzungslizenz siehe http://www.bensmann.com/license_de.html
;; All Rights Reserved. Use is subject to license terms, see http://www.bensmann.com/license_en.html
;; 
;; Created by: rbe
;; 
(ns com.bensmann.clas.request
  (require [clojure.contrib [str-utils :as str-utils]]))

;; Synchronous reference
(def *req-id* (ref 0x0))
(defn
  ^{
    :doc "Get next request id."
    :added ""
    :user/comment "A comment."
   }
  next-req-id
  []
  (dosync
    (commute *req-id* inc)))

(defn
  ^{
    :doc "Documentation for parse-mvc-request."
    :user/comment "A comment."
   }
  parse-mvc-request
  [uri]
  (let [mvc (rest (str-utils/re-split #"/" uri))
        module (nth mvc 0 nil)
        version (nth mvc 1 nil)
        controller (nth mvc 2 nil)
        action (nth mvc 3 nil)
        additional-path (nthnext mvc 4)]
    (if (and
         (not= nil controller)
         (not= nil action))
      (do
        (println "parse-mvc-request: module=" module)
        (println "parse-mvc-request: version=" version)
        (println "parse-mvc-request: controller=" controller)
        (println "parse-mvc-request: action=" action)
        (println "parse-mvc-request: additional-path=" additional-path)
        [module version controller action additional-path]))))

(defn
  ^{
    :doc "Documentation for params->str."
    :added ""
    :user/comment "A comment."
   }
  params->str
  [req]
  (reduce #(str %1 "<br/>" %2) (map #(str (first %) " ---> " (second %)) req)))

(defn
  ^{
    :doc "Get name of function corresponding to request method."
    :added ""
    :user/comment "A comment."
   }
  request-method->fun
  [k]
  (let [s (str-utils/str-join "" (rest (str k)))
        fc (Character/toUpperCase (first s))
        rc (rest s)]
    (apply str (conj rc fc "do"))))

(defn  ; TODO url/encode-url-params!?
  ^{
    :doc "Convert map with form parameters to a string."
    :added ""
    :user/comment "A comment."
   }
  form-params->string
  [req]
  (let [x (:form-params req)]
    (str-utils/str-join "&" (map #(format "%s=%s" (key %) (val %)) x))))
