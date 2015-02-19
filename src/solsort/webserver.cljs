(ns solsort.webserver
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.registry :refer [testcase routes]]
    [solsort.router :refer [call-raw]]
    [solsort.ws]
    [clojure.string :refer [split]]
    [solsort.util :refer [jsextend parse-json-or-nil]]
    [solsort.system :as system :refer [log is-nodejs set-immediate global read-file-sync]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(comment is-nodejs) 
(log 'is-nodejs is-nodejs)
(if is-nodejs 
  (do 
    ;cljs-bug (go (let [a :ok] (print #js{:bug a} {:no-bug a})))
    (def cached-file (memoize read-file-sync))

    (defn default-route []
      (this-as obj (go
                     (case (aget obj "content-type")
                       "png" #js{:headers #js{:Content-Type "image/png"} :content (cached-file "misc/_default.png")}
                       "gif" #js{:headers #js{:Content-Type "image/gif"} :content (cached-file "misc/_default.gif")}
                       #js{:error "not-implemented"}))))
    (defn handler [route]
      (fn [req res]
        (go
          (let [t0 (js/Date.now)
                argext (.split (.slice (.-path req) (.-length route)) ".")
                query (.-query req)
                body (.-body req)
                args (or (parse-json-or-nil (aget query "args"))
                         (aget body "args")
                         (.filter (.split (aget argext 0) "/") #(< 0 (.-length %))))
                callback (aget query "callback")
                kind (if callback "json" (.join (.slice argext 1) "."))
                f (or (aget routes route) default-route)
                o (clj->js { :content-type kind
                            :client "remote" })
                result (<! (.apply f o args)) 
                headers (aget result "headers")
                ]
            (if (and headers (aget headers "Content-Type") (aget result "content"))
              (do
                (.set res headers)
                (.send res (aget result "content")))
              (do
                (.set res "Content-Type" "application/javascript")
                (.send res 
                       (if callback
                         (str callback "(" (js/JSON.stringify result) ")") 
                         (js/JSON.stringify result)))))
            (log 'web 
                 (.-url req) 
                 (str (- (js/Date.now) t0) "ms")
                 (aget (.-headers req) "x-solsort-remote-addr")
                 (.-body req)
                 )))))

    (defn -start-server []
      (aset global "bodyParser" (js/require "body-parser"))
      (let [express (js/require "express")
            app (express)
            host (or (aget js/process.env "HOST") "localhost")
            port (or (aget js/process.env "PORT") 9999)
            http-server-instance (.createServer (js/require "http") app) ]

        (.use app (.json js/bodyParser))
        (.use app (.urlencoded js/bodyParser #js{"extended" false}))
        (doall (for [k (seq (js/Object.keys routes))]
                 (.all app (str "/" k "*" ) (handler k))))
        (.all app "*" (handler ""))
        (.listen http-server-instance 9999)
        (solsort.ws/start-websocket-server http-server-instance)
        (log 'webserver 'starting host port)))
    (set-immediate -start-server)
    (comment end is-nodejs))) 
