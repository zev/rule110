(ns rule110.graphics
  (:import (java.awt Color Graphics Dimension)
           (java.awt.image BufferedImage)
           (javax.swing JPanel JFrame)
           (javax.imageio ImageIO)
           (java.io File)))

(set! *warn-on-reflection* true)


(def dim-board   [100  250])
(def dim-screen  [1000  750])
(def dim-scale   (mapv / dim-screen dim-board))
(def rule-name (atom ""))

(def current-row (atom 0))
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
        x #_(inc) (* xo x-scale)
        y #_(inc) (* yo y-scale)]
    #_(prn "X " x " y " y " NX " x " NY " y)
    (doto g
      (.setColor (if (= 1 state) on-color off-color))
      (.fillRect x y x-scale #_(dec x-scale) #_(dec y-scale) y-scale))))

(defn render [graphics img board]
  (let [background-graphics (.getGraphics img)]
    (doto background-graphics
      (.setColor off-color)
      (.fillRect 0 0 (dim-screen 0) (dim-screen 1)))
    (doseq [row (with-coords board)
            cell row]
      (render-cell background-graphics cell))
    (.drawImage graphics img 0 0 nil))
  (when (= 250 (count board))
    (prn "writing image")
    (ImageIO/write img "png" (File. (str "./rules/output_" @rule-name ".png")))
    (Thread/sleep 200)))



(def ^JPanel panel
  (let [[screen-x screen-y] dim-screen
        img (BufferedImage. screen-x screen-y BufferedImage/TYPE_INT_ARGB)]
    (doto (proxy [JPanel] []
            (paint [g] (render g img @board)))
      (.setPreferredSize (Dimension.
                          screen-x
                          screen-y)))))

(def frame (doto (JFrame.) (.add panel)
                 .pack .show
                 (.setDefaultCloseOperation javax.swing.JFrame/DISPOSE_ON_CLOSE)))

(defn draw-frame
  [grid]
  (swap! board conj grid)
  (.repaint panel)
  (when (zero? (count @board))
    (.setTitle frame @rule-name))
  grid)

(defn reset-frame
  []
  (reset! board []))
