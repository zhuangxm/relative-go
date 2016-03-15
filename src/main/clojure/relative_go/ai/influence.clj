(ns relative-go.ai.influence)

(defn default-influence
  [[row1,col1] [row2 col2]]
  (let [d-row (- row2 row1)
        d-col (- col2 col1)]
    (if (and (= d-row 0) (= d-col 0))
      0
      (/ 1.0 (+ (* d-row d-row) (* d-col d-col))))))

(defn update-position-influence
  [m color influence]
  (update-in m [:influence color] #((fnil + 0) % influence)))

(defn update-row-influence
  [row v-row color move]
  (vec (for [col (range (count v-row))]
         (let [influence (default-influence [row col] move)]
           (update-position-influence (nth v-row col) color influence)))))

(defn update-board-influence
  [board color move]
  (let [board-size (count board)]
    (vec (for [i (range board-size)]
           (update-row-influence i (nth board i) color move)))))

(defn normalize-influce
  [influence]
  (let [b-i (get influence :black 0)
        w-i (get influence :white 0)]
    (if (not= b-i w-i 0)
      (let [len (Math/sqrt (+ (* b-i b-i) (* w-i w-i)))]
        {:black (/ b-i len) :white (/ w-i len)})
      influence)))

(defn addup-influence
  [r m]
  (-> r
      (update-in [:white] #(+ % (get-in m [:white] 0)))
      (update-in [:black] #(+ % (get-in m [:black] 0)))))

(defn calculate-row-influence
  [v-row]
  (reduce addup-influence
          {:white 0 :black 0}
          (map (-> :influence normalize-influce) v-row)))

(defn calculate-influence
  [board]
  (reduce addup-influence
          {:white 0 :black 0}
          (map calculate-row-influence board)))

(defn black-net-influce
  "black influence minus white influce"
  [influence]
  (- (get influence :black 0)
     (get influence :white 0)))

(defn compare-black-influence
  "if the black influence of influence1 is better than influence2
  return true otherwise false"
  [influence1 influence2]
  (let [norm1 (normalize-influce influence1)
        norm2 (normalize-influce influence2)]
    (if (= (:black norm1) (:black norm2))
      (> (:black influence1) (:black influence2))
      (> (:black norm1) (:black norm2)))))

(defn compare-influence
  [color old-move new-move]
  (let [old-influence (second old-move)
        new-influence (second new-move)]

    (if (= (= color :black)
           (compare-black-influence new-influence old-influence))
      new-move
      old-move)))


