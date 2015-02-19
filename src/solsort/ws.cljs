(ns solsort.ws
  (:require-macros 
    [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.registry :refer [testcase]]
    [solsort.system :refer [is-nodejs is-browser log]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(if is-nodejs
  (do
    (def ws (js/require "ws"))
    ))

(defn start-websocket-server [http-server]
  (log 'ws 'start)
  (let [ws (js/require "ws")
        wss (ws.Server. #js{:server http-server})]
    (log 'ws ws wss)
    (.on wss "connection" 
         (fn [ws]
           (log 'ws 'incoming-connection ws)
           (.on ws "message" (fn [data flags]
                               (.send ws data)
                               (log 'ws 'receive data)))
           (.on ws "close" (fn [data flags]
                               (log 'ws 'close )))
           (.send ws (js/JSON.stringify #js{ :hullo "hi"}))
           ))
    ))

(if is-browser
  (do
    (let
      [ws (js/WebSocket. "ws://localhost/ws/")]
      (aset ws "onopen" (fn [e] 
                          (log 'ws 'open e)
                          (.send ws (js/JSON.stringify #js{:a "Hullo"}))
                          ))
      (aset ws "onerror" (fn [e] (log 'ws 'error) (js/console.log e)))
      (aset ws "onclose" (fn [e] (log 'ws 'close e)))
      (aset ws "onmessage" (fn [e] 
                             (log 'ws 'message)
                             (js/console.log e)
                             ))
      (log 'ws ws)
      )))
