(ns maye.util)

(defn frame-period
  "Returns an :update-filter predicate for running systems
  on a given frame-period"
  [period]
  (fn [world] (zero? (mod (:frame world) period))))

(defn assoc-by-id [state key item]
  (update state key assoc (:id item) item))

(defn assoc-coll-by-id [state key coll]
  (update state key (fn [current]
                      (reduce #(assoc %1 (:id %2) %2) current coll))))
