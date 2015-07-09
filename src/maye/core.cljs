(ns maye.core
  (:require [tailrecursion.priority-map :as pm]))

(def world {:paused? false
            :systems (pm/priority-map)
            :entities {}
            :frame-count 0})

(defn frame-period [period]
  "Return a predicate for running systems on a given frame-period"
  (fn [world] (= (mod (:frame-count world) period) 0)))

(defn id-map [id]
  "Takes a map key like :id and returns a transducer for converting
  maps of the format {:id id :keys :values} to pairs of [id map]"

  (map (fn [entry] [(get entry id) entry])))

(defn assoc-components [entity components]
  (into entity (id-map :name) components))

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
  (every? (partial contains? entity) components))

(defn get-component [entity component]
  (get entity component))

(defn assoc-component [entity component]
  (assoc entity (:name component) component))

(defn remove-component [entity component]
  (dissoc entity (:name component)))

(defn remove-components [entity components]
  (persistent! (reduce #(dissoc! %1 (:name %2))
                       (transient entity)
                       components)))

(defn get-entity [world id]
  (get-in world [:entities id]))

(defn get-entities [world]
    (vals (:entities world)))

(defn get-entities-with-component [world component]
  (let [entities (get-entities world)]
    (filter #(contains? % component) entities)))

(defn get-entities-with-components [world & components]
  (let [entities (get-entities world)]
    (filter #(has-components? % components) entities)))

(defn assoc-entity [world entity]
  (update world :entities #(assoc % (:id entity) entity)))

(defn add-systems [world systems]
  (update world :systems #(into % systems)))

(defn assoc-entities [world entities]
  (update world :entities #(into % (id-map :id) entities)))

(defn call-systems [world]
  (reduce (fn [world system]
            (let [entities (vals (:entities world))
                  {:keys [matcher-fn update-fn run-when]} system]
              (if (run-when world)
                (update-fn world (filter matcher-fn entities))
                world)))
          world
          (keys (:systems world))))
