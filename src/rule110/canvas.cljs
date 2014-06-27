(ns rule110.canvas
  (:require [domina :as dm]
            [rule110.constants :as const]))

(def html-board (dm/by-id "board"))
(def canvas (.getContext html-board "2d"))


(def dim-board   [const/grid-size const/max-steps])
(def dim-screen  [1000  750])
(def dim-scale   (mapv / dim-screen dim-board))

(def rule-name (atom ""))

;; A place to holder the progression of the automata
(def board (atom []))

(def on-color "green")
(def off-color "black")

(defn with-coords
  ([board]
      (for [[row-idx row] (map-indexed vector board)]
        (with-coords row-idx row)))
  ([row-idx row]
     (for [[col-idx val] (map-indexed vector row)]
          [val col-idx row-idx])))

(defn render-cell [g cell]
  (let [[state xo yo] cell
        [x-scale y-scale] dim-scale
        x (* xo x-scale)
        y (* yo y-scale)]
    (set! (.-fillStyle g) (if (= 1 state) on-color off-color))
    (.fillRect g x y x-scale y-scale)))


(defn draw-board
  []
  (set! (.-fillStyle canvas) off-color)
  (.fillRect canvas 0 0 (dim-screen 0) (dim-screen 1)))

(defn render [graphics board]
  (draw-board)
  (doseq [row (seq (with-coords board))
          cell row]
    (render-cell graphics cell)))

(defn render-row [graphics row-idx row]
  (doseq [cell (seq (with-coords row-idx row))]
    (render-cell graphics cell)))

(defn draw-frame
  "Draw the state of the current automata's grid"
  [grid]
  (when (zero? (count @board))
    "setup the title here or in the driver")

  (swap! board conj grid)
  (render-row canvas (count @board) grid)
  grid)

(defn reset
  "Reset the screen for a fresh drawing"
  []
  (reset! board []))
