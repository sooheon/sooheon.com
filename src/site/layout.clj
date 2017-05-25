(ns site.layout
  (:require [hiccup.page :as hiccup-page]
            [site.common :as common]
            [site.util :as util]
            [clojure.string :as string]))

(defn recent-posts [entry entries]
  (let [recent-entries (->> entries
                            (filter :date-published)
                            (sort-by :date-published)
                            reverse
                            (take 5))]
    (when (not-empty entries)
      [:div.mb5
       [:h2 "Recent Posts"]
       (into
        [:ol.list-reset]
        (for [post recent-entries]
          [:li
           [:a {:href (:permalink post)} (:title post)]
           [:code.ml1 (when-let [date (:date-published post)]
                        (util/month-fmt date))]]))])))

(defn layout [content]
  (hiccup-page/html5
   {:lang "en"}
   (common/head {})
   [:body
    common/header
    [:div.content.mx-auto
     [:div.clearfix content]]
    common/footer]))

(defn index [{:keys [entries]}]
  (layout
   [:div
    [:h2 "Contents"]
    [:ul
     (doall (map (fn [{:keys [draft title permalink date-published] :as entry}]
                   [:li
                    [:a {:href permalink} title]
                    " "
                    (if draft
                      [:span.grey.mono "(draft)"]
                      [:span.grey.mono "(" (util/iso-date-fmt date-published) ")"])])
                 entries))]
    [:br]
    [:hr.rule]
    [:p "This is the blog of Sooheon Kim"]]))

(defn post [{:keys [entry entries]}]
  (layout [:main
           [:article
            [:h1.h1 (:title entry)]
            [:div.grey.mono "Published: " (util/iso-date-fmt (:date-published entry))]
            (:content entry)]
           ;; (common/disqus entry)
           (recent-posts entry entries)]))

(defn year-overview [{:keys [entries]}]
  (layout
   [:div
    [:h2 (str "Year " (-> (first entries)
                          :permalink
                          (string/split #"/")
                          second))]
    [:ul
     (doall (map (fn [{:keys [draft title permalink date-published] :as entry}]
                   [:li [:a {:href permalink} title]])
                 entries))]]))
