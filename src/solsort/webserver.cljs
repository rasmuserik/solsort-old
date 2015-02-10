(ns solsort.webserver
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.registry :refer [testcase]]
    [clojure.string :refer [split]]
    [solsort.system :as system :refer [log]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(def initialised (atom false))
(def services (atom {:default #(go nil)}))
(defn path-split [string]
  (try
  (let [[full-path param] (.split string "?")
        path-parts (filter #(< 0 (.-length %)) (seq (.split full-path "/")))
        params (into {} (map #(split % #"=" 2) (split param "&")))
        ]
    [(butlast path-parts) (last path-parts) params])
  (catch :default e [#js["bad-req"] (str string ".error") {}])))

(defn cookie [req res] "TODO"
  (let [cookies (or (aget (.-headers req) "cookie") "")
        xzcookie (re-matches #"xz=[0-9]*" cookies)
        _ (print 'xzcookie xzcookie cookies)
        xz (.slice (str "" (js/Math.random)) 2) 
        max-age (* 350 24 60 60)
        cookie (str "xz=" xz ";Max-Age=" max-age ";Path=/;Domain=.solsort.com")]
    (.setHeader res "Set-Cookie" cookie)))

(defn read-file [filename]
  (log 'web 'caching filename)
  (.readFileSync (js/require "fs") filename))
(def cached-file (memoize read-file))
(defn http-serve [req res]
  ;  (cookie req res)
  (go
    (let [[path id params] (path-split (.-url req))
          f (or (@services (first path)) 
                (@services :default))
          split-pos (.lastIndexOf id ".")
          filename (if (< 0 split-pos) (.slice id 0 split-pos) id)
          extension (if (< 0 split-pos) (.slice id (inc split-pos)) "")
          info {:path path :filename filename :extension extension :params params}
          t0 (js/Date.now)
          content (<! (f info))
          ]
      (if (nil? content)
        (do
          (log 'web 404 path id)
          ;(.writeHead res 404)
          ;(.end res)
          (.setHeader res "Content-Type" "application/javascript")
          (.end res "{error:'not implemented'}")
          )
      (case extension
        "png" 
        (do
          (.setHeader res "Content-Type" "image/png")
          (.end res (cached-file "misc/_default.png")))
        "gif" 
        (do
          (.setHeader res "Content-Type" "image/gif")
          (.end res (cached-file "misc/_default.gif")))
        (do
          (.setHeader res "Content-Type" "application/javascript")
          (.end res (str (params "callback") "(" (js/JSON.stringify (clj->js (<! (f info)))) ")")))))
      (log 'web 
           (.-url req) 
           (str (- (js/Date.now) t0) "ms")
           (aget (.-headers req) "x-solsort-remote-addr"))
      ;(.log js/console req)
      )))

(defn start-server []
  (if (not system/is-nodejs)
    (throw "error: not on node"))
  (go
    (let [c (chan)
          http (js/require "http")
          server (.createServer http http-serve)
          port (or (aget js/process.env "PORT") 9999)
          host (or (aget js/process.env "HOST") "localhost") ]
      (.listen server port host)
      (log (str "starting server on " host ":" port)))))

(defn add [path f]
  (if (not @initialised)
    (do
      (reset! initialised true)
      (start-server)))
  (go
    (swap! services assoc path f)
    true))
