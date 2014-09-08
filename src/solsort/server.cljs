(ns solsort.server
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
   [cljs.core.async :refer [>! <! chan put! take! timeout close!]]
   [solsort.express]))

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

(set! (.-innerHTML js/document.body) "hallo blah blah blah server dashboard")
