(ns relative-go.game.go
  (:require [relative-go.game.board :as b]))

;; player true black false white
;; {:last-removed #{[row col] [row col]} :last-move [row col] :board ... :next-player true/flase
;;  :board-size 9 :moves [[row col], [row col]]

(defn new-game [board-size]
  {:board (b/create-board board-size 0)
   :board-size board-size
   :next-player true
   :moves []})

(defn try-play?
  "return true success, false fail"
  [game move color]
  (let [board (:board game)
        new-board (b/update-stone board move color)
        dead-stones (b/check-kill new-board move)]
    (cond
      ;;cannot kill only the stone that just killed only you
      (and (= (:last-removed game) #{move})
           (= dead-stones #{(:last-move game)})) false
      ;;suicde is not allowed
      (and (nil? (seq dead-stones)) (b/suicide? new-board move)) false
      :default true)))

(defn leagal-move?
  "if the move is leagal, move is nil means pass"
  [game move color]
  (or (nil? move)
      (and (not (b/stone? (:board game) move))
           (try-play? game move color))))

(defn playable-moves
  [game color]
  (let [board-size (:board-size game)]
    (for [i (range board-size) j (range board-size)
          :when (leagal-move? game [i,j] color)]
      [i,j])))

(defn play
  "play a move return a new game"
  [game move color]
  (if-not (leagal-move? game move color)
    (throw (ex-info "invalid move" {:game game :move move :color color}))
    (let [dead-stones (when move (-> (:board game)
                                     (b/update-stone move color)
                                     (b/check-kill move)))
          new-board (if move (-> (:board game)
                                 (b/update-stone move color)
                                 (b/remove-stones dead-stones))
                             (:board game))]
      (-> game
          (assoc :last-removed dead-stones)
          (assoc :board new-board)
          (assoc :last-move move)
          (update-in [:moves] #(conj % move))
          (update-in [:next-player] not)))))
