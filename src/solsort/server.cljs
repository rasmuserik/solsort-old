(ns solsort.server
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]
    [solsort.manager]
    [solsort.relvis-server]
    [solsort.webserver]
    ))

(enable-console-print!)

(def commands (atom {}))
(defn register [cmd f] (swap! commands assoc cmd f))

(register "manager" solsort.manager.start)
(register "relvis-server" solsort.relvis-server.start)
(register "hello" (fn [] (solsort.webserver/add "hello" (fn [info] 
                                                          (print 'muyhaha)
                                                          (go info)))))
(solsort.webserver/add :default #(go :default))

(def arg
  (or
    (and js/window.process (get js/window.process.argv 2))
    (and (exists? js/window) js/window js/window.location (.slice js/window.location.hash 1))))

(print arg)

(apply (or (get @commands arg)
           (fn []
             (print "possible arguments:")
             (doall (map (fn [[a b]] (print a)) @commands)))))



#_(

   (.reload js/location)

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
