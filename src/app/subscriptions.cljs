(ns app.subscriptions
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [com.rpl.specter :as sp :refer [select
                                            select-one
                                            select-one!]]
            [tick.alpha.api :as t]))

(defn version [db _]
  (->> db
       (select-one! [:version])))

(defn theme [db _]
  (->> db
       (select-one! [:settings :theme])))

(defn selected-day [db _]
  (->> db (select-one! [:view :view/selected-day])))

(defn calendar [db _]
  (->> db (select-one! [:calendar])))

(defn sessions [db _]
  (->> db (select-one! [:sessions])))

(reg-sub :version version)
(reg-sub :theme theme)

(reg-sub :selected-day selected-day)
(reg-sub :calendar calendar)
(reg-sub :sessions sessions)

(reg-sub
  :sessions-for-this-day

  :<- [:selected-day]
  :<- [:calendar]
  :<- [:sessions]

  (fn [[selected-day calendar sessions] _]
    (let [this-day (get calendar selected-day)]
      (->> this-day
           :calendar/sessions
           (map #(get sessions %))
           vec))))

(reg-sub
  :this-day

  :<- [:selected-day]

  ;; If there is only one signal sub then the inputs are not in a vector :O
  (fn [selected-day _]
    (let [month (t/month selected-day)
          year  (t/year selected-day)
          now   (t/now)]
      {:day-of-week   (->> selected-day
                           t/day-of-week
                           str
                           (take 3)
                           (clojure.string/join ""))
       :day-of-month  (t/day-of-month selected-day)
       :year          year
       :month         month
       :display-year  (not= year (t/year now))
       :display-month (not= month (t/month now))})))
