(ns app.screens.day
  (:require
   ["color" :as color]
   ["react" :as react]
   ["react-native" :as rn]
   ["react-native-gesture-handler" :as g]
   ["react-native-modal-datetime-picker" :default DateTimePicker]
   ["react-native-paper" :as paper]

   [applied-science.js-interop :as j]
   [reagent.core :as r]

   [app.components.menu :as menu]
   [app.helpers :refer [<sub >evt get-theme clear-datetime-picker active-gesture?]]
   [app.screens.core :refer [screens]]
   [app.tailwind :refer [tw]]
   [potpuri.core :as p]))

(defn date-indicator []
  (let [{:keys [day-of-week
                day-of-month
                year
                month
                selected-day
                display-year
                display-month]}
        (<sub [:this-day])

        {:date-time-picker/keys [value mode visible]
         dtp-id                 :date-time-picker/id}
        (<sub [:date-time-picker])]

    [:> g/RectButton {:style    (tw "flex flex-1 flex-row justify-center")
                      :on-press #(>evt [:set-date-time-picker
                                        #:date-time-picker
                                        {:value   selected-day
                                         :mode    "date"
                                         :visible true
                                         :id      :day}])}
     [:> rn/View {:style (tw "flex flex-1 flex-row justify-center")}
      (when display-year
        [:> paper/Text {:style (tw "font-bold text-center m-2")} year])
      (when display-month
        [:> paper/Text {:style (tw "font-bold text-center m-2")} month])
      [:> paper/Text {:style (tw "font-bold text-center m-2")} day-of-week]
      [:> paper/Text {:style (tw "font-bold text-center m-2")} day-of-month]

      (when (and (some? value)
                 (= dtp-id :day))
        [:> DateTimePicker {:is-visible           visible
                            :is-dark-mode-enabled true
                            :date                 value
                            :mode                 mode
                            :on-hide              #(>evt clear-datetime-picker)
                            :on-cancel            #(>evt clear-datetime-picker)
                            :on-confirm           #(do
                                                     (tap> "day picker")
                                                     (>evt [:set-selected-day %])
                                                     (>evt clear-datetime-picker))}])]]))

(defn tracking-sessions []
  (let [theme  (->> [:theme] <sub get-theme)
        tracks (<sub [:tracking])]
    [:> g/ScrollView
     [:> paper/Surface {:style (tw "p-2 mb-2")}

      (for [{:tracking-render/keys
             [relative-width
              color-hex
              text-color-hex
              indicator-color-hex
              indicator-position
              show-indicator
              ripple-color-hex
              label] :as t} tracks]

        [:> rn/View {:key   (random-uuid)
                     :style (merge (tw "w-full max-h-32 m-2")
                                   ;; TODO justin 2020-01-23 Add to custom tailwind theme
                                   {:min-height 32}) }

         ;; session
         [:> rn/View {:style (merge
                               (tw "absolute h-8 p-1")
                               {:position         "absolute"
                                :top              0
                                :left             0
                                :overflow         "hidden"
                                :width            relative-width
                                :border-radius    (-> theme (j/get :roundness))
                                :background-color color-hex})}

          ;; intended duration indication
          (when show-indicator
            [:> rn/View {:style (merge
                                  (tw "absolute w-2 h-8")
                                  {:top              0
                                   :left             indicator-position
                                   :background-color indicator-color-hex})}])

          [:> paper/Text {:style {:color text-color-hex}} label]]

         ;; selection button needs to go "over" everything
         [:> g/RectButton {:on-press       #(println "selected tracking item")
                           :ripple-color   ripple-color-hex
                           :underlay-color ripple-color-hex
                           :active-opacity 0.7
                           :style          (-> (tw "absolute h-8 w-full")
                                               (merge {:top           0
                                                       :left          0
                                                       :border-radius (-> theme (j/get :roundness))}))}]])]]))

(defn top-section [props]
  (let [theme         (->> [:theme] <sub get-theme)
        menu-color    (-> theme
                          (j/get :colors)
                          (j/get :text))
        toggle-drawer (-> props
                          (j/get :navigation)
                          (j/get :toggleDrawer))]

    [:> rn/View {:style (tw "flex flex-col max-h-72")}

     [:> rn/View {:style (tw "flex flex-row items-center")}
      [menu/button {:button-color menu-color
                    :toggle-menu  toggle-drawer}]

      [date-indicator]]

     [tracking-sessions]]))

(defn time-indicators []
  (let [theme (->> [:theme] <sub get-theme)
        hours (<sub [:hours])]
    [:> rn/View
     (for [{:keys [top val]} hours]
       [:> rn/View {:key   (str (random-uuid))
                    :style (-> (tw "absolute w-full ml-1")
                               (merge {:top top}))}
        [:> paper/Divider]
        [:> paper/Text {:style {:color (-> theme
                                           (j/get :colors)
                                           (j/get :disabled))}}
         val]])]))

