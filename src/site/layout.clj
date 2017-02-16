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
   [:a {:href "/feed.rss"} "Subscribe"] " * "
   [:a {:href "mailto:tngjs0@gmail.com"} "Conversation"]])

(defn recent-posts [entry entries]
  (let [recent-entries (->> entries
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
           [:code.ml1 (month-fmt (:date-published post))]]))])))

(defn main [entry entries]
  [:main
   [:article
    [:h1.h1 (:title entry)]
    [:div.grey.mono "Published: " (iso-date-fmt (:date-published entry))]
    (:content entry)]
   (common/disqus entry)
   (recent-posts entry entries)])

(defn base [content]
  (hp/html5
   {:lang "en"}
   (common/head {})
   [:body
    header
    content
    footer]))

(defn index-page [{:keys [entries]}]
  (base (main (first entries) entries)))

(defn post-page [{:keys [entry entries]}]
  (base (main entry entries)))

(defn about-page [{:keys [meta entry]}]
  (base
   [:main
    [:h1 "About"]
    [:p "My name is 김수헌, written in English as Kim Sooheon, first name
pronounced Sue Hun. That made me at one time a boy named Sue and Hon(ey)."]
    [:p "After finding myself toiling over yet another long-winded response to a
random stranger online, I decided I might as well collect my thoughts in one
place, hence the blog. Enjoy, and don't hesitate to hit me up by email if
there's anything at all you want to discuss."]
    [:p "I enjoy coding Clojure, yak-shaving with Emacs, some armchair
philosophy, and generally looking to find reasonable beliefs in a complex
world."]]))
