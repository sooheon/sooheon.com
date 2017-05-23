(ns site.layout
  (:require [site.common :as common]
            [hiccup.page :as hp]
            [clj-time.coerce :as to]
            [clj-time.format :as tf]))

(defn trace [x] (prn x) x)

(defn iso-date-fmt [date]
  (tf/unparse (tf/formatter "yyyy-MM-dd") (to/from-date date)))

(defn month-fmt [date]
  (str (subs (tf/unparse (tf/formatter "MMMM") (to/from-date date)) 0 3)
       (tf/unparse (tf/formatter " yyyy") (to/from-date date))))

(def header
  [:header.mono
   [:div.title [:a {:href "/"} "****"]]
   [:div.subtitle "Things I've done and thought about"]])

(def footer
  [:footer.mono
   [:a {:href "/about.html"} "About"] " * "
   [:a {:href "/feed.rss"} "Subscribe"] " * "
   [:a {:href "mailto:tngjs0@gmail.com"} "Conversation"]])

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
                        (month-fmt date))]]))])))

(defn layout [content]
  (hp/html5
   {:lang "en"}
   (common/head {})
   [:body
    header
    [:div.content.mx-auto
     [:div.clearfix content]]
    footer]))

(defn index-page [{:keys [entries] :as data}]
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
                      [:span.grey.mono "(" (iso-date-fmt date-published) ")"])])
                 entries))]]))

(defn post-page [{:keys [entry entries]}]
  (layout [:main
           [:article
            [:h1.h1 (:title entry)]
            [:div.grey.mono "Published: " (iso-date-fmt (:date-published entry))]
            (:content entry)]
           ;; (common/disqus entry)
           ;; (recent-posts entry entries)
           ]))

(defn about-page [{:keys [meta entry]}]
  (layout
   [:main
    [:h1 "About"]
    [:p "My name is 김수헌, written in English as Kim Sooheon, pronounced \"Sue
    Hun\"."]
    [:p "After finding myself toiling over yet another long-winded response to a
    random stranger online, I decided I might as well collect my thoughts in one
    place, hence the blog. Enjoy, and don't hesitate to hit me up by email if
    there's anything at all you want to discuss."]
    [:p "I enjoy coding Clojure, yak-shaving with Emacs, some armchair
    philosophy, and generally looking for reasonable beliefs to hold about a
    complex world."]]))
