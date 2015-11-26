(ns maye.core
  (:require [maye.component :as component]
            [maye.entity :as entity]
            [maye.system :as system]
            [maye.state :as state]))

;;-------Component--------
(defn new-component component/new-component)

;;-------Entity----------
(defn new-entity entity/new-entity)
(defn assoc-components entity/assoc-components)
(defn assoc-component entity/assoc-component)
(defn dissoc-components entity/dissoc-components)
(defn dissoc-component entity/dissoc-component)
(defn contains-components? entity/contains-components?)

;;------System-----------
(defn new-system system/new-system)
(defn frame-period system/frame-period)

;;------State---------
(defn new-state state/new-state)
(defn add-systems state/add-systems)
(defn add-entities state/add-entities)
(defn update-state state/update-state)
