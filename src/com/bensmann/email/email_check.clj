(ns com.bensmann.email.email-check
	(:use (com.bensmann.email util email)))

;; Email checks
(defn check-from
  "Docstring for check-from."
  [email]
	(not= nil (get-hdr email :from)))

(defn check-subject
  "Docstring for check-subject."
  [email]
	(not= nil (get-hdr email :subject)))

;; Header and body checks, external tools
;; Used e.g. during SMTP session or after SMTP session
(def header-check '[check-from check-subject])
(def body-check '[])
(def external-tool '[])

(defn add-header-check
  "Docstring for add-header-check."
  [fn])

(defn add-body-check
  "Docstring for add-body-check."
  [])

(defn add-external-tool
  "Docstring for add-external-tool."
  [])

(defn all-header-check
  "Produce a vector with all header checks."
  []
	(concat header-check body-check external-tool))

(defn check-email
  "Check an email: header, body, attachments."
  [email]
  (let [check (all-header-check)
		result (ref {})]
	(doseq
		(for [f check] (let [k (keyword (get-sym-name f))
							v (call-fn-by-sym f email)]
							(dosync (commute result assoc k v)))))
  result))
