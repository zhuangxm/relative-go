(ns relative-go.game.board
  (:require [clojure.set :as s]))

;;borad is a vector of vector of map data structure
;; below a example of 3x3 board
;; [ [0 1 2] [1 1 1] [0 0 0] ]

;;position data color 0 = null 1 = black 2 white

(defn row [n initial-value]
  (vec (repeat n initial-value)))

(defn create-board [n initial-value]
  (vec (repeat n (row n initial-value) )))

(defn clear-board
  [board initial-value]
  (create-board (count board) initial-value))

(defn board-size
  [board]
  (count board))

(defn update-stone
  [board move color]
  (-> board
      (assoc-in move color)))

(defn remove-stones
  [board stones]
  (reduce #(update-stone %1 %2 0) board stones))

(defn left
  [board [row col]]
  (when-not (== col 0)
    [row (dec col)]))

(defn down
  [board [row col]]
  (when-not (== row 0)
    [(dec row) col]))

(defn right
  [board [row col]]
  (let [b-size (board-size board)]
    (when-not (== col (dec b-size))
      [row (inc col)])))

(defn up
  [board [row col]]
  (let [b-size (board-size board)]
    (when-not (== row (dec b-size))
      [(inc row) col])))

(defn get-stone-color
  [board move]
  (get-in board move 0))

(defn stone?
  [board move]
  (not= (get-stone-color board move) 0))

(def dirs [left right down up])

(defn color-neighors
  [board move color]
  (->> dirs
       (map #(% board move))
       (filter #(when % (= (get-stone-color board %) color)))))

(defn stone-liberties
  [board move]
  (count (color-neighors board move 0)))

(defn group-liberties
  [board stones]
  (if (seq stones)
    (apply + (map #(stone-liberties board %) stones))
    0))


(defn connected-stone [board stones moves]
  (if-not (seq moves)
    stones
    (let [move (first moves)
          stones (conj stones move)
          color (get-stone-color board move)
          connected-neighbors (set (color-neighors board move color))
          new-stones (s/difference connected-neighbors stones)]
      (connected-stone board stones
                       (concat new-stones (rest moves)))) ))

(defn check-kill
  "return stone that was dead"
  [board move]
   (let [color (get-stone-color board move)
         opponet-color (if (= color 1) 2 1)
         neighors (color-neighors board move opponet-color)
         groups (map #(connected-stone board #{} [%]) neighors)]
     (set (apply concat (filter #(= (group-liberties board %) 0) groups)))))

(defn suicide?
  "return true if it is suicide move"
  [board move]
  (let [stones (connected-stone board #{} [move])]
    (= (group-liberties board stones) 0)))

