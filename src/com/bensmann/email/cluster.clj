;; Every node has its own set of data
;; Every node has one or more backup nodes
;; Before committing an action the node has to ensure that the data arrived at backup node(s)
;; A backup node should observe the other node and in case of failure
;; it must a) partition data to all remaining nodes b) complete its work
