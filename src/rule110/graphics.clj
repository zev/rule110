(ns rule110.graphics
  (:require [rule110.constants :as const])
  (:import (java.awt Color Graphics Dimension)
           (java.awt.image BufferedImage)
           (javax.swing JPanel JFrame)
           (javax.imageio ImageIO)
           (java.io File)))

(set! *warn-on-reflection* true)

(def dim-board   [const/grid-size const/max-steps])
(def dim-screen  [1000  750])
(def dim-scale   (mapv / dim-screen dim-board))

;; a place holder to keep track of the currently running rule for
;; output to a file and title windows
(def rule-name (atom ""))

;; A place to holder the progression of the automata
(def board (atom []))

(def on-color java.awt.Color/GREEN)
(def off-color java.awt.Color/BLACK)

(defn with-coords [board]
  (for [[row-idx row] (map-indexed vector board)]
    (for [[col-idx val] (map-indexed vector row)]
      [val col-idx row-idx])))

(defn render-cell [^Graphics g cell]
  (let [[state xo yo] cell
        [x-scale y-scale] dim-scale
        x (* xo x-scale)
        y (* yo y-scale)]
    (doto g
      (.setColor (if (= 1 state) on-color off-color))
      (.fillRect x y x-scale y-scale))))

(defn write-img-to-file
  "Write the current image to a file"
  [img]
  (let [filename (str "./rules/output_" @rule-name ".png")]
    (prn "writing image to " filename)
    (ImageIO/write img "png" (File. filename))
    (Thread/sleep 100)))

(defn render [graphics img board]
  (let [background-graphics (.getGraphics img)]
    (doto background-graphics
      (.setColor off-color)
      (.fillRect 0 0 (dim-screen 0) (dim-screen 1)))
    (doseq [row (with-coords board)
            cell row]
      (render-cell background-graphics cell))
    (.drawImage graphics img 0 0 nil))
  (when (= const/max-steps (count board))
    (write-img-to-file img)))


(def ^JPanel panel
  (let [[screen-x screen-y] dim-screen
        img (BufferedImage. screen-x screen-y BufferedImage/TYPE_INT_ARGB)]
    (doto (proxy [JPanel] []
            (paint [g] (render g img @board)))
      (.setPreferredSize (Dimension.
                          screen-x
                          screen-y)))))

(def frame (doto (JFrame.)
             (.add panel)
             .pack .show
             (.setTitle "Cellular Automata")
             (.setDefaultCloseOperation javax.swing.JFrame/DISPOSE_ON_CLOSE)))

(defn draw-frame
  "Draw the state of the current automata's grid"
  [grid]
  (when (zero? (count @board))
    (.setTitle frame @rule-name))
  (swap! board conj grid)
  (.repaint panel)
  grid)

(defn reset
  "Reset the screen for a fresh drawing"
  []
  (reset! board []))
