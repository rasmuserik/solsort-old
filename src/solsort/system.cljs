(ns solsort.system
  (:require-macros 
    [solsort.system :refer [defapi]]
    [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.test :refer [testcase]]
    [solsort.mbox :as mbox :refer [route local log]]
    [solsort.platform :as platform :refer [ensure-dir]]
    [clojure.string :as string]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))
(comment enable-print)
(enable-console-print!)


(def is-nodejs platform/is-nodejs)
(def is-browser platform/is-browser)
(def origin platform/origin)
(def XHR platform/XHR)
(def fs platform/fs)
(def set-immediate platform/set-immediate)


(defn read-file-sync [filename]
  (.readFileSync (js/require "fs") filename))
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


(defn ajax [url & {:keys [post-data CORS jsonp]}]
  (if (and jsonp is-browser)
    (platform/jsonp (str url "?callback="))
    (let [c (chan)
          req (XHR.)
          ]
      (.open req (if post-data "POST" "GET") url true)
      (if CORS (aset req "withCredentials" true))
      (aset req "onreadystatechange"
            (fn []
              (if (= (aget req "readyState") (or (aget req "DONE") 4))
                (let [text (aget req "responseText")]
                  (if text
                    (put! c text)
                    (close! c))))))
      (.send req)
      c)))

