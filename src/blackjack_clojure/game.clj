(ns blackjack-clojure.game
  (:require [blackjack-clojure.interface :as interface]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer :all])
  (:import org.bson.types.ObjectId))




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

(defn update-wins-from-database [player]
  (let [conn (mg/connect)
        db   (mg/get-db conn "blackjack")
        coll "gameresults"
        player-name (:player-name player)]
    (mc/update db coll {:player-name player-name}
               {"$inc" {:number-of-wins 1}})
    (mc/update db coll {:player-name player-name}
               {"$set" {:consecutive-wins 1}})))

(defn end-game [player dealer]
  (let [player-points (:points player)
        dealer-points (:points dealer)
        player-name (:player-name player)
        dealer-name (:player-name dealer)
        message (cond
                  (and (> player-points 21) (> dealer-points)) "Ambos perderam\n"
                  (= player-points dealer-points) "Empate\n"
                  (> player-points 21) (str dealer-name " ganhou o jogo\n")
                  (> dealer-points 21) (do
                                         (update-wins-from-database player)
                                         (str player-name " ganhou o jogo\n"))
                  (> player-points dealer-points) (do
                                                    (update-wins-from-database player)
                                                    (str player-name " ganhou o jogo\n"))
                  (> dealer-points player-points) (str dealer-name " ganhou o jogo\n"))]
    (display-cards player)
    (display-cards dealer)
    (print message)))


(defn user-exists [player]
  (let [conn (mg/connect)
        db (mg/get-db conn "blackjack")
        player-name (:player-name player)
        results (mc/find-one db "gameresults" {:player-name player-name})]
    (if (nil? results) false true)
    ))


(defn insert-data-database [player]
  (let [conn (mg/connect)
        db   (mg/get-db conn "blackjack")
        player-name (:player-name player)]
    (mc/insert db "gameresults" {
                                 :_id (ObjectId.)
                                 :player-name player-name
                                 :number-of-wins 0
                                 :consecutive-wins 0})))


(defn show-data-from-database [player]
  (if (user-exists player)
    (let [conn (mg/connect)
          db (mg/get-db conn "blackjack")
          player-name (:player-name player)
          results (mc/find-one db "gameresults" {:player-name player-name})
          player-name (get results "player-name")
          wins (-> results (.getLong "number-of-wins"))]
      (println (str "Nome: " player-name ";"))
      (println (str "Vitórias: " wins))
      )))


(defn start-game []
  (println "Seja bem-vindo(a) ao Blackjack!\nPor favor, insira o seu nome para iniciar um novo jogo ou carregar as informações de um jogo anterior.")
  (let [player-name (read-line)]
    (def player-user (player player-name))
    (def player-opponent (player "Dealer"))
    (if (not (user-exists player-user))
      (insert-data-database player-user)))
  (println "--------------------------------")
  (display-cards player-user)
  (display-cards player-opponent)
  (def player-after-game (game player-user player-decision-continue))
  (def partial-dealer-decision-continue (partial opponent-decision-continue (:points player-after-game)))
  (def dealer-after-game (game player-opponent partial-dealer-decision-continue))
  (end-game player-after-game dealer-after-game))

;(update-wins-from-database player-user)
;(insert-data-database player-user)
;(show-data-from-database player-user)

(start-game)


