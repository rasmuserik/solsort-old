(ns solsort.ws
  (:require-macros 
    [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.registry :refer [testcase post]]
    [solsort.system :refer [is-nodejs is-browser log pid set-immediate]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(def pids (atom {}))
(declare send-message)
(defn broadcast [msg]
  (doall (for [pid (keys @pids)]
           (send-message pid msg))))
(defn send-message [pid msg] 
  (.send (@pids pid) (js/JSON.stringify msg)))


(comment server)
(if is-nodejs
  (do
    (def ws (js/require "ws"))
    (defn handle-message [pid] 
      (fn [msg]
        (let [msg (js/JSON.parse msg)]
          (aset msg "src" (str "ws:" pid))
          (post msg)
          (log 'ws pid 'msg msg))))
    (defn close-connection [id] 
      (fn []
        (log 'ws id 'close)
        (swap! pids dissoc id)))
    (defn start-websocket-server [http-server]
      (log 'ws 'start)
      (let [ws (js/require "ws")
            wss (ws.Server. #js{:server http-server})]
        (.on wss "connection" 
             (fn [ws]
               (log 'ws 'incoming-connection ws)
               (.send ws (js/JSON.stringify  #js{:pid pid}))
               (.on ws "message" 
                    (fn [data flags]
                      (let [data (js/JSON.parse data)
                            pid (aget data "pid") ]
                        (if pid
                          (do
                            (.removeAllListeners ws "message")
                            (.on ws "message" (handle-message pid))
                            (.on ws "close" (close-connection pid))
                            (swap! pids assoc pid ws)
                            (log 'ws 'added-client pid @pids)
                            )
                          (log 'ws 'error-unexpected-first-message data)
                          ))))
               ))
        ))
    ))


(comment browser)
(if is-browser
  (do
    (comment keep-alive loop)
    (go 
      (loop []
        (<! (timeout 55000))
        (broadcast #js{:mbox "keep-alive"})
        (recur)))

    (def socket-server
      (if (= -1 (.indexOf js/location.origin "solsort"))
        (if (= "http" (.slice js/location.origin 0 4))
        (str (.replace js/location.origin #"https?" "ws") "/ws/")
          "ws://ws.solsort.com/ws/")
        "ws://ws.solsort.com/ws/"
        ))

    (defn ws-connect []
      (let
        [ws (js/WebSocket. socket-server)]
        (aset ws "onopen" (fn [e] (.send ws (js/JSON.stringify #js{:pid pid}))))
        (aset ws "onerror" 
              (fn [e] 
                (log 'ws 'error) 
                (js/console.log e)))
        (aset ws "onclose" 
              (fn [e] 
                (log 'ws 'close e)
                ; TODO exponential delay reconnect if server to connect to
                (go
                  (<! (timeout 1000))
                  (ws-connect))))
        (aset ws "onmessage" 
              (fn [e] 
                (let [data (js/JSON.parse (aget e "data"))
                      pid (aget data "pid")]
                  (if pid
                    (do
                      (aset ws "onmessage" 
                            (fn [e]
                              (let [msg (js/JSON.parse (aget e "data"))]
                                (aset msg "src" (str "ws:" pid))
                                (post msg)
                                (log 'ws pid 'msg msg))))
                      (aset ws "onclose" 
                            (fn [e] 
                              (log 'ws 'close e pid)
                              (swap! pids dissoc pid)
                              (set-immediate ws-connect)))

                      (swap! pids assoc pid ws)
                      (log 'ws 'added-client pid @pids)
                      )
                    (log 'ws 'error-unexpected-first-message data)
                    ))))
        (log 'ws 'message)
        ))
    (ws-connect)
    ))
