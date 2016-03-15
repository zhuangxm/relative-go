(ns relative-go.core-test
  (:require [midje.sweet :refer :all]
            [relative-go.core :refer :all]))

(fact "test color convert"
      (str->color "b") => 1
      (str->color "B") => 1
      (str->color "w") => 2)

(fact "letter num convert"
      (letter->num \Z) => 24
      (letter->num \A) => 0
      (letter->num \J) => 8
      (num->letter 0) => \A
      (num->letter 24) => \Z
      (num->letter 8) => \J)

(fact "test position convert"
      (position->move "b13") => [12, 1]
      (position->move "J8") => [7, 8]
      (move->position [7,8]) => "J8"
      (move->position [12, 1]) => "B13"
      (move->position nil) => "pass"
      (position->move "pass") => nil)


