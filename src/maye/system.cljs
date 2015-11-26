(ns maye.system
  (:require [schema.core :as s :include-macros true]
            [maye.state :as state :refer [State]]
            [maye.entity :as entity :refer [Entity]]))

(s/defrecord System [id :- s/Uuid
                     name :- s/Keyword
                     update-fn :- (s/=>* [Entity] [State [Entity]])
                     update-filters :- [(s/=>* s/Bool [State])]
                     add-entity :- (s/=>* System [System Entity])
                     entity-filters :- [(s/=>* s/Bool [Entity])]
                     entity-ids :- #{s/Uuid}])

(defn frame-period
  "Returns an update-filter predicate for running systems
  on a given frame-period"
  [period]
  (s/defn update-filter :- s/Bool
    [state :- State]
    (zero? (mod (:frame state) period))))

(s/defn update-entities :- [Entity]
  "State -> [Entity] -> [Entity]
  Return updated entities to be assoc'd into the game state"
  [_ :- State
   entities :- [Entity]]
  entities)

(s/defn add-entity-to-system :- System
  "System -> Entity -> System
  Return updated system with any new entity ids conj'd"
  [system :- System
   entity :- Entity]
  (update system :entities (fnil conj #{}) (:id entity)))

(declare validate-system)

(defn new-system
  "Create a new System with given name and options"
  [& {:keys [name id update-fn update-filters add-entity entity-filters entity-ids]}]
  (validate-system
    (map->System
      {:name           name
       :id             (or id (random-uuid))
       :update-fn      (or update-fn update-entities)
       :update-filters (or (not-empty update-filters)
                           [(constantly true)])
       :add-entity     (or add-entity add-entity-to-system)
       :entity-filters (or (not-empty entity-filters)
                           [(constantly true)])
       :entity-ids     (set (or entity-ids []))})))

(def test-entity (entity/new-entity))
(def test-state (state/new-state))

(defn validate-update-fn
  "Ensure update-fn is of type:
  State -> [Entity] -> [Entity]"
  [update-fn]
  (s/with-fn-validation
    (update-fn test-state [test-entity])))

(defn validate-update-filter
  "Ensure update filter is of type:
  State -> Bool"
  [update-filter]
  (s/with-fn-validation
    (update-filter test-state)))

(defn validate-entity-filter
  "Ensure entity-filter is of type:
  Entity -> Bool"
  [entity-filter]
  (s/with-fn-validation
    (entity-filter test-entity)))

(defn validate-add-entity
  "Ensure add-entity is of type:
  System -> Entity -> System"
  [add-entity]
  (s/with-fn-validation
    (add-entity (new-system) test-entity)))

(defn validate-system
  [{:keys [update-fn update-filters add-entity entity-filters]
    :as   system}]
  (map validate-update-fn update-fn)
  (map validate-update-filter update-filters)
  (map validate-entity-filter entity-filters)
  (map validate-add-entity add-entity)
  (s/validate System system))


