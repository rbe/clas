;;
;; /Users/rbe/project/clas/src/com/bensmann/clas/stats.clj
;; 
;; Copyright (C) 2009-2010 Informationssysteme Ralf Bensmann.
;; Alle Rechte vorbehalten. Nutzungslizenz siehe http://www.bensmann.com/license_de.html
;; All Rights Reserved. Use is subject to license terms, see http://www.bensmann.com/license_en.html
;; 
;; Created by: rbe
;; 
(ns com.bensmann.clas.stats
  (:use (com.bensmann.clas log))
  (:import (java.util.concurrent TimeUnit)))

(defn
  ^{
    :doc "Documentation for memory-info. See http://blog.codebeach.com/2008/02/determine-available-memory-in-java.html"
    :added ""
    :user/comment "A comment."
   }
  memory-info
  []
  (let [r (Runtime/getRuntime)
        [maxmem freemem allocatedmem] (vector (.maxMemory r) (.freeMemory r) (.totalMemory r))
        maxmem-mb (double (/ maxmem 1024 1024))
        freemem-mb (double (/ freemem 1024 1024))
        allocatedmem-mb (double (/ allocatedmem 1024 1024))
        usedallocated-mb (double (/ (- allocatedmem freemem) 1024 1024))
        totalfree-mb (double (/ (+ freemem (- maxmem allocatedmem)) 1024 1024))]
    {:maxmem maxmem-mb
     :allocatedmem allocatedmem-mb
     :usedallocated usedallocated-mb
     :freeallocated freemem-mb
     :totalfree totalfree-mb}))

(def stats-time-agt (agent 0))

(defmacro
  ^{
    :doc "Documentation for my-time."
    :user/comment "A comment."
   }
  with-stats
  [req & forms]
  `(do
    ~@(for [form# forms]
      `(let [start# (System/nanoTime)
             result# ~form#
             end# (System/nanoTime)
             diff-ns# (- end# start#)
             diff-ms# (.toMillis TimeUnit/NANOSECONDS diff-ns#)]
        ;; (println (format "%s = %s took %d ns = %d ms" '~form# result# diff-ns# diff-ms#))
        (with-log-stats ~req (format "%s = %s took %d ns = %d ms" '~form# result# diff-ns# diff-ms#))
        (send-off stats-time-agt + diff-ns#)
        result#))))

(defn
  ^{
    :doc "Return formatted representation of timing statstics."
    :added ""
    :user/comment "A comment."
   }
  time-stats->string
  []
  (let [overall-ns# (do (await stats-time-agt) @stats-time-agt)
        overall-ms# (.toMillis TimeUnit/NANOSECONDS overall-ns#)
        overall-min# (.toMinutes TimeUnit/NANOSECONDS overall-ns#)
        overall-hrs# (.toHours TimeUnit/NANOSECONDS overall-ns#)]
  (format "overall computational time %d ms = %d min = %d hours" overall-ms# overall-min# overall-hrs#)))
