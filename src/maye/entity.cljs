(ns maye.entity
  (:require [schema.core :as s :include-macros true]))

(s/defrecord Entity [id :- s/Uuid])

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


