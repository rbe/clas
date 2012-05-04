(ns com.bensmann.email.server
  (:use (com.bensmann.email util email email-check routing)))

;; New TCP session: do we accept client? Rate limiting, etc.
;; 		test: IP from an public, assigned range? check against blacklists
;; Generate a SMTP-session-ID
;; SMTP session initiated: display greeting
;; Read commands and execute action
;; HELO/EHLO
;;          test: hostname
;; MAIL FROM
;; RCPT TO
;;          test: do we relay mail for recipient?
;; DATA ends with newline . newline
;; Persist mail as Clojure data structure
;; Close SMTP session with meaningful return code
;; Check email and add result to Clojure data structure
(def eml (struct-map email :header (struct-map header :from "ralf@bensmann.com") :body "blabla"))
(def result (check-email eml))
(println (:check-subject @result))
;; Calculate weight of checks: accept mail or consider it spam?
;; Route email
