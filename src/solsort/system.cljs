(ns solsort.system
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(enable-console-print!)
(def browser (exists? js/window))
(def global (if browser js/window js/global)) ; various conditional global assignments below
(if (not browser) (aset global "window" global))
(defn exec [cmd]
  (let [c (chan)]
    (.exec (js/require "child_process") cmd
           (fn [err stdout stderr]
             (if (= err nil)
               (put! c stdout)
               (close! c)
               )))
    c))

(defn each-lines [filename]
  (let
    [c (chan 1)
     buf (atom "")
     stream (.createReadStream (js/require "fs") filename)]
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
(def nodejs (and 
              (exists? js/global)
              (.hasOwnProperty js/global "process") 
                 (.hasOwnProperty js/global.process "title") 
                 (= js/global.process.title "node")))
(defn set-immediate [f] "execute function immediately after event-handling"
  (if (exists? js/setImmediate) 
    js/setImmediate ; node.js and IE (IE might be buggy)
    (fn [f] (js/setTimeout f 0))))

(set-immediate #(print 'isBrowser browser))
