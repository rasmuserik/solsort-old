(ns solsort.lib.net
  (:require-macros 
    [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.sys.test :refer [testcase]]
    [solsort.sys.mbox :refer [post local msg log processes]]
    [solsort.sys.platform :refer [is-nodejs is-browser set-immediate XHR global]]
    [solsort.sys.util :refer [unique-id]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))


;; WebSocket connections
(def pids (atom {}))
(defn broadcast [mbox data]
  (doseq [pid (keys @pids)]
    (post pid mbox data)))
(defn send-message [msg] 
  (.send (@pids (aget msg "pid")) (js/JSON.stringify msg)))


(when is-nodejs
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
      (swap! processes dissoc id)
      (swap! pids dissoc id)))
  (defn start-websocket-server [http-server]
    (log 'ws 'start)
    (let [ws (js/require "ws")
          wss (ws.Server. #js{:server http-server})]
      (.on wss "connection" 
           (fn [ws]
             (log 'ws 'incoming-connection ws)
             (.send ws (js/JSON.stringify  #js{:pid local}))
             (.on ws "message" 
                  (fn [data flags]
                    (let [data (js/JSON.parse data)
                          pid (aget data "pid") ]
                      (if pid
                        (do
                          (.removeAllListeners ws "message")
                          (.on ws "message" (handle-message pid))
                          (.on ws "close" (close-connection pid))
                          (swap! processes assoc pid send-message)
                          (swap! pids assoc pid ws)
                          (log 'ws 'added-client pid @pids)
                          )
                        (log 'ws 'error-unexpected-first-message data)
                        ))))
             ))
      ))
  )


(when is-browser
  (comment keep-alive loop)
  (go 
    (loop []
      (<! (timeout 55000))
      (broadcast "keep-alive" nil)
      (recur)))

  (def socket-server ;: url for websocket server
    (if (= -1 (.indexOf js/location.origin "solsort"))
      (if (= "http" (.slice js/location.origin 0 4))
        (str (.replace js/location.origin #"https?" "ws") "/ws/")
        "ws://ws.solsort.com/ws/")
      "ws://ws.solsort.com/ws/"
      ))

  (defn ws-connect []
    (log 'ws 'connect)
    (let
      [ws (js/WebSocket. socket-server)]
      (aset ws "onopen" (fn [e] (.send ws (js/JSON.stringify #js{:pid local}))))
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
              (log 'ws 'message)
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
                            (swap! processes dissoc pid)
                            (set-immediate ws-connect)))

                    (swap! pids assoc pid ws)
                    (swap! processes assoc pid send-message)
                    (log 'ws 'added-client pid @pids)
                    )
                  (log 'ws 'error-unexpected-first-message data)
                  ))))
      ))
  (set-immediate ws-connect))



;; http-client/AJAX
(defn ajax [url & {:keys [post-data CORS jsonp]}]
  (if (and jsonp is-browser)
    (let [url (str url "?callback=")
          c (chan)
          id (unique-id)]
      (aset global id
            (fn [o]
              (if o 
                (put! c (js/JSON.stringify o))
                (close! c))
              (goog.object.remove global id)))
      (let [tag (js/document.createElement "script")]
        (aset tag "src" (str url id))
        (js/document.head.appendChild tag))
      c)
    (let [c (chan)
          req (XHR.)]
      (.open req (if post-data "POST" "GET") url true)
      (when CORS (aset req "withCredentials" true))
      (aset req "onreadystatechange"
            (fn []
              (when (= (aget req "readyState") (or (aget req "DONE") 4))
                (let [text (aget req "responseText")]
                  (if text
                    (put! c text)
                    (close! c))))))
      (.send req)
      c)))

