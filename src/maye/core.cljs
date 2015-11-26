(ns maye.core
  (:require [maye.component :as component]
            [maye.entity :as entity]
            [maye.system :as system]
            [maye.state :as state]))

;;-------Component--------
(def new-component component/new-component)

;;-------Entity----------
(def new-entity entity/new-entity)
(def assoc-components entity/assoc-components)
(def assoc-component entity/assoc-component)
(def dissoc-components entity/dissoc-components)
(def dissoc-component entity/dissoc-component)
(def contains-components? entity/contains-components?)

;;------System-----------
(def new-system system/new-system)
(def frame-period system/frame-period)

;;------State---------
(def new-state state/new-state)
(def add-systems state/add-systems)
(def add-entities state/add-entities)
(def update-state state/update-state)
