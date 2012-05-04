(ns com.bensmann.email.util)

(defn get-sym-name [f]
	"Get name of a symbol"
	(:name (meta (resolve f))))

(defn call-fn-by-sym [f arg]
	"Call a function by its symbol."
	((resolve f) arg))

(defn all-keys
  "Find a key in a list of maps."
  [k & m]
  (map k m))

(defn find-key-value
  "Lookup a certain value of a key in a list of maps and return the corresponding map."
  [k v & m]
  )