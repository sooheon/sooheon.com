(def project 'sooheon.org)
(def version "0.1.0")

(set-env!
 :source-paths #{"src" "content/drafts" "content/posts"}
 :resource-paths #{"resources"}
 :dependencies '[[org.clojure/tools.nrepl "0.2.13" :exclusions [org.clojure/clojure]]
                 [pandeiro/boot-http "0.8.3" :exclusions [org.clojure/clojure]]
                 [deraen/boot-livereload "0.2.0"]
                 [perun "0.4.2-SNAPSHOT"]
                 [confetti/confetti "0.2.0"]
                 [hiccup "1.0.5" :exclusions [org.clojure/clojure]]
                 [clj-time "0.13.0"]])

(require '[pandeiro.boot-http :refer [serve]]
         '[deraen.boot-livereload :refer [livereload]]
         '[io.perun :as p]
         '[io.perun.core :as perun]
         '[io.perun.meta :as pm]
         '[site.util :as util]
         '[clojure.string :as string]
         '[confetti.boot-confetti :refer [sync-bucket]]
         '[boot.lein])

(boot.lein/generate)

(task-options!
 pom {:project project
      :version version})

(defn- slug-fn [_ {:keys [date-published filename]}]
  "Parses a filename into a url slug. If the file is a published post, the year and month
  are prepended to the path:

  *base-url*/2017/05/file-name.html"
  (let [file-slug (->> (string/split filename #"[-\.]")
                       drop-last
                       (string/join "-")
                       string/lower-case)]
    (if date-published
      (str (util/yyyy-MM-fmt date-published) file-slug)
      file-slug)))

(defn- permalink-fn [global-meta {:keys [parent-path slug]}]
  (-> (str parent-path slug)
      perun/path-to-url
      (str ".html")
      (string/replace (re-pattern (str "^" (:doc-root global-meta))) "")
      perun/absolutize-url))

(defn- in-2017?
  "If the permalink URL after sooheon.org begins with \"2017\"."
  [{:keys [permalink]}]
  (= "2017" (second (string/split permalink #"/"))))

;; Build and Dev Tasks

(deftask new-post
  "Creates a new post with frontmatter and uuid. TITLE should be a
  string (spaces and capitalization OK)."
  [n title TITLE str "Title of new post"]
  (let [uuid (java.util.UUID/randomUUID)
        filepath (format "content/drafts/%s"
                         (str (string/replace (.toLowerCase title) #" " "-")
                              ".md"))
        front-matter (format "---\ntitle: %s \nuuid: %s\ndraft: true\ndate-published: \n---\n"
                             title
                             uuid)]
    (spit filepath front-matter)))

(deftask build
  "Build sooheon.org"
  [i include-drafts bool "Include drafts?"]
  (comp
   (p/global-metadata)
   (p/markdown :md-exts {:smartypants true})
   (if include-drafts identity (p/draft))
   (p/slug :slug-fn slug-fn)
   (p/permalink :permalink-fn permalink-fn)
   (p/render :renderer 'site.layout/post)
   (p/collection :renderer 'site.layout/index :page "index.html")
   (p/collection :renderer 'site.layout/year-overview
                 :page "2017/index.html"
                 :filterer in-2017?)
   (p/static :renderer 'site.static/about :page "about.html")
   (p/rss)
   (p/sitemap :filterer #(not= (:slug %) "404"))))

(deftask dev
  "Build sooheon.org for local development"
  [e exclude-drafts bool "Drafts are included by default. Exclude drafts?"]
  (comp
   (serve :resource-root "public")
   (watch)
   (build :include-drafts (not exclude-drafts))
   (target)
   (livereload :asset-path "public")))

(deftask deploy
  "Publish to S3 using confetti. Requires a confetti.edn file in directory
  root."
  []
  (task-options! build {:include-drafts false})
  (comp (build)
        (p/inject-scripts :scripts #{"ga-inject.js"})
        (sift :include #{#"^public/"})
        (sift :move {#"^public/" ""})
        (sync-bucket :confetti-edn "sooheon-org.confetti.edn")))
