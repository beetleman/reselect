(ns reselect.core
  (:require [reagent.core :as reagent :refer [atom]]
            [dommy.utils :refer [->Array]]
            [reselect.views :refer [document-root-fn]]
            [dommy.core :as dom :refer-macros [sel sel1]]))

(enable-console-print!)

(defn logger [& args]
  (when-not (empty? args)
    (-> (first args) clj->js js/console.debug)
    (recur (rest args))))

(defn create-state [initial-state]
  (let [app-state (atom initial-state)]
    app-state))

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
        el-options (options->map (sel-options selector))
        el-selected-options (options->map (sel-selected-options selector))
        app-state (create-state
                   {:attrs {:multiple (select-multiple? selector)
                            :name (get-select-name selector)}
                    :filter-by ""
                    :options (options->map (sel-options selector))
                    :selected (set (options->map
                                    (sel-selected-options selector)))})]
    (reagent/render-component [(document-root-fn app-state)]
                              (sel1 target_id))))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
