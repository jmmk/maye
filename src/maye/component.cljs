(ns maye.component
  (:require [schema.core :as s :include-macros true]))

(s/defrecord Component [name :- s/Keyword])

(defn new-component [props]
  (map->Component props))

