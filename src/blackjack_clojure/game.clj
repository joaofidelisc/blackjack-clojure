(ns blackjack-clojure.game
  (:require [blackjack-clojure.interface :as interface]
            [blackjack-clojure.mongodb :as mongodb]
            [clojure.string :as str]))


(defn new-card []
  (+ (rand-int 13) 1))


(defn convertJQKto10 [card]
  (if (> card 10) 10 card))


(defn convertAto11 [card]
  (if (= card 1) 11 card))


(defn points-cards [cards]
  (let [cards-without-JQK (map convertJQKto10 cards)
        cards-with-A11 (map convertAto11 cards-without-JQK)
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
     :points      points}))


(defn display-cards [player]
  (println (str "Aqui estão suas cartas, " (:player-name player)))
  (let [card-texts (map interface/create-card-text )])
  (doseq [card (:cards player)]
    (println (interface/create-card-text card)))
  (println (str "\nPontos:" (:points player)))
  (println "--------------------------------"))


(defn more-card [player]
  (let [card (new-card)
        cards (conj (:cards player) card)
        new-player (assoc player :cards cards)
        points (points-cards cards)]
    (assoc new-player :points points)))


(defn opponent-decision-continue [player-points opponent]
  (let [opponent-points (:points opponent)]
    (if (> player-points 21) false (<= opponent-points player-points))))


(defn player-decision-continue [player]
  (println (:player-name player) ":mais carta?")
  (= (read-line) "sim"))


(defn game [player fn-decision-continue]
  (let [player-points (:points player)]
    (if (fn-decision-continue player)
      (let [player-with-more-cards (more-card player)]
        (display-cards player-with-more-cards)
        (recur player-with-more-cards fn-decision-continue))
      player)))


(defn end-game [player dealer [conn db]]
  (let [player-points (:points player)
        dealer-points (:points dealer)
        player-name (:player-name player)
        dealer-name (:player-name dealer)
        message (cond
                  (and (> player-points 21) (> dealer-points)) "Ambos perderam\n"
                  (= player-points dealer-points) "Empate\n"
                  (> player-points 21) (str dealer-name " ganhou o jogo\n")
                  (> dealer-points 21) (str player-name " ganhou o jogo\n")
                  (> player-points dealer-points) (str player-name " ganhou o jogo\n")
                  (> dealer-points player-points) (str dealer-name " ganhou o jogo\n"))]

    (display-cards dealer)
    (display-cards player)
    (print message)
    (if (str/includes? message player-name)
      (mongodb/update-wins-from-database player true [conn db])
      (mongodb/update-wins-from-database player false [conn db])
      )))


(defn start-game []
  (println "Seja bem-vindo(a) ao Blackjack!\nPor favor, insira o seu nome para iniciar um novo jogo ou carregar as informações de um jogo anterior.")
  (let [player-name (read-line)
        [conn db] (mongodb/establish-connection)]
    (def player-user (player player-name))
    (def player-opponent (player "Dealer"))
    (if (not (mongodb/get-user-exists player-user [conn db]))
      (mongodb/insert-data-database player-user [conn db]))
    (println "--------------------------------")
    (display-cards player-opponent)
    (display-cards player-user)
    (def player-after-game (game player-user player-decision-continue))
    (def partial-dealer-decision-continue (partial opponent-decision-continue (:points player-after-game)))
    (def dealer-after-game (game player-opponent partial-dealer-decision-continue))
    (end-game player-after-game dealer-after-game [conn db])))


(start-game)


