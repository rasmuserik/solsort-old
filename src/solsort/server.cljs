(ns solsort.server
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
   [cljs.core.async :refer [>! <! chan put! take! timeout close!]]
   [solsort.node :refer [exec]]
   ))

(defn parse-json-or-nil [str]
  (try
    (js/JSON.parse str)
    (catch :default _ nil)))
(go
  (.log js/console "main hello")
  (.log js/console (parse-json-or-nil (<! (exec "ssh uccorganism@93.165.158.107 'curl -s localhost:8080/status'"))))

 "ssh uccorganism@93.165.158.107 'killall VBoxHeadless; launchctl load Library/LaunchAgents/apiserver.plist; launchctl start apiserver'"

  (<! (timeout 1000))
  (.log js/console "main hello2")
 )

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
