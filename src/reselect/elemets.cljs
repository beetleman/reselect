(ns reselect.elements
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [clojure.string :as string]
            [clojure.set :as set]
            [dommy.core :as dom]))


(defn option [option]
  [:option {:value (:value option)}
   (:text option)])


;; `orig' hidden option
(defn select [attrs options selected]
  [:select {:multiple (:multiple @attrs)
            :style {:display "none"}
            :value (mapv :value @selected)
            :name (:name @attrs)}
   (for [o @options]
     ^{:key o} [option o])])


;; custom-option
(defn custom-option-on-click-fn [option state]
  (fn [_]
    (swap! state update :selected #(conj % option))))

(defn custom-option [option state]
  (let [text (:text option)
        value (:value option)]
    [:div.custom-option
     {:data-value value
      :on-click (custom-option-on-click-fn option state)}
     text]))

(defn custom-select [options-filtred state]
  [:div.custom-select
   (for [o @options-filtred]
     ^{:key o} [custom-option o state])])


;; custom-option-selected
(defn custom-option-selected-on-click-fn [option state]
  (fn [_]
    (swap! state update :selected #(set/difference % #{option}))))

(defn custom-option-selected [option state]
  (let [text (:text option)
        value (:value option)]
    [:div.custom-option-selected
     {:data-value value
      :on-click (custom-option-selected-on-click-fn option state)}
     text]))

(defn custom-select-selected [selected state]
  [:div.custom-select-selected
   (for [o @selected]
     ^{:key o} [custom-option-selected o state])])


;; filter input
(defn input-on-change-fn [state]
  (fn [e]
    (swap! state assoc :filter-by (dom/value (.-target e)))))

(defn input [state]
  [:input {:on-change (input-on-change-fn state)}])


;; document-root
(defn document-root-fn [state]
  (let [options (reaction (get @state :options []))
        selected (reaction (get @state :selected #{}))
        filter-by (reaction (get @state :filter-by ""))
        options-filtred (reaction
                         (doall (filter
                                 (fn [o]
                                   (and
                                    (string/includes? (:text o)
                                                      @filter-by)
                                    (not (contains? @selected o))))
                          @options)))
        attrs (reaction (:attrs @state))]
    (fn [state]
      [:div.reselect
       [select attrs options selected]
       [:div.input-wrapper
        [input state]
        [custom-select options-filtred state]]
       [custom-select-selected selected state]])))
