;; 
;; /Users/rbe/project/clas/src/com/bensmann/clas/appmodule.clj
;; 
;; Copyright (C) 2009-2010 Informationssysteme Ralf Bensmann.
;; Alle Rechte vorbehalten. Nutzungslizenz siehe http://www.bensmann.com/license_de.html
;; All Rights Reserved. Use is subject to license terms, see http://www.bensmann.com/license_en.html
;; 
;; Created by: rbe
;; 
(ns com.bensmann.appsrv.appmodule
  (:use (com.bensmann.clas config core log stats url))
  (:require [clojure.contrib [str-utils :as str-utils]]))

(defn compile-appmodule
  {
    :doc "Compile an appmodule namespace."
    :user/comment "A comment."
  }
  [req more]
  (let [n (str-utils/str-join "." more)
        sym (symbol n)]
    (locking n ; lock: do not compile at same time
      (with-pre-log-debug req (format "require'ing %s" sym)
        (require :reload sym)) ':verbose
        (let [found-ns (find-ns sym)]
          (or found-ns
              (try*
               (with-pre-log-debug req (format "compiling %s" sym)
                 (binding [*compile-path* "src/appmodule/classes"]
                   (let [compiled (compile sym)
                         found-ns (find-ns sym)]
                     found-ns)))
               (with-post-log-error req (format "could not find %s" n) nil)))))))

(defn find-appmodule
  {
    :doc "Try to load an appmodule."
    :user/comment "A comment."
  }
  [req & more]
  (let [n (str-utils/str-join "." more)
        sym (symbol n)]
    (case (cv-enabled? :development-mode)
          true (do
                 (with-pre-log-debug req (format "RECOMPILE %s" more))
                 (compile-appmodule req more))
          false (if-let [found-ns (find-ns sym)]
                  found-ns
                  (compile-appmodule req more)))))

(defn
  ^{
    :doc "List all appmodule namespaces which can/should be reloaded."
    :added ""
    :user/comment "A comment."
   }
  reloadable-appmodules
  []
  (let [swf #(.startsWith % "com.bensmann")
        ewf #(not (or (.endsWith % "config") (.endsWith % "log")))
        f #(and (swf %) (ewf %))]
    (for [n (filter f (map str (all-ns)))]
      (symbol n))))
