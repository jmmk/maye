(defproject maye "0.2.0-SNAPSHOT"
  :description "Minimal ECS for ClojureScript"
  :url "https://www.github.com/jmmk/maye"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [tailrecursion/cljs-priority-map "1.1.0"]
                 [prismatic/schema "1.0.3"]
                 [org.clojure/clojurescript "1.7.170"]]
  :plugins [[lein-cljsbuild "1.1.1"]]
  :clean-targets ^{:protect false} ["target" "out" "out-adv"]
  :source-paths ["src"]
  :figwheel {:nrepl-port 7888}
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src"]
                        :compiler {:main "maye.core"
                                   :output-to "out/maye.js"
                                   :output-dir "out"
                                   :optimizations :none
                                   :cache-analysis true
                                   :source-map true}}
                       {:id "release"
                        :source-paths ["src"]
                        :compiler {:output-to "out-adv/maye.min.js"
                                   :output-dir "out-adv"
                                   :optimizations :advanced
                                   :pretty-print false}}]})
