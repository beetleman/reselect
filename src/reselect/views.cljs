(ns reselect.views
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent]
            [clojure.string :as string]
            [reselect.logger :as logger]
            [clojure.set :as set]
            [dommy.core :as dom]))


;; utils
(defn visable-option? [o filter-by selected]
  (and
   (string/includes? (:text o)
                     filter-by)
   (not (contains? selected o))))

(defn filter-options [options filter-by selected]
  (filterv #(visable-option? % filter-by selected) options))


;; `orig' hidden option
(defn option [option]
  [:option {:value (:value option)}
   (:text option)])

(defn select [attrs options selected]
  [:select {:multiple (:multiple attrs)
            :style {:display "none"}
            :value (mapv :value @selected)
            :readOnly true
            :name (:name attrs)}
   (for [o options]
     ^{:key (:value o)} [option o])])


;; custom-option
(defn custom-option-on-click-fn [option state]
  (fn [_]
    (swap! state update :selected #(conj % option))))

(defn custom-option-on-mouseOver-fn [index state]
  (fn [_]
    (swap! state assoc :hover-index index)))

(defn custom-option [option hovered state index]
  (let [text (:text option)
        value (:value option)]
    [:div
     {:class (str
              "custom-option "
              (when hovered "hover"))
      :on-mouseOver (custom-option-on-mouseOver-fn index state)
      :on-click (custom-option-on-click-fn option state)}
     text]))

(defn custom-select-render [options-filtred hover-index state]
  (let [hi @hover-index]
    [:div.custom-select
     (map-indexed
      (fn [i o] ^{:key (:value o)} [custom-option o (= i hi) state i])
      @options-filtred)]))

(defn custom-select-scroller [this]
  (let [[_ options-filtred hover-index state] (reagent/argv this)
        node (reagent/dom-node this)
        hovered-node (aget node "childNodes" @hover-index)
        hovered-height (.-offsetHeight hovered-node)
        hovered-top (.-offsetTop hovered-node)
        min-top (.-scrollTop node)
        max-top (+ (.-offsetHeight node) min-top (- 0 hovered-height))]
    (cond
      (< hovered-top min-top)
      (aset node "scrollTop" hovered-top)
      (> hovered-top max-top)
      (aset node "scrollTop" (+ min-top hovered-height)))))

(def custom-select
  (with-meta custom-select-render
    {:component-did-mount custom-select-scroller
     :component-did-update custom-select-scroller}))


;; custom-option-selected
(defn custom-option-selected-on-click-fn [option state]
  (fn [_]
    (swap! state update :selected #(set/difference % #{option}))))

(defn custom-option-selected [option state]
  (let [text (:text option)
        value (:value option)]
    [:div.custom-option-selected
     {:on-click (custom-option-selected-on-click-fn option state)}
     text]))

(defn custom-select-selected [selected state]
  [:div.custom-select-selected
   (for [o @selected]
     ^{:key (:value o)} [custom-option-selected o state])])


;; filter input
(defn input-on-change-fn [state]
  (fn [e]
    (swap! state assoc :filter-by (dom/value (.-target e)))))

(defn is-enter-ev? [e]
  (= 13 (.-keyCode e)))

(defn is-up-ev? [e]
  (= 38 (.-keyCode e)))

(defn is-down-ev? [e]
  (= 40 (.-keyCode e)))

(defn keyboard-ev->key [e]
  (cond
    (is-enter-ev? e) :enter
    (is-up-ev? e) :up
    (is-down-ev? e) :down
    :default :unknown))

(defn next-item [max-index state]
  (swap! state update :hover-index
         (fn [v] (if (< v @max-index)
                   (inc v)
                   @max-index))))

(defn previous-item [state]
  (swap! state update :hover-index
         (fn [v] (if (< 0 v)
                   (dec v)
                   v))))

(defn select-item [filtred-options hover-index state]
  (swap! state update :selected
         #(conj % (nth @filtred-options @hover-index))))

(defn input-on-key-down-fn [filtred-options hover-index max-index state]
  (fn [e]
    (cond
      (is-enter-ev? e) (select-item filtred-options hover-index state)
      (is-up-ev? e) (previous-item state)
      (is-down-ev? e) (next-item  max-index state))))


(defn input [filtred-options hover-index max-index state]
  [:input {:on-change (input-on-change-fn state)
           :on-key-down (input-on-key-down-fn
                         filtred-options
                         hover-index
                         max-index
                         state)}])

;; document-root
(defn document-root-fn [state]
  (let [options (:options @state)
        attrs (:attrs @state)
        selected (reaction (:selected @state))
        filter-by (reaction (:filter-by @state))
        filtred-options (reaction (filter-options options @filter-by @selected))
        max-index (reaction (dec (count @filtred-options)))
        hover-index (reaction (let [i (:hover-index @state)]
                                (if (> i @max-index)
                                  @max-index
                                  i)))]
    (fn []
      [:div.reselect
       [select attrs options selected]
       [:div.input-wrapper
        [input filtred-options hover-index max-index state]
        [custom-select filtred-options hover-index state]]
       [custom-select-selected selected state]])))
