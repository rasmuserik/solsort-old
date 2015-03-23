(ns solsort.webserver
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.registry :refer [testcase]]
    [solsort.mbox :refer [local-mboxes local-mbox? call local route]]
    [solsort.html :refer [html->http]]
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
    (route "default-route" default-route)
    (defn process-result [result]
      (if (= "html" (:type result))
        (html->http result)
        (clj->js result)))
    (defn handler [req res]
        (go
          (let [t0 (js/Date.now)
                query (.-query req)
                body (.-body req)
                [route & arglist] (.split (.slice (.-path req) 1) #"[/.]")
                arglist (if (< 0 (.-length (js/Object.keys body)))
                          (cons body arglist)
                          arglist)
                [route arglist]
                (if (local-mbox? route) 
                  [route arglist]
                  ["default-route" (into [route] arglist)])
                callback (aget query "callback")
                result (process-result (<! (apply call local route arglist)) )
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
                 ))))

    (defn -start-server []
      (aset global "bodyParser" (js/require "body-parser"))
      (let [express (js/require "express")
            app (express)
            host (or (aget js/process.env "HOST") "localhost")
            port (or (aget js/process.env "PORT") 9999)
            http-server-instance (.createServer (js/require "http") app) ]

        (.use app (.json js/bodyParser))
        (.use app (.urlencoded js/bodyParser #js{"extended" false}))
        (.all app "*" handler)
        (.listen http-server-instance 9999)
        (start-websocket-server http-server-instance)
        (log 'webserver 'starting host port)))
    (set-immediate -start-server)
    (comment end is-nodejs))) 
