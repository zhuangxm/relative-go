(ns relative-go.ai.core
  (:require [relative-go.game.board :as b]
            [relative-go.ai.influence :as i]
            [clojure.string :as str]
            [relative-go.game.go :as go]))

(comment

  (defn play
    [board color move]
    (-> board
        (i/update-board-influence color move)
        (b/play color move)))

  (defn max-influence-move
    [board color]
    (let [board-size (count board)
          influences (for [row (range board-size)
                           col (range board-size) :when (can-play board row col)]
                       (let [new-board (play board color [row col])
                             influence (i/calculate-influence new-board)]
                         [[row col] influence]))]
      (->> influences
           (reduce #(i/compare-influence color %1 %2))
           first)))

  (defn gen-move
    [board color]
    (max-influence-move board color)))

(defn random-move!
  [game color]
  (let [moves (go/playable-moves game color)
        c (count moves)]
    (when  (> c 0) (nth moves (rand-int c)) )))

(defn gen-move
  [game color]
  (random-move! game color))



