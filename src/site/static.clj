(ns site.static
  (:require [site.layout :as layout]))

(defn about [{:keys [meta entry]}]
  (layout/layout
   [:main
    [:article
     [:h1 "About"]
     [:p "My name is 김수헌, written in English as Kim Sooheon, pronounced \"Sue
    Hun\". You can find me going by the alias "
      [:code "sooheon"]
      " in a few places online, including "
      [:a {:href "https://news.ycombinator.com/user?id=sooheon"} "Hacker News"] ", and "
      [:a {:href "https://github.com/sooheon"} "Github"] "."]
     ;; [:p "I decided I might as well collect my thoughts in one place, hence the
     ;; blog. Enjoy, and don't hesitate to hit me up by email if there's anything
     ;; at all you want to discuss."]
     [:p "I enjoy coding Clojure, yak-shaving with Emacs, some armchair
    philosophy, and generally looking for reasonable beliefs to hold."]]]))
