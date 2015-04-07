(ns solsort.platform
  (:require-macros 
    [cljs.core.async.macros :refer [go alt!]])
  (:require
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(def global 
  (cond
    (exists? js/window) js/window
    (exists? js/global) js/global
    (exists? js/self) js/self
    :else ((fn [] js/this))))
(def is-browser (and (exists? js/window) (exists? js/window.document)))
(def is-nodejs (and
                 (exists? js/global)
                 (.hasOwnProperty js/global "process")
                 (.hasOwnProperty js/global.process "title")))
(def origin (if is-nodejs "http://localhost:9999" js/location.origin))
(def XHR (if is-nodejs (aget (js/require "xmlhttprequest") "XMLHttpRequest") js/XMLHttpRequest))
(def fs (if is-nodejs (js/require "fs")))
(def set-immediate ; "execute function immediately after event-handling"
  (if (exists? js/setImmediate)
    js/setImmediate ; node.js and IE (IE might be buggy)
    (fn [f] (js/setTimeout f 0))))

(if (and is-nodejs (not is-browser)) 
  (do 
    (aset global "Worker" (aget (js/require "webworker-threads") "Worker"))
    (aset global "React" (js/require "react"))))
(aset global "window" global)
(if (and is-nodejs (not (exists? Showdown)))
  (aset global "Showdown" (js/require "showdown")))
