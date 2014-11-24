(ns solsort.util
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
   [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(defn parse-json-or-nil [str]
  (try
    (js/JSON.parse str)
    (catch :default _ nil)))

(defn http-req
  ([url params] (throw "not implemented"))
  ([url]
   (let
     ;TODO ie8/9-cors-support
     [result (chan)
      xhr (js/XMLHttpRequest.)]
     (.open xhr "GET" url true)
     (set! (.-withCredentials xhr) true)
     (set! (.-onerror xhr) #(close! result))
     (set! (.-onload xhr) #(put! result (.-responseText xhr)))
     (.send xhr)
     result)))
