;; 
;; /Users/rbe/project/clas/src/com/bensmann/clas/core.clj
;; 
;; Copyright (C) 2009-2010 Informationssysteme Ralf Bensmann.
;; Alle Rechte vorbehalten. Nutzungslizenz siehe http://www.bensmann.com/license_de.html
;; All Rights Reserved. Use is subject to license terms, see http://www.bensmann.com/license_en.html
;; 
;; Created by: rbe
;; 
(ns com.bensmann.clas.core)

;; Return f when f is true/non-nil etc. otherwise return m
;; (defmacro elvis
;;     [f & m]
;;     `(if-let [r# ~f]
;;         r#
;;         ~@m))

(defmacro
  ^{
    :doc "More than Groovy-like elvis operator: (or) in Clojure."
    :user/comment "A comment."
   }
  elvis
  [& m]
  `(or ~@m))

(defmacro aif [expr & body]
  `(let [~'it ~expr] (if ~'it (do ~@body))))

(defmacro try*
    ([form]
        `(try ~form (catch Exception e# (do (.printStackTrace e#) nil))))
    ([form catch-form]
        `(try ~form (catch Exception e# (do (.printStackTrace e#) ~catch-form nil))))
    ([form catch-form finally-form]
        `(try ~form (catch Exception e# ~catch-form) (finally ~finally-form))))

(defmacro defn-from-str [n args & body]
    `(defn ~(symbol (eval n)) ~args ~@body))

(defn
  ^{
    :doc "Documentation for all-not-nil."
    :added ""
    :user/comment "A comment."
   }
  all-not-nil?
  [s]
  (every? #(not= nil %) s))

(defn
  ^{
    :doc "Documentation for dump-threads."
    :added ""
    :user/comment "A comment."
   }
  dump-threads
  []
  (for [x (.dumpAllThreads (java.lang.management.ManagementFactory/getThreadMXBean) false false)]
    (println x)))
