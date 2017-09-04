(ns site.common
  (:require [hiccup.page :as hp]))

(defn head
  [{:keys [title]}]
  [:head
   [:meta {:name "google-site-verification" :content "eseBEPCF7c-u0FfNjY5n4RXqqwLbmJnm1-yq-irSw6c"}]
   [:meta {:charset "utf-8"}]
   [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
   [:meta {:itemprop "author" :name "author" :content "Sooheon Kim (sooheon.k@gmail.com)"}]
   [:link {:rel "shortcut icon" :href "/images/favicon.ico"}]
   (hp/include-css "/vendor/basscss@8.0.1.min.css")
   (hp/include-css "/vendor/highlight.css")
   (hp/include-css "/stylesheets/site.css")
   (hp/include-js "/vendor/highlight.js")
   ;; [:script "hljs.initHighlightingOnLoad();"]
   [:title (if title (str title " | Sooheon") "(dissoc mind :thoughts)")]])

(def header [:header.mono
             [:div#title [:a {:href "/"}
                          [:span#letter "*"]
                          [:span#letter "*"]
                          [:span#letter "*"]
                          [:span#letter "*"]]]
             [:div "(dissoc mind :thoughts)"]])

(def footer [:footer.mono
             [:a {:href "/about.html"} "About"]
             " * "
             [:a {:href "/feed.rss"} "RSS Feed"]
             " * "
             [:a {:href "mailto:sooheon.k@gmail.com"} "Email"]])

(defn disqus [post]
  [:div.mt4.mb4
   [:div {:id "disqus_thread"}]
   [:script {:async true :defer true} (format "
var disqus_config = function () {
this.page.url = '%s';
this.page.identifier = '%s';
};

(function() {
var d = document, s = d.createElement('script');
s.src = '//sooheon-com.disqus.com/embed.js';
s.setAttribute('data-timestamp', +new Date());
(d.head || d.body).appendChild(s);
})();"
                                              (:canonical-url post)
                                              (:uuid post))]
   [:noscript "Please enable JavaScript to view the <a href=\"https://disqus.com/?ref_noscript\">comments powered by Disqus.</a>"]])
