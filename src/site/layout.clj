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

(defn welcome []
  [:p "Welcome to " [:code "(dissoc mind :thoughts)"] ", a blog by Sooheon Kim."])

(defn index [{:keys [entries]}]
  (layout
   [:article
    [:p "Welcome to " [:code "(dissoc mind :thoughts)"]
     ", a blog by me, Sooheon Kim."]
    [:h1 "Posts"]
    [:ul
     (doall (map (fn [{:keys [draft title permalink date-published] :as entry}]
                   [:li
                    [:a {:href permalink} title]
                    " "
                    (if draft
                      [:span.mono "(draft)"]
                      [:span.mono "(" (util/iso-date-fmt date-published) ")"])])
                 entries))]]))

(defn post [{:keys [entry entries]}]
  (layout [:main
           [:article
            [:div.article-title (:title entry)]
            [:div [:code "Published: " (util/iso-date-fmt (:date-published entry))]]
            (:content entry)
            [:br]
            [:p "Thanks for reading."]
            [:a {:href "/"} "Back to index"]
            ;; (common/disqus entry)
            ;; (recent-posts entry entries)
            ]]))

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
