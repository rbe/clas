(ns com.bensmann.email.email
	(:use (com.bensmann.email util)))

;; The email: header, body and attachment(s)
(defstruct email :header :body :attachment)

;; A header
(defstruct header :date :from :to :cc :bcc :subject)
(defstruct hdr :name :val)

;; An attachment
(defstruct attachment :name :data)

(defn make-header
  "Docstring for make-header."
  [name & val]
  (struct-map hdr :name name :val val))

(defn make-email
  "Docstring for make-email."
  [header body & attachment]
  (struct-map email :header header :body body :attachtment attachment))

(defn get-hdr
	"Get header of email."
	[email hdr]
	(hdr (:header email)))

(defn get-bdy
	"Get body of email."
	[email]
	(:body email))

(defn get-att
	"Get attachment from email."
	([email] (:attachment email))
	([email attname] (attname (:attachment email))))

(def h (make-header 'subject "bla"))
(println h)
(def e (make-email h "body"))
(println (get-hdr email :subject))
