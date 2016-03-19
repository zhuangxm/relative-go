(ns relative-go.sgf.core-test
  (:use [midje.sweet])
  (:require [relative-go.sgf.core :refer :all]))


(facts "parse property"
       (parse-property-values "[4] [5]\n[6]GM") => ["4" "5" "6" "GM"]
       (parse-properties "FF[4]GM[1][2]SZ[19]\n ;hello")
       => [["FF", '("4")] ["GM", '("1" "2")] ["SZ", '("19")] ";hello"]
       (parse-properties "FF[4]GM[1][2]SZ[19]\n (;hello")
       => [["FF", '("4")] ["GM", '("1" "2")] ["SZ", '("19")] "(;hello"])

(facts "parse node"
       (parse-node ";FF[4]GM[1][2]SZ[19]\n (;hello[])")
       => [:node {"FF", '("4") "GM", '("1" "2") "SZ", '("19")} "(;hello[])"]
       (parse-nodes-or-trees ";FF[4]GM[1][2]SZ[19]\n (;hello[])")
       => '[[:node {"FF", ("4") "GM", ("1" "2") "SZ", ("19")}] ,(:tree (:node {"hello" ("")})) ""]
       (parse-nodes-or-trees "(;FF[4]GM[1][2]SZ[19]\n ;hello[])")
       => '[[:tree [:node {"FF", ("4") "GM", ("1" "2") "SZ", ("19")}] [:node {"hello", ("")}]] ""])

(facts "test parse sgf string"
       (parse "(;FF[4]GM[1]SZ[19]\n GN[Copyright goproblems.com]\n PB[Black]\n HA[0]\n PW[White]\n KM[5.5]\n DT[1999-07-21]\n TM[1800]\n RU[Japanese]\n ;AW[bb][cb][cc][cd][de][df][cg][ch][dh][ai][bi][ci]\n AB[ba][ab][ac][bc][bd][be][cf][bg][bh]\n C[Black to play and live.]\n (;B[af];W[ah]\n (;B[ce];W[ag]C[only one eye this way])\n (;B[ag];W[ce]))\n (;B[ah];W[af]\n (;B[ae];W[bf];B[ag];W[bf]\n (;B[af];W[ce]C[oops! you can't take this stone])\n (;B[ce];W[af];B[bg]C[RIGHT black plays under the stones and lives]))\n (;B[bf];W[ae]))\n (;B[ae];W[ag]))")
       => truthy)

(facts "test main brach"
       (let [sgf (parse "(;FF[4]GM[1]SZ[19]\n GN[Copyright goproblems.com]\n PB[Black]\n HA[0]\n PW[White]\n KM[5.5]\n DT[1999-07-21]\n TM[1800]\n RU[Japanese]\n ;AW[bb][cb][cc][cd][de][df][cg][ch][dh][ai][bi][ci]\n AB[ba][ab][ac][bc][bd][be][cf][bg][bh]\n C[Black to play and live.]\n (;B[af];W[ah]\n (;B[ce];W[ag]C[only one eye this way])\n (;B[ag];W[ce]))\n (;B[ah];W[af]\n (;B[ae];W[bf];B[ag];W[bf]\n (;B[af];W[ce]C[oops! you can't take this stone])\n (;B[ce];W[af];B[bg]C[RIGHT black plays under the stones and lives]))\n (;B[bf];W[ae]))\n (;B[ae];W[ag]))")]
         (main-branch sgf)
         => '({"SZ" ("19")} {"AB" ("ba" "ab" "ac" "bc" "bd" "be" "cf" "bg" "bh"), "AW" ("bb" "cb" "cc" "cd" "de" "df" "cg" "ch" "dh" "ai" "bi" "ci")} {"B" ("af")} {"W" ("ah")} {"B" ("ce")} {"W" ("ag")})
         (play (main-branch sgf)) => truthy))
