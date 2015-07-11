(ns maye.core
  (:require [tailrecursion.priority-map :as pm]))

(def world {:paused? false
            :systems (pm/priority-map)
            :entities {}
            :frame-count 0})

(defn frame-period
  "Returns a :run-when predicate for running systems on a given frame-period"
  [period]
  (fn [world] (zero? (mod (:frame-count world) period))))

(defn assoc-components [entity components]
  (reduce #(assoc %1 (:name %2) %2) entity components))

(defn system [& {:keys [id name update-fn matcher-fn run-when entities]}]
  {:id (or id (random-uuid))
   :name name
   :update-fn (or update-fn identity)
   :matcher-fn (or matcher-fn (constantly true))
   :run-when (or run-when (constantly true))})

(defn entity
  ([] (entity []))
  ([components] (assoc-components {:id (random-uuid)} components)))

(defn has-components? [entity components]
  (every? #(contains? entity %) components))

(defn get-component [entity component]
  (get entity component))

(defn assoc-component [entity component]
  (assoc entity (:name component) component))

(defn remove-component [entity component]
  (dissoc entity (:name component)))

(defn remove-components [entity components]
  (reduce #(dissoc %1 (:name %2)) entity components))

(defn get-entity [world id]
  (get-in world [:entities id]))

(defn assoc-entity [world entity]
  (update world :entities #(assoc % (:id entity) entity)))

(defn add-systems [world systems]
  (update world :systems #(reduce (fn [current [system priority]]
                                    (assoc current system priority)) % systems)))

(defn assoc-entities [world entities]
  (update world :entities (fn [current]
                            (reduce #(assoc %1 (:id %2) %2) current entities))))

(defn frame-counter [world]
  (update world :frame-count inc))

(defn call-systems
  "Calls each of the registered systems in priority order"
  [world]
  (reduce (fn [world system]
            (let [entities (vals (:entities world))
                  {:keys [matcher-fn update-fn run-when]} system]
              (if (run-when world)
                (update-fn world (filter matcher-fn entities))
                world)))
          world
          (keys (:systems world))))

(defn update-world [world]
  (-> world
      (frame-counter)
      (call-systems)))
