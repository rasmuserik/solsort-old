(ns solsort.node
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(defn exec [cmd]
  (let [c (chan)]
    (.exec (js/require "child_process") cmd
           (fn [err stdout stderr]
             (if (= err nil)
               (put! c stdout)
               (close! c)
               )))
    c))

(defn eachLines [filename]
  (let
    [c (chan 1)
     buf (atom "")
     fs (js/require "fs")
     stream (.createReadStream fs filename)
     ]
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
