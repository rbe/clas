(ns com.bensmann.glue.beta)

;; document set == rdbms table

;; an io or storage pool defines where and how to store data
(def io-pools {
                     :pool-1 {
                              :location ""
                              :encrypt fun
                              :decrypt fun
                              :read-thread# 1
                              :write-thread# 1
                              :io-actions {
                                           :pre-load []
                                           :post-load []
                                           :pre-save []
                                           :post-save []
                                           }
                              }
                     :pool-2 {
                              }
                     })

;; processing instructions can be applied to a single field or a whole document
;; storage-pool: where and how to store
;; actions: just functions called with value as parameter in a certain stage of processing
(def processing-instructions {
                              :io-pool :pool-1
                              :data-actions {
                                             :pre-load []
                                             :post-load [] ;; incl. validator
                                             :pre-save [] ;; incl. validator
                                             :post-save [] ;; incl. validator
                                             }
                              ;; :pre-validator nil
                              ;; :post-validator nil
                              :approval {
                                         :need fun ;; determines the need for an approval
                                         :user "ralf@bensmann.com"
                                         :dealine "2011/02/01"
                                         :default true
                                         }
                              })

;; field
;; = variable; name with possibly associated value
;; has processing instructions
;; with validator can define valid value, list of possible values, ...
;; may have references to other field(s)
(def field {
            :processing-instructions {}
            :objectid 1
            :name :field1
            :value nil
            })

;; document
;; has processing instructions
;; has zero to x field(s)
(def document {
               :processing-instructions nil
               :objectid 2
               :field {field1 field2}
               :process-functions [
                                   fun1
                                   fun2
                                   ]
               })
