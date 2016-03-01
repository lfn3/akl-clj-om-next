(defproject akl-clj-om-next "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.189"]
                 [org.omcljs/om "1.0.0-alpha30"]

                 ;;Server
                 [http-kit "2.1.18"]

                 ;;Dev deps
                 [devcards "0.2.1-4"]
                 [figwheel-sidecar "0.5.0-2"]
                 [com.cemerick/piggieback "0.2.1"]
                 [org.clojure/tools.nrepl "0.2.10"]]

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :main ^:skip-aot akl-clj-om-next.core
  :target-path "target/%s"

  :profiles {:uberjar {:aot :all}})
