(ns solsort.webserver
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.registry :refer [testcase local-mboxes local-mbox? call-local route]]
    [solsort.html :refer [jsonhtml->http]]
    [solsort.ws :refer [start-websocket-server]]
    [clojure.string :refer [split]]
    [solsort.util :refer [jsextend parse-json-or-nil]]
    [solsort.system :as system :refer [log is-nodejs set-immediate global read-file-sync]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(comment is-nodejs) 
(if is-nodejs 
  (do 
    ;cljs-bug (go (let [a :ok] (print #js{:bug a} {:no-bug a})))
    (def cached-file (memoize read-file-sync))
    (defn default-route [& args]
                     (case (last args)
                       "png" #js{:http-headers #js{:Content-Type "image/png"} :content (cached-file "misc/_default.png")}
                       "gif" #js{:http-headers #js{:Content-Type "image/gif"} :content (cached-file "misc/_default.gif")}
                       #js{:error "not-implemented"}))
    (route :default-route default-route)
    (defn process-result [result]
      (if (= "json-html" (aget result "type"))
        (jsonhtml->http result)
        result))
    (defn handler [route]
      (fn [req res]
        (go
          (let [t0 (js/Date.now)
                query (.-query req)
                body (.-body req)
                path (.slice (.-path req) 1)
                path (if (= (.slice path 0 (.-length route)) route)
                       (.slice path (.-length route))
                       path)
                path (if (= "." (aget path 0)) (.slice path 1) path)
                path (if (= "/" (aget path 0)) (.slice path 1) path)
                arglist (.split path #"[/.]")
                arglist (if (< 0 (.-length (js/Object.keys body)))
                          (cons body arglist)
                          arglist)
                callback (aget query "callback")
                f (if (local-mbox? route) route :default-route)
                result (process-result (<! (apply call-local f arglist)) )
                headers (aget result "http-headers")]
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
        (doall (for [k (local-mboxes)]
                 (.all app (str "/" k "*" ) (handler k))))
        (.all app "*" (handler ""))
        (.listen http-server-instance 9999)
        (start-websocket-server http-server-instance)
        (log 'webserver 'starting host port)))
    (set-immediate -start-server)
    (comment end is-nodejs))) 
