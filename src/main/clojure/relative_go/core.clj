(ns relative-go.core
  (:require [clojure.string :as str]
            [relative-go.ai.core :as ai]
            [relative-go.game.go :as go]
            [relative-go.sgf.core :as sgf]
            [relative-go.game.board :as b]))

(defn int?
  [s]
  (try
    (Integer/parseInt s)
    (catch Exception e false)))

(defn letter->num
  "A-Z exclue I, 0..24"
  [ch]
  (if (>= (int ch) (int \I))
    (- (int ch) (int \A) 1)
    (- (int ch) (int \A))))

(defn num->letter
  "A-Z exclue I, 0..24"
  [n]
  (if (>= n 8)
    (char (+ n (int \A) 1))
    (char (+ n (int \A)))))

(defn position->move
  "convert string poistion lkie b13 to vector position [6,1]
  13 from bottom to top start at 1, change to 6 from top to bottom start at 0
   string pass to nil"
  [board-size str-position]
  (let [position (str/upper-case str-position)]
    (when-not (= position "PASS")
      (let [[col, row] (split-at 1 position)]
        [(- board-size (Integer/parseInt (apply str row)))
         (letter->num (first col))]))))

(defn move->position
  "convert vector poistion [12, 1] to string position b7
  row convert from top to bottom start at 0 to from bottom to top start at 1"
  [board-size move]
  (if move
    (str (num->letter (second move)) (- board-size (first move)))
    "pass"))

(defn str->color
  "convert b or B or w or W to :black or :white"
  [c]
  (if (= (str/upper-case c) "B") 1 2))

(defn parse-input
  [input]
  (let [args (str/split input #" ")]
    (if (int? (first args))
      {:id (first args) :command (second args) :args (drop 2 args)}
      {:command (first args) :args (rest args)})))

(defn response
  [id & args]
  (str "=" id " " (str/join " " args)))

(defn cmd-name
  [id & args]
  (response id "relative-go"))

(defn cmd-protocol-version
  [id & args]
  (response id "2"))

(defn cmd-version
  [id & args]
  (response id "0.1.0"))

(declare command-fs)

(defn cmd-list
  [id & args ]
  (response id (str (str/join "\r" (map (comp str name) (keys command-fs)))
                    "\r\r")))

(def game (atom nil))

(defn cmd-board-size
  [id & args ]
  (let [board-size (Integer/parseInt (first args))]
    (reset! game (go/new-game board-size))
    (response id)))

(defn cmd-clear-board
  [id & args]
  (reset! game (go/new-game (:board-size @game)))
  (response id))

(defn cmd-play
  [id & args]
  (let [color (str->color (first args))
        move (position->move (go/board-size @game) (second args))]
    (swap! game #(go/play % move color))
    (response id)))

(defn cmd-gen-move
  [id & args]
  (let [color (str->color (first args))
        move (ai/gen-move @game color)]
    (swap! game #(go/play % move color))
    (response id (move->position (go/board-size @game) move))))

(defn cmd-loadsgf
  [id & args]
  (let [filename (first args)
        data (sgf/main-branch (sgf/parse-file filename))]
    (reset! game (sgf/play data))
    (response id)))

(def command-fs {:name cmd-name
                 :protocol_version cmd-protocol-version
                 :version cmd-version
                 :list_commands cmd-list
                 :boardsize cmd-board-size
                 :clear_board cmd-clear-board
                 :play cmd-play
                 :genmove cmd-gen-move
                 :loadsgf cmd-loadsgf
                 :reg_genmove cmd-gen-move})

(defn -main
  [& args]
  (loop [input (read-line)]
    (let [command (parse-input input)
          command_name (:command command)
          f (get command-fs (keyword command_name))]
      (when (and f (not= command_name "quit"))
        (println (str (apply f (:id command) (:args command)) "\n"))
        (recur (read-line))))))