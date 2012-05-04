(ns bla.ralf
  (:use lazytest.describe))

(describe + "with integers"
  (it "adds small numbers"
    (= 7 (+ 3 4)))
  (it "adds large numbers"
    (= 53924864 (+ 41885013 12039851)))
  (it "adds negative numbers"
    (= -10 (+ -4 -6)))
  (it "subtracts"
    (= 1 (- 1 1))))

(describe "The for-each macro"
  (for-each "tests the same expression with many values"
    [x y z] (= z (+ x y))
    [1 1 2]
    [2 2 4]
    [3 4 7]))

(spec minus "The minus function"
  (spec one-arg "when called with one argument"
    (spec negates "negates that argument"
      (is (= -1 (- 1))
        (= -2 (- 2))))))
