;;
;; /Users/rbe/project/clas/src/com/bensmann/clas/config.clj
;; 
;; Copyright (C) 2009-2010 Informationssysteme Ralf Bensmann.
;; Alle Rechte vorbehalten. Nutzungslizenz siehe http://www.bensmann.com/license_de.html
;; All Rights Reserved. Use is subject to license terms, see http://www.bensmann.com/license_en.html
;; 
;; Created by: rbe
;; 
(ns com.bensmann.clas.config
  (:use (com.bensmann.clas config log))
  (:require [com.bensmann.clas [fpersist :as f]]))

(def *config-vars* (agent {:development-mode {:enabled false :value nil}}))

(defn
  ^{
     :doc "Get value for config-var."
     :added ""
     :user/comment "A comment."
     }
  get-cv
  [k]
  (get-in @*config-vars* [k :value ]))

(defn
  ^{
     :doc "Set value for config-var."
     :added ""
     :user/comment "A comment."
     }
  set-cv
  [k v]
  (let [cv (get-in @*config-vars* [k])]
    (with-log-info (format "queueing change for config-var %s from %s to %s" k (:value cv) v))
    (if (nil? cv)
      (dosync
        (send *config-vars* assoc k {:value v :enabled false}))
      (dosync
        (send *config-vars* assoc-in [k :value ] v)))))

(defn
  ^{
     :doc "Enable a config-var."
     :added ""
     :user/comment "A comment."
     }
  enable-cv
  [k]
  (dosync
    (send *config-vars* assoc-in [k :enabled ] true)))

(defn
  ^{
     :doc "Disable a config-var."
     :added ""
     :user/comment "A comment."
     }
  disable-cv
  [k]
  (dosync
    (send *config-vars* assoc-in [k :enabled ] false)))

(defn
  ^{
     :doc "Is config-var enabled?"
     :added ""
     :user/comment "A comment."
     }
  cv-enabled?
  [k]
  (get-in @*config-vars* [k :enabled ]))

(defn
  ^{
     :doc "Is config-var disabled?"
     :added ""
     :user/comment "A comment."
     }
  cv-disabled?
  [k]
  (not= true (get-in @*config-vars* [k :enabled ])))

(defmacro
  ^{
     :doc "Execute code when a config-var is enabled."
     :user/comment "A comment."
     }
  with-cv-enabled
  [k & forms]
  (if (cv-enabled? k)
    `(do ~@forms)))

(defmacro
  ^{
     :doc "Execute code when a config-var is disabled."
     :user/comment "A comment."
     }
  with-cv-disabled
  [k & forms]
  (if (cv-disabled? k)
    `(do ~@forms)))

(defn
  ^{
     :doc "Show a config-var in REPL."
     :added ""
     :user/comment "A comment."
     }
  show-cv
  [k]
  (let [cv (k @*config-vars*)
        e (if (:enabled cv) "+" "-")
        v (or (:value cv) "no value")]
    (format "%s %s = %s" e (name k) v)))

(defn
  ^{
     :doc "Persist actual config-vars in a file."
     :added ""
     :user/comment "A comment."
     }
  save-cv
  [& file]
  (f/save-map (or file "config-vars.clj") @*config-vars*))

(defn
  ^{
     :doc "Load config-vars from file."
     :added ""
     :user/comment "A comment."
     }
  load-cv
  [& file]
  (if-let [cv-form (f/load-map (or file "config-vars.clj"))]
    (do
      (dosync
        (send *config-vars* merge cv-form))
      (await *config-vars*)
      true)
    false))
