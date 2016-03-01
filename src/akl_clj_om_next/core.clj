(ns akl-clj-om-next.core
  (:require [compojure.core :refer [routes GET POST]]
            [compojure.route :refer [resources]]
            [org.httpkit.server :as hk]
            [clojure.java.io :as io]
            [figwheel-sidecar.repl-api :as ra])
  (:gen-class))

(def figwheel-config
  {:figwheel-options {:css-dirs ["resources/public/css"]}
   :all-builds       [{:id           "devcards"
                       :figwheel     {:devcards true}
                       :source-paths ["src/akl_clj_om_next"]
                       :compiler     {:main       "akl-clj-om-next.core"
                                      :asset-path "/out-devcards"
                                      :output-to  "resources/public/js/devcards.js"
                                      :output-dir "resources/public/out-devcards"
                                      :verbose    true}}]})

(defn start []
  (ra/start-figwheel! figwheel-config))

(defn stop []
  (ra/stop-figwheel!))

(def ring-handler
  (routes
    (GET "/" [] (io/resource "public/index.html"))
    #_ (POST "/api")                                        ; TODO: make a working remote (Super-extra credit)
    (resources "/")))                                       ; You can use om-next's parse functions on the server
                                                            ; If you want to cheat look at David Nolen's om-next-demo
                                                            ; https://github.com/swannodette/om-next-demo

(def srv (atom nil))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (reset! srv (hk/run-server ring-handler {:port 8080}))
  (start))
