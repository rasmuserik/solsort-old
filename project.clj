(defproject wecademy "0.1.0-SNAPSHOT"
  :description "solsort.com server"
  :url "http://solsort.com"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2311"]
                 [org.clojure/core.async "0.1.319.0-6b1aca-alpha"]]

  :plugins [[lein-cljsbuild "1.0.3"]]

  :source-paths ["src"]

  :cljsbuild { 
    :builds [
             {:id "server"
              :source-paths ["src"]
              :compiler {
                :output-to "build/server.js"
                :output-dir "build"
                :optimizations :none
                :pretty-print true
                :source-map "build/server.map"}}]})
