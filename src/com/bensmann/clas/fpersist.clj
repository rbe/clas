;; 
;; /Users/rbe/project/clas/src/com/bensmann/clas/fpersist.clj
;; 
;; Copyright (C) 2009-2010 Informationssysteme Ralf Bensmann.
;; Alle Rechte vorbehalten. Nutzungslizenz siehe http://www.bensmann.com/license_de.html
;; All Rights Reserved. Use is subject to license terms, see http://www.bensmann.com/license_en.html
;; 
;; Created by: rbe
;; 
(ns com.bensmann.clas.fpersist
  (:use (com.bensmann.clas core)))

(defn make-file
  "If file is an instance of java.io.File return file,
  else if it's an java.lang.String make new File object with file being the name,
  otherwise generate a temporary name."
  [file]
  (cond
    (instance? java.io.File file) file
    (instance? java.lang.String file) (java.io.File. file)
    :else (java.io.File/createTempFile "glue", ".clj")))

(defn save-map
  "Save form to a file using first parameter as filename returns java.io.File object."
  [file m]
  (let [fh (make-file file)]
    (spit fh (pr-str m))
    fh))

(defn load-map
  [file]
  (if-let [fh (make-file file)]
    (read-string (slurp fh))))
