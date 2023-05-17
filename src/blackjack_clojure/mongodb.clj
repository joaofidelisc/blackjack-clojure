(ns blackjack-clojure.mongodb
  (:require  [monger.core :as mg]
             [monger.collection :as mc]
             [monger.operators :refer :all])
  (:import org.bson.types.ObjectId))


(defn establish-connection []
  (let [conn (mg/connect)
        db (mg/get-db conn "blackjack")]
    [conn db]))

(defn won-the-last-round [player [conn db]]
  (let [player-name (:player-name player)
        results (mc/find-one db "gameresults" {:player-name player-name})
        won-last-round (get results "won-the-last-round")]
    won-last-round))


(defn update-won-last-round [player bool [conn db]]
  (let [coll "gameresults"
        player-name (:player-name player)]
    (mc/update db coll {:player-name player-name}
               {"$set" {:won-the-last-round bool}})))


(defn update-wins-from-database [player won-current-match [conn db]]
  (let [coll "gameresults"
        player-name (:player-name player)]
    (if won-current-match (do
                            (mc/update db coll {:player-name player-name}
                         {"$inc" {:number-of-wins 1}}))
                          (update-won-last-round player false [conn db]))
    (if (won-the-last-round player [conn db])
      (do
        (mc/update db coll {:player-name player-name}
                   {"$inc" {:consecutive-wins 1}})
        (update-won-last-round player true [conn db]))
      (do
        (if won-current-match (do
                                (mc/update db coll {:player-name player-name}
                                           {"$set" {:consecutive-wins 1}})
                                (update-won-last-round player true [conn db]))
                              (do
                                (mc/update db coll {:player-name player-name}
                                           {"$set" {:consecutive-wins 0}})
                                (update-won-last-round player false [conn db]))
                              )
        ))))


(defn get-user-exists [player [conn db]]
  (let [player-name (:player-name player)
        results (mc/find-one db "gameresults" {:player-name player-name})]
    (if (nil? results) false true)
    ))


(defn insert-data-database [player [conn db]]
  (let [player-name (:player-name player)]
    (mc/insert db "gameresults" {
                                 :_id (ObjectId.)
                                 :player-name player-name
                                 :number-of-wins 0
                                 :consecutive-wins 0
                                 :won-the-last-round false})))


(defn get-data-from-database [player [conn db]]
  (if (get-user-exists player [conn db])
    (let [player-name (:player-name player)
          results (mc/find-one db "gameresults" {:player-name player-name})
          player-name (get results "player-name")
          wins (-> results (.getLong "number-of-wins"))]
      (println (str "Nome: " player-name ";"))
      (println (str "Vitórias: " wins))
      )))