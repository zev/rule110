(ns rule110.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [rule110.constants :as const]
            [rule110.midi :as midi]
            [rule110.canvas :as canvas]
            [domina :as dm]
            [cljs.core.async :as async :refer [go <! timeout]]))


;; A list of all the possible states a cell and it's immediate
;; neighbors can be in
(def rule-states [[1 1 1] [1 1 0] [1 0 1] [1 0 0] [0 1 1] [0 1 0] [0 0 1] [0 0 0]])

(defn- pad
  "Pad out the binary string to include the prefixed 0s"
  [n]
  (let [l (- 8 (count n))]
    (if (zero? l)
      n
      (str (apply str (mapv (constantly "0") (range l))) n))))

(defn all-possible-rules
  "Return a list of rule number and step-fn's for all possible cellular automata
   in the domain of rule 110 i.e. 1 dimensional automaton"
  []
  (map (fn [n]
         [n (apply hash-map
                   (interleave rule-states
                               (map #(js/parseInt (str %))
                                    (pad (.toString n 2)))))])
       (range 256)))


;; rule 110 for refernce
;; (def step-fn {
;;                 [1 1 1] 0,
;;                 [1 1 0] 1,
;;                 [1 0 1] 1,
;;                 [1 0 0] 0,
;;                 [0 1 1] 1,
;;                 [0 1 0] 1,
;;                 [0 0 1] 1,
;;                 [0 0 0] 0
;;                 })


(defn step
  "Apply the rule to each cell in the grid and return a new grid"
  [grid step-fn]
  (let [torus (concat [(last grid)] grid [(first grid)])]
    (map step-fn (partition 3 1 torus))))


(defn random-grid
  "Generate a random grid of size size with x percent of the cells set to 1"
  [size percent-on]
  (mapv (fn [_] (if (<= (rand-int 100) percent-on)
                  1 0))
        (range size)))


(defn- rule-and-grid-name
  "Return a string of the rule and starting grid state for use in
   titles or file names"
  [rule-num grid ]
  (str rule-num "-g" (.toString (js/parseInt (apply str grid) 2) 16)))

(defn- end-rule
  []
   (canvas/reset))

(defn- iterate-rule
  "Run the rule then call self again in a timeout to allow the drawing
   to show row by row."
  [step-fn grid step-acc]

  (if (< step-acc const/max-steps)
    (let [ngrid (reduce (fn [s v] (-> s
                                     (step step-fn)
                                     canvas/draw-frame
                                     midi/play-grid))
                        grid [1])]
      (go (<! (timeout 100)))
      (js/setTimeout #(iterate-rule step-fn
                                    ngrid
                                    (inc step-acc))
                     100))
    (end-rule)))

(defn run-rule
  "Runs a celular automata based on the passed step-fn passed
   rule-num is just the name of the rule
   step-fn takes a grid and returns a new grid based on the automata rules.
   grid is the state of the starting automata."
  ([rule-num step-fn]
     (run-rule rule-num step-fn (random-grid const/grid-size 25)))

  ([rule-num step-fn grid]
     (reset! canvas/rule-name (rule-and-grid-name rule-num grid))
     (canvas/draw-board)

     (comment
       ;; This is the direct iteration that draws the whole image on call.
       (reduce (fn [s v] (-> s
                            (step step-fn)
                            canvas/draw-frame
                            midi/play-grid))
               grid
               (range const/max-steps))
       (go (<! (timeout 100)))
       #_(canvas/reset))

     ;; even with the async timeout the above seems to only show the grid at the end.
     ;; but now it appears that the midi is not in sync with the line draw
     (iterate-rule step-fn grid 0)
     ))


(defn init-game
  [& args]
  (let [grid (random-grid const/grid-size 25)
        action (or (first args) :all)
        all-rules (all-possible-rules)
        rules (if (= :all (keyword action))
                all-rules
                [(nth all-rules (js/parseInt action))])]
    (dm/log "starting grid " grid " action " action)
    (doseq [ [rule-num rule-map] rules]
      (dm/log "rule -num " rule-num " map " rule-map)
      (run-rule rule-num rule-map grid)))
  ;; event handler to check for command to repeat
  (dm/log "Done")
  (midi/silence)
  nil)


(defn init
  [& args]
  (midi/init #(apply init-game args)))

(set! (.-onload js/window) #(init "110"))
