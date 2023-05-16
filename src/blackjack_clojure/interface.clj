(ns blackjack-clojure.interface)

(defn create-card-suit []
  (let [suits ["♧" "♢" "♡" "♤"]
        random-suit (rand-nth suits)]
    random-suit))

(defn convert-value [value]
  (case value
    1  "A"
    11 "J"
    12 "Q"
    13 "K"
    value))

(defn create-card-text [card-value]
  (let [converted-value (convert-value card-value)
        suit (create-card-suit)]
    (if (= card-value 10)
      (str " ________"
           "\n|        |"
           "\n|" converted-value "      |"
           "\n|   " suit "    |"
           "\n|        |"
           "\n|      " converted-value "|"
           "\n|________|")
      (str " ________"
           "\n|        |"
           "\n|" converted-value "       |"
           "\n|   " suit "    |"
           "\n|        |"
           "\n|       " converted-value "|"
           "\n|________|")
      )
   ))
