(ns app.main
  (:require
   [goog.dom :as gdom]
   [cognitect.transit :as t]
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [cljs-time.format :as tf]
   [cljs-time.core :as tc :refer [now]]
   [clojure.string :as s]
   [goog.string :as gstring]
   [goog.string.format]))

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (atom {}))

(def time-formarts (->> tf/formatters
                     (map (fn [[k v]]
                            (when (string? (:format-str v))
                              k)))
                     (filter identity)))

(defn get-app-element []
  (gdom/getElement "app"))

(defn- set-transit-value! [val]
  (swap! app-state assoc :transit val))

(defn- set-edn-value! [val]
  (swap! app-state assoc :edn val))
(defn- transit->edn [transit-text]
  (let [r (t/reader :json)]
    (pr-str (t/read r transit-text))))

(defn transit-edn []
  (let [button-style {:padding "2px 4px" :margin-right "1rem"}]
    [:div
     [:div
      [:div "Transit+Json"]
      [:textarea {:rows 5
                  :cols 80
                  :style {:margin-bottom "1rem"}
                  :value (:transit @app-state "")
                  :on-change #(do
                                (set-transit-value! (-> % .-target .-value))
                                (set-edn-value! nil))}]]
     [:div
      [:div "Clojure EDN"]
      [:textarea {:readOnly true
                  :rows 5
                  :cols 80
                  :on-change nil
                  :value (:edn @app-state "")
                  :style {:margin-bottom "1rem"}}]]
     [:div
      [:input {:type :button
               :style button-style
               :on-click #(do
                            (set-transit-value! nil)
                            (set-edn-value! nil))
               :value "Reset"}]
      [:input {:type :button
               :style button-style
               :disabled (s/blank? (:transit @app-state))
               :on-click #(set-edn-value! (transit->edn (:transit @app-state)))
               :value "Convert"}]]]))

(defn- set-value! [key val]
  )

(defn- time-input [title key val-fn]
  [:div {:style {:margin-right "1rem"}}
   [:div title]
   [:input {:value (key @app-state "")
            :on-change #(set-value! key (val-fn (-> % .-target .-value)))}]])

(defn- time-format-selector []
  ;; todo
  (let [ft (:time-formatter @app-state :rfc822)]
    [:div "Time format"
     [:div {:on-click #(swap! app-state update :show-timeformat-picker not)}
      [:div
       [:div (name ft)]
       [:div (-> tf/formatter ft :format-str)]]
      [:div (tf/unparse (tf/formatters ft) (now))]]]))

(defn- timeformat-picker []
  [:div "picker timeformat here"])

(defn time-converter []
  (let [button-style {:padding "2px 4px" :margin-right "1rem"}]
    [:div
     (when :show-timeformat-picker
       [timeformat-picker])
     [:div {:style {:display :flex :margin-bottom "1rem"}}
      [time-format-selector]
      [time-input "Time zone" :time-zone identity]]
     [:div {:style {:display :flex :margin-bottom "1rem"}}
      [time-input "Epoch time in milliseconds" :time-in-millis js/parseInt]
      [time-input "Epoch time in seconds" :time-in-seconds js/parseInt]
      [time-input "Readable time" :readable-time identity]]
     [:div
      [:input {:type :button
               :style button-style
               :on-click #(do
                            (set-transit-value! nil)
                            (set-edn-value! nil))
               :value "Reset"}]
      [:input {:type :button
               :style button-style
               :disabled (s/blank? (:transit @app-state))
               :on-click #(set-edn-value! (transit->edn (:transit @app-state)))
               :value "Convert"}]]]))

(defn mount [el]
  (rdom/render
    [:div
     [:div
      [:h3 "Transit to EDN conversion"]
      [transit-edn]]
     [:div
      [:h3 "Time conversion"]
      [time-converter]]]
    el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

;; conditionally start your application based on the presence of an "app" element
;; this is particularly helpful for testing this ns without launching the app

(defn init []
  (mount-app-element))

