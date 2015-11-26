(ns maye.state
  (:require [schema.core :as s :include-macros true]
            [tailrecursion.priority-map :as pm]
            [maye.util :as util]))

(s/defrecord State [frame :- s/Int
                    entities :- {}
                    systems :- {}
                    paused? :- s/Bool
                    event-queue :- IAtom])

(def new-priority-map
  "Create a priority-map ordered by the :priority key"
  (pm/priority-map-by
    #(compare (:priority %1)
              (:priority %2))))

(defn dispatch-event!
  "Add a msg to the queue specified by `id`,
  creating the queue if it doesn't exist"
  [{:keys [event-queue]} id msg]
  (swap! event-queue update id (fnil conj []) msg))

(defn new-state [& {:keys [frame entities systems]}]
  (map->State
    {:paused?     false
     :systems     (or systems new-priority-map)
     :entities    (or entities {})
     :event-queue (atom {})
     :frame       (or frame 0)}))

(defn inc-frame [state]
  (update state :frame inc))


(defn get-entity [state id]
  (get-in state [:entities id]))

(defn add-entities-to-systems
  "Call add-entity for each matching entity for each system"
  [{:keys [systems] :as state} entities]
  (for [system (vals systems)]
    (let [{:keys [entity-filters add-entity]} system
          entities-to-add (filter (apply some-fn entity-filters) entities)]
      (reduce add-entity system entities-to-add))))

(defn add-systems
  "Assoc systems into the state
  and add any entities from the state into the systems"
  [state systems]
  (let [prioritized-systems (map (fn [[system priority]]
                                   (assoc system :priority priority))
                                 systems)]
    (as-> state current
          (util/assoc-coll-by-id current :systems prioritized-systems))))
          ;(util/assoc-coll-by-id current :systems (add-entities-to-systems current (vals (:entities current)))))))

(defn add-entity [state entity]
  "Assoc entity into state and add it to any matching systems"
  (as-> state current
        (util/assoc-by-id current :entities entity)))
        ;(util/assoc-coll-by-id current :systems (add-entities-to-systems current [entity]))))

(defn add-entities [state entities]
  (as-> state current
        (util/assoc-coll-by-id current :entities entities)))
        ;(util/assoc-coll-by-id current :systems (add-entities-to-systems current entities))))

(defn update-systems
  "Call each of the registered systems in priority order
  and assoc the returned entities into the state"
  [{:keys [systems] :as state}]
  (reduce (fn [{:keys [entities] :as state} system]
            (let [{:keys [update-fn entity-filters update-filters entity-ids]} system
                  valid-entities (filter (apply some-fn entity-filters) (vals entities))]
                  ;valid-entities (vals (select-keys entities entity-ids))]
              (if ((apply every-pred update-filters) state)
                (util/assoc-coll-by-id state :entities (update-fn state valid-entities))
                state)))
          state
          (vals systems)))

(defn update-state
  "Move the gamestate forward one frame by calling all systems"
  [state]
  (-> state
      inc-frame
      update-systems))
