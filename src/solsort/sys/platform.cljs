(ns solsort.sys.platform
  (:require-macros 
    [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.sys.mbox :refer [log]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(enable-console-print!)

;; Global+predicates
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


;; Browser API
(def origin (if is-nodejs "http://localhost:9999" js/location.origin))
(def XHR (if is-nodejs (aget (js/require "xmlhttprequest") "XMLHttpRequest") js/XMLHttpRequest))
(def set-immediate ; "execute function immediately after event-handling"
  (if (exists? js/setImmediate)
    js/setImmediate ; node.js and IE (IE might be buggy)
    (fn [f] (js/setTimeout f 0))))

(def worker 
  (if is-nodejs 
    (aget (js/require "webworker-threads") "Worker")
    (aget global "Worker")))


; jsonp
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


; react
(when (and is-nodejs (not is-browser)) 
  (aset global "React" (js/require "react")))


;; File system
(def fs (if is-nodejs (js/require "fs")))
(defn ensure-dir [dirname] (if (not (.existsSync fs dirname)) (.mkdirSync fs dirname)))

(defn read-file-sync [filename] (.readFileSync (js/require "fs") filename))
(defn each-lines [filename]
  (let
    [c (chan 1)
     buf (atom "")
     stream (.createReadStream fs filename)]
    (.on stream "data"
         (fn [data]
           (.pause stream)
           (go
             (swap! buf #(str % data))
             (let [lines (.split @buf "\n")]
               (swap! buf #(aget lines (- (.-length lines) 1)))
               (loop [i 0]
                 (if (< i (- (.-length lines) 1))
                   (do
                     (>! c (str (aget lines i) "\n"))
                     (recur (inc i))))))
             (.resume stream)
             )
           ))
    (.on stream "close"
         (fn []
           (put! c @buf)
           (close! c)))
    c))


;; OS
(defn exec [cmd]
  (let [c (chan)]
    (.exec (js/require "child_process") cmd
           (fn [err stdout stderr]
             (if (= err nil)
               (put! c stdout)
               (close! c)
               )))
    c))

(defn exit [errcode]
  (go
    (<! (timeout 300))
    (log 'system 'exit errcode)
    (if is-nodejs
      (js/process.exit errcode))))