(defn sessions-component []
  (let [theme    (->> [:theme] <sub get-theme)
        sessions (<sub [:sessions-for-this-day])]

    [:> rn/View {:style (tw "ml-20")}
     (for [{:session-render/keys [left
                                  id
                                  top
                                  height
                                  width
                                  ;; elevation
                                  color-hex
                                  ripple-color-hex
                                  text-color-hex
                                  label
                                  is-selected
                                  start-label
                                  stop-label]
            :as                  s} sessions]

       [:> rn/View {:key (:session/id s)}

        (when is-selected
          [:> rn/View {:style (merge
                                (tw "absolute flex flex-row items-center")
                                {:top    (-> top (- 2))
                                 :height 2
                                 :left   -50
                                 :right  0})}
           [:> paper/Text start-label]
           [:> rn/View {:style (merge (tw "w-full ml-1")
                                      {:height           2
                                       :background-color (-> theme (j/get :colors) (j/get :text))}) }]])

        (when is-selected
          [:> rn/View {:style (merge
                                (tw "absolute flex flex-row items-center")
                                {:top    (-> top (+ height) (+ 2))
                                 :height 2
                                 :left   -50
                                 :right  0})}
           [:> paper/Text stop-label]
           [:> rn/View {:style (merge (tw "w-full ml-1")
                                      {:height           2
                                       :background-color (-> theme (j/get :colors) (j/get :text))}) }]])
        [:> g/LongPressGestureHandler
         {:min-duration-ms         800
          :on-handler-state-change (fn [e]
                                     (let [is-active (active-gesture? e)]
                                       (when is-active
                                         (>evt [:set-selected-session id]))))}
         [:> rn/View {:style (merge
                               (tw "absolute")
                               {:top    top
                                :left   left
                                :height height
                                :width  width})}
          [:> g/RectButton {:style
                            (-> (tw "h-full w-full")
                                (merge {;; :elevation        elevation
                                        :background-color color-hex}
                                       (when (not is-selected)
                                         {:border-radius (-> theme (j/get :roundness))} )))
                            :on-press       #(do (>evt [:set-selected-session id])
                                                 (>evt [:navigate (:session screens)]))
                            :ripple-color   ripple-color-hex
                            :underlay-color ripple-color-hex
                            :active-opacity 0.7}
           [:> rn/View {:style (tw "h-full w-full overflow-hidden p-1")}
            [:> paper/Text {:style {:color text-color-hex}} label]]]]]])]))

(defn now-indicator-component []
  (let [theme                 (->> [:theme] <sub get-theme)
        {:now-indicator-render/keys
         [position
          label
          display-indicator]} (<sub [:now-indicator])
        text-color            (-> theme (j/get :colors) (j/get :placeholder))]

    (when display-indicator
      [:> rn/View {:elevation 99 ;; otherwise the second session in a collision group covers it
                   ;; Theoritically enough items in a collision group will get past this but it isn't practical
                   :style     (-> (tw "absolute flex flex-row items-center")
                                  (merge {:top    position
                                          :height 2
                                          :left   32
                                          :right  0})) }
       [:> paper/Text {:style {:color text-color}} label]
       [:> rn/View {:style (-> (tw "w-full ml-1")
                               (merge {:height           2
                                       :background-color text-color}))}]
       ])))

(defn zoom-buttons []
  [:> rn/View {:style (tw "absolute top-0 right-0 opacity-25 w-12 h-3/4")}
   [:> paper/IconButton
    {:icon     "magnify-plus-outline"
     :size     32
     :on-press #(tap> "zoom in")
     :style    (tw "absolute top-80")}]

   [:> paper/IconButton
    {:icon     "magnify-minus-outline"
     :size     32
     :on-press #(tap> "zoom out")
     :style    (tw "absolute top-96")}]])

(def sheet-ref (j/call react :createRef))

(defn screen [props]
  (r/as-element
    [(fn [] ;; don't pass props here I guess that isn't how `r/as-element` works
       (let [theme (->> [:theme] <sub get-theme)
             zoom  (<sub [:zoom])]

         [:> rn/SafeAreaView {:style (merge (tw "flex flex-1")
                                            {:background-color (-> theme (j/get :colors) (j/get :background))})}
          [:> paper/Surface {:style (-> (tw "flex flex-1")
                                        (merge {:background-color (-> theme (j/get :colors) (j/get :background))}))}
           [:> rn/View
            [:> rn/StatusBar {:visibility "hidden"}]

            [top-section props]

            [:> rn/View ;; This allows for zoom buttons to be positioned below top section but _over_ scroll view of sessions

             [:> g/ScrollView
              [:> g/TapGestureHandler
               {:on-handler-state-change (fn [e]
                                           (let [is-active (active-gesture? e)]
                                             (when is-active
                                               (>evt [:set-selected-session nil]))))}
               [:> rn/View
                {:style {:height        (-> 1440 (* zoom))
                         :margin-bottom 256}}

                [time-indicators]

                [sessions-component]

                [now-indicator-component]]]]

             [zoom-buttons]]
            ]]]))]))
