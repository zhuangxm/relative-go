(ns relative-go.sgf.core
  (:require [clojure.string :as str]
            [relative-go.game.go :as go]
            [relative-go.game.board :as b]))

(defn trim-str
  [content]
  (str/trim content))

(defn parse-property-values
  "return [value1 value2 ... remains-string]"
  [content]
  (let [s (trim-str content)
        p_start (str/index-of s "[")
        p_end (str/index-of s "]")]
    (if (and p_start (= p_start 0) p_end)
      (cons (subs s (inc p_start) p_end)
            (parse-property-values (subs s (inc p_end))))
      [s])))

(defn legal-property
  [s]
  (every? #(Character/isLetter ^char %) s))

(defn parse-properties
  "return [p1 p2 remain-string]
   p1 p2 properties : {name value}"
  [content]
  (let [s (trim-str content)
        p_start (str/index-of s "[")]
    (if (and p_start (legal-property (subs s 0 p_start)))
      (let [vs (parse-property-values (subs s p_start))]
        (cons
          [(subs s 0 p_start) (drop-last 1 vs)]
          (parse-properties (last vs))))
      [s])))

(declare parse-tree)
(declare parse-node-or-tree)

(defn parse-node
  "return [node remain-string] , content must include at least one node"
  [content]
  (let [s (trim-str content)
        node_start (str/index-of s ";")
        ps (parse-properties (subs s (inc node_start)))]
    (vector :node (into {} (drop-last ps)) (last ps))))

(defn parse-nodes-or-trees
  [content]
  (let [s (trim-str content)
        node_start (str/index-of s ";")
        tree_start (str/index-of s "(")
        tree_end (str/index-of s ")")
        rs (when (not= 0 tree_end)
             (cond
               (and node_start tree_start) (if (< node_start tree_start) (parse-node s) (parse-tree s))
               (and node_start) (parse-node s)
               (and tree_start) (parse-tree s)))]
     (if rs
       (cons (drop-last rs) (parse-nodes-or-trees (last rs)))
       [s])))

(defn parse-tree
  "return [node1 node2 remain-string], content must include at least one tree"
  [content]
  (let [s (trim-str content)
        tree_start (str/index-of s "(")
        rs (parse-nodes-or-trees (subs s (inc tree_start)))
        remains (last rs)]
    (cons :tree (conj (vec (drop-last rs)) (subs remains (inc (str/index-of remains ")")))))))

(defn parse
  [str-sgf]
  (drop-last (parse-tree str-sgf)))

(defn parse-file
  [file-name]
  (parse (slurp file-name)))

(defn take-only-one-tree
  "only keep one tree (the main tree)"
  [sgf]
  (let [[notes, trees] (split-with #(not= (first %) :tree) sgf)]
    (concat notes (take 1 trees))))

(defn main-branch
  [sgf]
  (let [k (first sgf)
        steps (cond
                (= k :tree) (mapcat main-branch (take-only-one-tree (rest sgf)))
                (= k :node) (rest sgf))]
    (->> steps
         (map #(select-keys % #{"AW", "AB", "B", "W" "SZ"}))
         (filter seq))))

(defn place-stones
  [game moves color]
  (reduce #(go/play %1 %2 color) game moves))

(defn cord->move
  "将 bd -> [3,1] tt -> nil empty string -> nil"
  [cord]
  (when (and (== (count cord) 2) (not= "tt" cord))
    [(- (int (second cord)) (int \a))
     (- (int (first cord)) (int \a))]))

(defn play-AW
  [game cords]
  (place-stones game (map cord->move cords) b/COLOR_WHITE))

(defn play-AB
  [game cords]
  (place-stones game (map cord->move cords) b/COLOR_BLACK))

(defn play-B
  [game cord]
  (go/play game (cord->move cord) b/COLOR_BLACK))

(defn play-W
  [game cord]
  (go/play game (cord->move cord) b/COLOR_BLACK))

(defn play-prop
  "k is one of [AB AW B W], v is two letter cord like df"
  [game k v]
  (cond
    (= k "AW") (play-AW game v)
    (= k "AB") (play-AB game v)
    (= k "B") (play-B game (first v))
    (= k "W") (play-W game (first v))
    :default game))

(defn play-node
  [game node]
  (let [ks (keys node)]
    (reduce #(play-prop %1 %2 (get node %2)) game ks)))

(defn play [sgf-data]
  (let [board-size (-> (first sgf-data) (get "SZ" ["19"]) (first) (Integer/parseInt))
        game (go/new-game board-size)]
    (reduce #(play-node %1 %2) game sgf-data)))