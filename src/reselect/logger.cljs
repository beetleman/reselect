(ns reselect.logger)

(defn log [& args]
  (when-not (empty? args)
    (-> (first args) clj->js js/console.debug)
    (recur (rest args))))
