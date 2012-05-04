(ns rms)

;; a list of rules each having 1..* facts
(def *rules* (agent (vector)))

;; Fact: map contains key :name
(defn make-fact
  "A fact is a statement about data and can be true or false."
  [^String name ^IFn fun]
  {:name name :fun fun})

;; Used as a getter to overcome structural changes.
(defn factfn
  "Get the function of a fact."
  [^Map fact]
  (:fun fact))

(defn apply-factfn
  "Apply a fact function against data."
  [^Map fact ^Map data]
  ((:fun fact) data))

;; 
(defn make-rule
  "Creates a rule that evaluates all facts and calls action function.
Return value of action function is used for further processing.
name: a keyword,
facts: a list of functions returning Boolean"
  [^String name ^Vector facts ^IFn action]
  ;; todo: detect duplicates (name) and/or replace
  (send *rules* conj {:name name :facts facts :action action}))

(defn has-fact-n
  "Has a rule the fact with index n?"
  [rule n]
  (if (get (:facts rule) n)
    true
    false))

(defn facts
  "Get all facts of a rule."
  [rule]
  (:facts rule))

;; (defn apply-rule
;;   "Apply a single rule and return vector in an atom with result of each fact."
;;   [^Map rule ^Map data]
;;   (let [^Atom result (atom [])]
;;     (for [^IFn fact (facts rule)]
;;       (let [r (apply-factfn fact data)]
;;         ;; (println (:name rule) (:name fact) r)
;;         (swap! result #(conj % r))))
;;     result))

;; ca 0.07msecs
(defn eval-rule
  "Apply a single rule and return vector in an atom with result of each fact."
  [^Map rule ^Map data]
  (for [^IFn fact (:facts rule)]
    {:name (:name fact) :result (apply-factfn fact data)}))

;; ca. 0.09msecs
;; (defn eval-rule
;;   "Apply a single rule and return vector in an atom with result of each fact."
;;   [^Map rule ^Map data]
;;   (loop [^Integer cnt 0
;;          ^Map fact (get (facts rule) cnt)
;;          ^Atom result (atom [])]
;;     ;; (println "evaluating:" (:name rule) "fact" cnt (:name fact) "? result==" result)
;;     (let [r (apply-factfn fact data)]
;;       (swap! result #(conj % r))
;;       ;; (println "evaluated:" (:name rule) "fact" cnt (:name fact) "==" r)
;;       (if (get (facts rule) (inc cnt))
;;         (recur (inc cnt) (get (facts rule) (inc cnt)) result)
;;         result))))

(defn eval-rules
  "Apply a list of rules against data. Returns a vector of maps with
rule name and result of each fact."
  [^Vector rules ^Map data]
  (for [^Map rule rules]
    {:name (:name rule) :facts (eval-rule rule data)}))

;;
;; Customer: name=ABC zip=48629
;; Test customer against: has name, zip and city?
;;
(make-rule "name-rule"
           [(make-fact "name == ralf"
                       (fn [^Map x] (= "ralf" (:name x))))]
           (fn [data results] "Hi, I am an action!"))

(make-rule "zip-rule"
           [(make-fact "zip == 48629"
                       (fn [^Map x] (= 48629 (:zip x))))]
           (fn [data results] "Hi, I am an another action!"))

(make-rule "last-contacted-rule"
           [(make-fact "within last 5 days"
                       (fn [^Map x] (diff today <= 5 (:last-contact-date x))))]
           (fn [data results] "Hi, I am an another action!"))

(make-action "write email an chef"
             )

(eval-rule (@*rules* 0) {:name "ralf" :zip 48629})
(eval-rules @*rules* {:name "ralf" :zip 48629})

;; Every rule has access to all results of previously evaluated rules
;; There's a data store for variables used for evaluation of a rule chain

;; eval and apply each rule and then, depending on result, proceed with next rule
(make-decision "contact customer?"
               [:rule "#1" :when [:one-true :continue-with-rule "#3" :score +3
                                  :one-false
                                  :any-true
                                  :any-false
                                  :some-true
                                  :some-false
                                  :all-true :action action-fn
                                  :all-false]
                :rule "#2" :when [:all-true :action action-fn
                                  :none-true :continue-with-rule "#5"]
                :rule "#3" :when cond :action bla
                :rule "#4" :eval fun ;; eval fun after applying rule #4
                ])

(decide-on {:name "ralf" :zip 48629} "contact customer?")

;; user> (postwalk tmp1 '[:when hey-ho])
;; "transforming [\"transforming :when\" \"transforming hey-ho\"]"
;; user> (defn tmp1 [attr] (format "transforming %s" attr) attr)
;; #'user/tmp1
;; user> (postwalk tmp1 '[:when hey-ho])
;; [:when hey-ho]
;; user> (defn tmp1 [attr] (println (type attr)) attr)
;; #'user/tmp1
;; user> (postwalk tmp1 '[:when hey-ho])
;; clojure.lang.Keyword
;; clojure.lang.Symbol
;; clojure.lang.PersistentVector
;; [:when hey-ho]
