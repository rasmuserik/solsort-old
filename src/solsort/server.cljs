(ns solsort.server
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(declare watcher)
(defn reload []
  (.close watcher)
  (.reload js/location))
(def watcher (.watch (js/require "fs") "build/server.js" reload))

(set! (.-innerHTML js/document.body) "hello blah blah blah server dashboard")
