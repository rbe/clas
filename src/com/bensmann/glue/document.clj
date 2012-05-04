(ns com.bensmann.glue.document)

(defstruct document-s :meta)
(defstruct document-meta-s :id :version :created-at :created-by :updated-at :updated-by)

;; create an instance of a document
(defn create
    [ds data]
    (let [d (merge (struct-map ds) data)
          m (struct-map document-meta-s :version 1 :created-at (java.util.Date.))]
        (assoc-in d [:meta] m)))

;; update an existing document
(defn update
    [o n]
    (let [o-meta (:meta o)
          n-version (+ 1 (:version o-meta))
          n-meta (merge o-meta {:version n-version :updated-at (java.util.Date.)})
          d (merge o n {:meta n-meta})]
        d))

(def ma1 (create document-s {:name "ma1"}))
(def ma2 (create document-s {:name "ma2" :zip 48629}))
(def ma3 (update ma1 ma2))
(println ma3)
