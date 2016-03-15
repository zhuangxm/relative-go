(ns relative-go.ai.influence-test
  (:use [midje.sweet])
  (:require [relative-go.ai.influence :refer :all]))

(fact "update-position-influence"
      (update-position-influence {} :black 3) => {:influence {:black 3}}
      (update-position-influence {:influence {:black 3}} :black 5) => {:influence {:black 8}}
      (update-row-influence 2 [{} {} {}] :white [1 1])
      => [{:influence {:white 0.5}} {:influence {:white 1.0}} {:influence {:white 0.5}}])

(fact "calculate-row-influence"
      (calculate-row-influence
        [{:influence {:white 0.5}}
         {:influence {:white 1.0 :black 0.5}}
         {:influence {:white 0.5}}])
      => {:white 2.0 :black 0.5})

(fact "calculate-influence"
      (calculate-influence
        [[{:influence {:white 0.5}}
          {:influence {:white 1.0 :black 0.5}}
          {:influence {:white 0.5}}]
         [{:influence {:white 23}} {:influence {:black 23}}]])
      => {:white 25.0 :black 23.5})