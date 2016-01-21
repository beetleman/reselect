(ns reselect.core
  (:require [reagent.core :as reagent :refer [atom]]
            [dommy.utils :refer [->Array]]
            [dommy.core :as dom :refer-macros [sel sel1]]))

(enable-console-print!)

(defn logger [& args]
  (when-not (empty? args)
    (-> (first args) clj->js js/console.debug)
    (recur (rest args))))


(defonce app-state (atom {:text "Hello world!"}))
(add-watch app-state :logger #(logger %4))

(defn hidden-options [options multiple]
  [:div
   [:h1 (:text @app-state)]
   (into [:select {:multiple multiple
                   :name "todo!"
                   :style {:display "none"}}]
         (map (fn [x] [:option {:value (:value x)}
                       (:text x)]) options))])

(defn options->map [options]
  (map
   (fn [o]
     {:value (dom/value o) :text (dom/text o)})
   options))

(defn sel-selected-options [target]
  (->Array (.-selectedOptions (dom/sel1 target))))

(defn select-multiple? [target]
  (-> (dom/sel1 target) (dom/attr :multiple) string?))

(defn get-select-name [target]
  (-> (dom/sel1 target) (dom/attr :name)))

(defn sel-options [target]
  (dom/sel [target "option"]))

(defn ^:export create [target_id]
  (let [selector [target_id :select]
        options (options->map (sel-options selector))
        multiple (select-multiple? selector)
        name (get-select-name selector)
        selected-options (options->map (sel-selected-options selector))]
    (logger multiple)
    (reagent/render-component [hidden-options options multiple]
                              (sel1 target_id))))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
