;; 
;; /Users/rbe/project/clas/src/com/bensmann/proxy/proxymodule.clj
;; 
;; Copyright (C) 2009-2010 Informationssysteme Ralf Bensmann.
;; Alle Rechte vorbehalten. Nutzungslizenz siehe http://www.bensmann.com/license_de.html
;; All Rights Reserved. Use is subject to license terms, see http://www.bensmann.com/license_en.html
;; 
;; Created by: rbe
;; 
(ns com.bensmann.proxy.proxymodule
  (:use (com.bensmann.clas config core log stats url))
  (:require [clojure.contrib [str-utils :as str-utils]]))

(defn find-proxymodule
  {
    :doc "Try to load an proxymodule namespace."
    :user/comment "A comment."
  }
  [req & more]
  ;; try to find already-compiled namespace
  (let [n (str-utils/str-join "." more)
        sym (symbol n)]
    (or
      (find-ns sym)
      ;; otherwise compile it
      (locking n ; lock: do not compile at same time
        (with-pre-log-debug req (format "got lock on monitor-object %s" n)
          (let [_ (with-cv-enabled :development-mode ; in devmode (require :reload), for reload-all go to REPL
                    (try
                      (with-pre-log-debug req (format "require'ing %s" sym)
                        (require :reload :verbose sym))
                      (catch Exception e nil)))
                found-ns (find-ns sym)]
            (or found-ns
              (try
                (with-pre-log-debug req (format "compiling %s" sym)
                  (binding [*compile-path* "src/proxymodule/classes"]
                    (let [compiled (compile sym)
                          found-ns (find-ns sym)]
                      found-ns)))
                (catch java.io.FileNotFoundException e nil)
                (catch Exception e
                  (with-post-log-error req (format "could not load %s: %s" n e) nil))))))))))
