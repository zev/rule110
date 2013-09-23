(ns rule110.core
  (:require [rule110.midi :as midi]
            [rule110.graphics :as graphics]))

(def grid-size 1024 )
(def max-steps 100)


(def rule-states [[1 1 1] [1 1 0] [1 0 1] [1 0 0] [0 1 1] [0 1 0] [0 0 1] [0 0 0]])

(defn pad
  [n]
  (let [l (- 8 (count n))]
    (if (zero? l)
      n
      (str (apply str (mapv (constantly "0") (range l))) n))))

(defn all-possible-rules
  []
  (map (fn [n]
         [n (apply hash-map
                   (interleave rule-states
                               (map #(Integer/parseInt (str %))
                                    (pad (Integer/toBinaryString n)))))])
       (range 256)))


;; ;; rule 110
;; (def step-rule {
;;                 [1 1 1] 0,
;;                 [1 1 0] 1,
;;                 [1 0 1] 1,
;;                 [1 0 0] 0,
;;                 [0 1 1] 1,
;;                 [0 1 0] 1,
;;                 [0 0 1] 1,
;;                 [0 0 0] 0
;;                 })

;; (def step-rule {
;;                 [1 1 1] 0,
;;                 [1 1 0] 1,
;;                 [1 0 1] 0,
;;                 [1 0 0] 0,
;;                 [0 1 1] 1,
;;                 [0 1 0] 0,
;;                 [0 0 1] 1,
;;                 [0 0 0] 1
;;                 })

;; (def step-rule {
;;                 [1 1 1] 1,
;;                 [1 1 0] 0,
;;                 [1 0 1] 0,
;;                 [1 0 0] 1,
;;                 [0 1 1] 0,
;;                 [0 1 0] 1,
;;                 [0 0 1] 1,
;;                 [0 0 0] 1
;;                 })

(def step-rule (atom {}))

(defn step
  "Apply the rule to each cell in the grid and return a new grid"
  [grid]
  (let [torus (concat [(last grid)] grid [(first grid)])]
    (map @step-rule (partition 3 1 torus))))


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

(defn run-rule
  ( [rule-num]
      (run-rule rule-num (random-grid 100 25))
      )
  ( [rule-num grid]
      (reset! graphics/rule-name (str rule-num "-g" (str (BigInteger. (apply str grid) 2))))
      (reduce (fn [s v] (-> s step #_draw-grid graphics/draw-frame #_midi/play-grid))
              grid
              (range 250))
      (Thread/sleep 200)
      (graphics/reset-frame)))


(defn -main
  [& args]
  (let [grid (random-grid 100 25)]
    (prn "starting grid " grid)
    #_(prn "all rules " (all-possible-rules))
    (doseq [ [rule-num rule-map] (all-possible-rules)]
      (prn "rule -num " rule-num " map " rule-map)
      (reset! step-rule rule-map)
      (run-rule rule-num grid)))
  (midi/stop)
  nil)
