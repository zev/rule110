(ns rule110.core
  (:require [rule110.midi :as midi]))

(def grid-size 1024 )
(def max-steps 100)

(def step-rule {
                [1 1 1] 0,
                [1 1 0] 1,
                [1 0 1] 1,
                [1 0 0] 0,
                [0 1 1] 1,
                [0 1 0] 1,
                [0 0 1] 1,
                [0 0 0] 0
                })

(defn step
  "Apply the rule to each cell in the grid and return a new grid"
  [grid]
  (let [torus (concat [(last grid)] grid [(first grid)])]
    (map step-rule (partition 3 1 torus))))


(defn random-grid
  "Generate a random grid of size size with x percent of the cells set to 1"
  [size percent-on]
  (mapv (fn [_] (if (<= (rand-int 100) percent-on)
                  1 0))
        (range size)))

(defn draw-grid
  "Simple drawing of grid to terminal and return grid"
  [grid]
  (doseq [c grid]
    (print (if (= 1 c) "*" " ")))
  (println "")
  grid)

(defn -main
  [& args]
  (reduce (fn [s v] (-> s step draw-grid midi/play-grid))
          (random-grid 100 25)
          (range 250))
  nil)
