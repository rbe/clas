;;
;; /Users/rbe/project/clas/src/com/bensmann/clas/log.clj
;; 
;; Copyright (C) 2009-2010 Informationssysteme Ralf Bensmann.
;; Alle Rechte vorbehalten. Nutzungslizenz siehe http://www.bensmann.com/license_de.html
;; All Rights Reserved. Use is subject to license terms, see http://www.bensmann.com/license_en.html
;; 
;; Created by: rbe
;; 
(ns com.bensmann.clas.log
  (:require [clojure.contrib [str-utils :as str-utils]]
            [clj-time [core :as time-core]
                      [format :as time-format]]))

(def log-count-agt (agent 0x0))

(defn
  ^{
    :doc "Documentation for write."
    :added ""
    :user/comment "A comment."
   }
  write
  [msg-id req type & message]
  (let [msg (str-utils/str-join " " message)
        d (time-format/unparse (time-format/formatters :date-hour-minute-second-ms) (time-core/now))
        m (format "%s %h %h %S %s" d msg-id (or (:req-id req) 0) (name type) msg)]
    (do
      (println m)
      (inc msg-id))))

(defmacro
  ^{
    :doc "First execute forms then log."
    :user/comment "A comment."
   }
  with-log
  [{:keys [type message req]}]
  `(send-off log-count-agt write ~req ~type ~message))

(defmacro
  ^{
    :doc "First execute forms then log."
    :user/comment "A comment."
   }
  with-post-log
  [{:keys [type message req]} & forms]
  `(let [result# ~@forms]
     (send-off log-count-agt write ~req ~type ~message)
     result#))

(defmacro
  ^{
    :doc "Log first then execute forms."
    :user/comment "A comment."
   }
  with-pre-log
  [{:keys [type message req]} & forms]
  `(do
     (send-off log-count-agt write ~req ~type ~message)
     ~@forms))

(defmacro with-pre-log-info    [req message & forms] `(with-pre-log {:type :info    :req ~req :message ~message} ~@forms))
(defmacro with-pre-log-warning [req message & forms] `(with-pre-log {:type :warning :req ~req :message ~message} ~@forms))
(defmacro with-pre-log-error   [req message & forms] `(with-pre-log {:type :error   :req ~req :message ~message} ~@forms))
(defmacro with-pre-log-debug   [req message & forms] `(with-pre-log {:type :debug   :req ~req :message ~message} ~@forms))
(defmacro with-pre-log-stats   [req message & forms] `(with-pre-log {:type :stats   :req ~req :message ~message} ~@forms))

(defmacro with-post-log-info    [req message & forms] `(with-post-log {:type :info    :req ~req :message ~message} ~@forms))
(defmacro with-post-log-warning [req message & forms] `(with-post-log {:type :warning :req ~req :message ~message} ~@forms))
(defmacro with-post-log-error   [req message & forms] `(with-post-log {:type :error   :req ~req :message ~message} ~@forms))
(defmacro with-post-log-debug   [req message & forms] `(with-post-log {:type :debug   :req ~req :message ~message} ~@forms))
(defmacro with-post-log-stats   [req message & forms] `(with-post-log {:type :stats   :req ~req :message ~message} ~@forms))

(defmacro with-log-info
  ([message] `(with-log {:type :info :message ~message}))
  ([req message] `(with-log {:type :info :req ~req :message ~message})))

(defmacro with-log-warning
  ([message] `(with-log {:type :warning :message ~message}))
  ([req message] `(with-log {:type :warning :req ~req :message ~message})))

(defmacro with-log-error
  ([message] `(with-log {:type :error :message ~message}))
  ([req message] `(with-log {:type :error :req ~req :message ~message})))

(defmacro with-log-debug
  ([message] `(with-log {:type :debug :message ~message}))
  ([req message] `(with-log {:type :debug :req ~req :message ~message})))

(defmacro with-log-stats
  ([message] `(with-log {:type :stats :message ~message}))
  ([req message] `(with-log {:type :stats :req ~req :message ~message})))
