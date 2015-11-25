(ns maye.util
  (:require [schema.core :as s :include-macros true]
            [maye.core :as core]))


(defn assoc-by-id [state key item]
  (update state key assoc (:id item) item))

(defn assoc-coll-by-id [state key coll]
  (update state key (fn [current]
                      (reduce #(assoc %1 (:id %2) %2) current coll))))
