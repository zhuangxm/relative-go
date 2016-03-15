(ns relative-go.game.board-test
  (:use [midje.sweet]
        [relative-go.game.board]))

(fact "check board update and get"
      (create-board 3 nil) => [ [nil nil nil] [nil nil nil] [nil nil nil]]
      (update-stone (create-board 3 nil) [1 2] 1)
      => [[nil nil nil] [nil nil 1] [nil nil nil]])

(fact "cheeck single stone"
      (let [board (-> (create-board 9 0) (update-stone [4 4] 1))]
        (stone-liberties board [4 4]) => 4
        (color-neighors board [4 4] 1) => empty?
        (check-kill board [4 4]) => empty?
        (connected-stone board #{} [[4 4]]) => #{[4 4]}
        (suicide? board [4 4]) => false))


(fact "check connected-stone"
      (let [board (-> (create-board 9 0)
                      (update-stone [4 4] 1)
                      (update-stone [4 3] 1)
                      (update-stone [4 5] 2))]
        (connected-stone board #{} [[4 4]]) => #{[4 4] [4 3]}
        (connected-stone board #{} [[4 3]]) => #{[4 4] [4 3]}
        (connected-stone board #{} [[4 5]]) => #{[4 5]}))