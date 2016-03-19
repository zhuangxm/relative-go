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
      (position->move 19 "b1") => [18, 1]
      (position->move 19 "J8") => [11, 8]
      (move->position 19 [11,8]) => "J8"
      (move->position 19 [6, 1]) => "B13"
      (move->position 19 nil) => "pass"
      (position->move 19 "pass") => nil)


