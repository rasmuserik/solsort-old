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


(comment jsonp)
(when is-browser
  (def -unique-id-counter (atom 0))
  (defn unique-id [] (str "id" (swap! -unique-id-counter inc)))
  (defn jsonp [url]
    (let [c (chan)    
          id (unique-id)]
      (aset global id 
            (fn [o]
              ;(log 'call-jsonp id o)
              (if o 
                (put! c (js/JSON.stringify o))
                (close! c))
              (goog.object.remove global id)))
      (let [tag (js/document.createElement "script")]
        (aset tag "src" (str url id))
        (js/document.head.appendChild tag))
      c)))


(comment global)
(when (and is-nodejs (not is-browser)) 
  (aset global "Worker" (aget (js/require "webworker-threads") "Worker"))
  (aset global "React" (js/require "react")))
