(ns site.layout
  (:require [site.common :as common]
            [boot.util :as util]
            [hiccup.page :as hp]
            [clj-time.core :as tc]
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
   [:div.subtitle "a blog about looking back, pillars of salt, &c."]])

(def footer
  [:footer.mono
   [:a {:href "/about.html"} "About"] " * "
   [:a {:href "/atom.xml"} "Subscribe"] " * "
   [:a {:href "mailto:tngjs0@gmail.com"} "Conversation"]])

(defn main [entry entries]
  (let [other-entries (->> entries
                           (remove #(= entry %))
                           (sort-by :date-published)
                           reverse)]
    [:main
     [:article
      [:h1.h1 (:title entry)]
      [:div.grey.mono "Published: " (iso-date-fmt (:date-published entry))]
      (:content entry)]
     (common/disqus entry)
     (when (not-empty other-entries)
       [:div.mb5
        [:h2 "Recent Posts"]
        (into
         [:ol.list-reset]
         (for [post other-entries]
           [:li
            [:a {:href (:permalink post)} (:title post)]
            [:code.ml1 (month-fmt (:date-published post))]]))])]))

(defn body [content]
  (hp/html5
   {:lang "en"}
   (common/head {})
   [:body
    header
    content
    footer]))

(defn index-page [{:keys [entries]}]
  (body (main (first entries) entries)))

(defn post-page [{:keys [entry entries]}]
  (body (main entry entries)))

(defn about-page [])
