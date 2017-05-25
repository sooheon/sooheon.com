(ns site.static
  (:require [site.layout :as layout]))

(defn about [{:keys [meta entry]}]
  (layout/layout
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
