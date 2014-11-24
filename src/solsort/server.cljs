(ns solsort.server
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
   [cljs.core.async :refer [>! <! chan put! take! timeout close!]]
   [solsort.manager]
   ))

(enable-console-print!)

(def commands (atom {}))
(defn register [cmd f] (swap! commands assoc cmd f))

(register "manager" solsort.manager.start)

(apply (or (get @commands (get js/process.argv 2))
            (fn []
                   (print "possible arguments:")
                   (doall (map (fn [[a b]] (print a)) @commands)))))




#_(
(ns solsort.server
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
   [cljs.core.async :refer [>! <! chan put! take! timeout close!]]
   [solsort.express]
   [solsort.util]
   ))

(declare watcher)
(def running (atom true))

(defn reload []
  (go
   (if @running
     (do
       (reset! running false)
       (.close watcher)
       (<! (solsort.express.shutdown))
       (<! (timeout 1000))
       (.reload js/location))
     nil)))

(def watcher (.watch (js/require "fs") "build/server.js" reload))

(defn log [& a] (.log js/console a))

(log "starting")

(defn find-solsort-repos []
  (go
   (loop [repos #{} i 1]
       (let
         [json (.parse js/JSON (<! (solsort.util.http-req (str "https://api.github.com/orgs/solsort/repos?page=" i)))) ]
         (if (= 0 (.-length json))
           repos
           (recur (into repos (map #(% "name") (js->clj json))) (inc i))
           )))))

#_(def fs (js/require "fs"))

;(go (dorun (map log (<! (find-solsort-repos)))))
#_(go (do-seq
     [name (<! (find-solsort-repos))]
     (.log js/console name)
     ))

)
