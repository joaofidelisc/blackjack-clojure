(ns blackjack-clojure.game)

; Usuário pode escolher uma carta com os valores entre 1 e 13
; A, 2, 3, 4, 5, 6, 7, 8, 9, 10, J, Q, K

(defn new-card []
  "Gera uma carta com seu valor entre 1 e 13, incluindo os extremos"
  (+ (rand-int 13) 1))

;Calcula os pontos de acordo com as cartas
; J, Q, K são iguais a 10 e não 11, 12, 13;
; [A 10] = 11 ou 21 = 21;
; [A 5 7] = 1+5+7 (13) ou 11+5+7 (23);
; Vamos usar o A = 11, porém se passar de 21, ele passa a valer 1.
; Sintax reduce (reduce op vi [v1 v2... vn]);
(defn JQK->10 [card]
  (if (> card 10) 10 card))

(defn A->11 [card]
  (if (= card 1) 11 card))

(defn points-cards [cards]
  (let [cards-without-JQK (map JQK->10 cards)
        cards-with-A11 (map A->11 cards-without-JQK)
        points-with-A-1 (reduce + cards-without-JQK)
        points-with-A-11 (reduce + cards-with-A11)]
    (if (> points-with-A-11 21) points-with-A-1 points-with-A-11)))


(defn player [player-name]
  (let [card1 (new-card)
        card2 (new-card)
        cards [card1 card2]
        points (points-cards cards)]
    {:player-name player-name
     :cards       cards
     :points      points
     }))

(println (player "João"))
(println (player "Dealer"))