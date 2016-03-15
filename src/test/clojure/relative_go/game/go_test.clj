(ns relative-go.game.go-test
  (:use [midje.sweet])
  (:require [relative-go.game.go :refer :all]))

(fact "check game "
      (let [game (-> (new-game 9) (play [4 4] 1))]
        (count (playable-moves game 1)) => 80))

(fact "check game "
      (let [game (-> (new-game 9) (play [4 4] 1))]
        (play game [4 5] 0) => truthy
        (play game nil 1) => truthy))
