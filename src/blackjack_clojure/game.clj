(ns blackjack-clojure.game)

; Usuário pode escolher uma carta com os valores entre 1 e 13
; A, 2, 3, 4, 5, 6, 7, 8, 9, 10, J, Q, K

(defn new-card []
  "Gera uma carta com seu valor entre 1 e 13, incluindo os extremos"
  (+ (rand-int 13) 1))

(defn player [player-name]
  (let [card1 (new-card)
        card2 (new-card)
        cards [card1 card2]
        points (points-cards cards)]
    {:player-name player-name
     :cards cards
     :points points
     }))

(println (player "João"))
(println (player "Dealer"))