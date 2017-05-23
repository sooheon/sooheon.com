(set-env!
 :source-paths #{"src" "content"}
 :resource-paths #{"resources"}
 :dependencies '[[org.clojure/tools.nrepl "0.2.13" :exclusions [org.clojure/clojure]]
                 [pandeiro/boot-http "0.8.3" :exclusions [org.clojure/clojure]]
                 [deraen/boot-livereload "0.2.0"]
                 [perun "0.4.2-SNAPSHOT"]
                 [confetti/confetti "0.1.5"]
                 [hiccup "1.0.5" :exclusions [org.clojure/clojure]]
                 [clj-time "0.13.0"]])

(require '[pandeiro.boot-http :refer [serve]]
         '[boot.util :as util]
         '[deraen.boot-livereload :refer [livereload]]
         '[io.perun :as p]
         '[io.perun.core :as perun]
         '[io.perun.meta :as pm]
         '[site.layout :as layout]
         '[clojure.string :as string]
         '[confetti.boot-confetti :refer [sync-bucket]])

(task-options!
 pom {:project 'sooheon.org
      :version "0.1.0"})

(deftask new
  "Creates a new post with frontmatter and uuid"
  [n filename FILENAME str "Filename of new post"]
  (let [uuid (java.util.UUID/randomUUID)
        filepath (format "content/drafts/%s" filename)
        front-matter (format "---\ntitle: %s \nuuid: %s\ndraft: true\ndate-published: \n---\n"
                             (-> (string/split filename #"\.")
                                 first
                                 (string/replace #"-" " "))
                             uuid)]
    (spit filepath front-matter)))

(defn slug-fn [_ m]
  "Parses `slug` portion out of the filename in the format: slug-title.ext"
  (->> (string/split (:filename m) #"[-\.]")
       drop-last
       (string/join "-")
       string/lower-case))

(defn published? [{:keys [date-published]}]
  date-published)

(deftask build
  "Build sooheon.org"
  [i include-drafts bool "Include drafts?"]
  (comp
   (p/global-metadata)
   (p/markdown :md-exts {:smartypants true})
   (if include-drafts identity (p/draft))
   (p/slug :slug-fn slug-fn)
   (p/permalink)
   (p/render :renderer 'site.layout/post-page)
   (p/collection :renderer 'site.layout/index-page :page "index.html")
   (p/static :renderer 'site.layout/about-page :page "about.html")
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
