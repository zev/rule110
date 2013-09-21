(ns rule110.core-test
  (:use clojure.test
        rule110.core))

(deftest a-test
  (testing "Drawing a random grid"
    (let [g (random-grid 5 50)]
      (prn g)
      (is (= 0 1)))))
