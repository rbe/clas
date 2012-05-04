;; 
;; /Users/rbe/project/clas/src/com/bensmann/clas/url.clj
;; 
;; Copyright (C) 2009-2010 Informationssysteme Ralf Bensmann.
;; Alle Rechte vorbehalten. Nutzungslizenz siehe http://www.bensmann.com/license_de.html
;; All Rights Reserved. Use is subject to license terms, see http://www.bensmann.com/license_en.html
;; 
;; Created by: rbe
;; 
(ns com.bensmann.clas.url
  (:use (clojure.contrib str-utils)))

(def *tiny-urls* (ref {}))

(defn
  ^{
    :doc "Makes a tiny URL using hash function and shortening it by generating a hex string."
    :added ""
    :user/comment "A comment."
   }
  make-tiny-url
  [^String url]
  (Integer/toHexString (hash url)))

(defn decode-url
  "Decode a urlencoded string using the default encoding."
  [s]
  (java.net.URLDecoder/decode s "UTF-8"))

(defn assoc-vec
  "Associate a key with a value. If the key already exists in the map, create a
  vector of values."
  [map key val]
  (assoc map key
    (if-let [cur (map key)]
      (if (vector? cur)
        (conj cur val)
        [cur val])
      val)))

(defn parse-params
  "Parse parameters from a string into a map."
  [param-string separator]
  (reduce
    (fn [param-map s]
      (let [[key val] (re-split #"=" s)]
        (assoc-vec param-map
          (keyword (decode-url key))
          (decode-url (or val "")))))
    {}
    (remove #(or (nil? %) (= % ""))
      (re-split separator param-string))))

(defn
  ^{
    :doc "Encode URL parameters. Input is a map with keywords as keys.
  user=> (encode-url-params (parse-params \"A=1&b=2\" #\"&\"))
  b=2&A=1"
    :added ""
    :user/comment ""
   }
  encode-url-params
  [params]
  (let [encode #(java.net.URLEncoder/encode (str %) "UTF-8")
        coded (for [[n v] params]
                (format "%s=%s" (encode (name n)) (encode v)))]
    (apply str (interpose "&" coded))))
