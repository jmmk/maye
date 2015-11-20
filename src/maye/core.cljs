(ns maye.core
  (:require [tailrecursion.priority-map :as pm]
            [maye.util :as util]))


;;----------Entities--------------

(defn assoc-components [entity components]
  (reduce #(assoc %1 (:name %2) %2) entity components))

(defn new-entity
  ([] (new-entity []))
  ([components] (assoc-components {:id (random-uuid)} components)))

(defn contains-components? [entity components]
  (every? #(contains? entity %) components))

(defn assoc-component [entity component]
  (assoc entity (:name component) component))

(defn dissoc-component [entity component]
  (dissoc entity (:name component)))

(defn dissoc-components [entity components]
  (reduce #(dissoc %1 (:name %2)) entity components))


;;------------Systems--------------

(defn update-entities
  "Return updated entities to be assoc'd into the game state"
  [system entities]
  entities)

(defn add-entity-to-system
  "Return updated system with any new entity ids conj'd"
  [system entity]
  (update system :entities conj (:id entity)))

(defn new-system
  "Create a new System with given name and options"
  [name & {:keys [id update-fn update-filter add-entity entity-filter entity-ids]}]
  {:name          name
   :id            (or id (random-uuid))
   :update-fn     (or update-fn update-entities)
   :update-filter (or update-filter (constantly true))
   :add-entity    (or add-entity add-entity-to-system)
   :entity-filter (or entity-filter (constantly true))
   :entity-ids    (set (or entity-ids []))})


;;-----------Game State--------------

(def priority-map (pm/priority-map-by
                    #(compare (:priority %1)
                              (:priority %2))))

(defn new-state [& {:keys [frame entities systems]}]
  {:paused?  false
   :systems  (or systems priority-map)
   :entities (or entities {})
   :frame    (or frame 0)})

(defn inc-frame [state]
  (update state :frame inc))

(defn update-systems
  "Call each of the registered systems in priority order
  and assoc the returned entities into the state"
  [state]
  (reduce (fn [{:keys [entities] :as state} system]
            (let [{:keys [update-fn run-when-fn entity-ids]} system]
              (if (run-when-fn state)
                (update state :entities merge (update-fn system (select-keys entities entity-ids)))
                state)))
          state
          (:systems state)))

(defn update-state
  "Move the gamestate forward one frame by calling all systems"
  [state]
  (-> state
      inc-frame
      update-systems))

(defn get-entity [state id]
  (get-in state [:entities id]))

(defn add-entities-to-systems
  "Call add-entity for each matching entity for each system"
  [{:keys [systems]} entities]
  (for [system systems]
    (let [{:keys [entity-filter add-entity]} system
          entities-to-add (filter entity-filter entities)]
      (reduce add-entity system entities-to-add))))

(defn add-systems
  "Assoc systems into the state
  and add any entities from the state into the systems"
  [state systems]
  (as-> state current
        (util/assoc-coll-by-id current :systems systems)
        (util/assoc-coll-by-id current :systems (add-entities-to-systems current (:entities current)))))

(defn add-entity [state entity]
  "Assoc entity into state and add it to any matching systems"
  (as-> state current
        (util/assoc-by-id current :entities entity)
        (util/assoc-coll-by-id current :systems (add-entities-to-systems current [entity]))))

(defn add-entities [state entities]
  (as-> state current
        (util/assoc-coll-by-id current :entities entities)
        (util/assoc-coll-by-id current :systems (add-entities-to-systems current entities))))
