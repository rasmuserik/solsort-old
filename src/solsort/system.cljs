(ns solsort.system
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.test :refer [testcase]]
    [clojure.string :as string]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))
(comment enable-print)
(enable-console-print!)
(def is-browser (and (exists? js/window) (exists? js/window.document)))
(def global 
  (cond
    (exists? js/window) js/window
    (exists? js/global) js/global
    (exists? js/self) js/self
    :else ((fn [] js/this))))

(defn exec [cmd]
  (let [c (chan)]
    (.exec (js/require "child_process") cmd
           (fn [err stdout stderr]
             (if (= err nil)
               (put! c stdout)
               (close! c)
               )))
    c))

(def is-nodejs (and
              (exists? js/global)
              (.hasOwnProperty js/global "process")
              (.hasOwnProperty js/global.process "title")))
(def fs (if is-nodejs (js/require "fs")))
(def pid (if is-nodejs js/process.pid (bit-or 0 (+ 65536 (* (js/Math.random) (- 1000000 65536))))))
(def hostname (if is-nodejs (.hostname (js/require "os")) "browser"))
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
(comment window-React-Worker-etc)
(if (and is-nodejs (not is-browser)) 
  (do 
    (aset global "Worker" (aget (js/require "webworker-threads") "Worker"))
    (aset global "React" (js/require "react"))))
(if (not is-browser) (aset global "window" global))
(testcase 'react
          #(= "<h1>Hello</h1>"
              (.renderToStaticMarkup
                js/React
                (.createElement
                  js/React
                  "h1" nil "Hello"))))
(defn set-immediate [f] "execute function immediately after event-handling"
  (if (exists? js/setImmediate)
    js/setImmediate ; node.js and IE (IE might be buggy)
    (fn [f] (js/setTimeout f 0))))

(defn two-digits [n] (.slice (str (+ (mod n 100) 300)) 1))
(defn three-digits [n] (.slice (str (+ (mod n 1000) 3000)) 1))
(defn six-digits [n] (.slice (str (+ (mod n 1000000) 3000000)) 1))
(defn date-string []
  (let [now (js/Date.)]
    (string/join "" (map two-digits [(.getUTCFullYear now) (inc (.getUTCMonth now)) (.getUTCDate now)]))))
(defn time-string []
  (let [now (js/Date.)]
    (string/join "" (map two-digits [(.getUTCHours now) (.getUTCMinutes now) (.getUTCSeconds now)]))))
(defn timestamp-string []
  (str (date-string) "-" (time-string) "." (three-digits (.now js/Date))))
(def logfile-name (atom nil))
(def logfile-stream (atom nil))
(def fs (if is-nodejs (js/require "fs")))
(defn ensure-dir [dirname]
  (if (not (.existsSync fs dirname)) (.mkdirSync fs dirname)))
(defn log [& args]
  (let [msg (string/join " " (concat
                               [(six-digits pid)
                                (timestamp-string)]
                               (map pr-str args)))
        date (date-string)
        logpath "logs/"
        logname (str logpath hostname "-" date ".log")]
    (if is-nodejs
      (do
        (if (not (= @logfile-name logname))
          (do
            (if @logfile-stream
              (let [oldname @logfile-name]
                (.on @logfile-stream "close" (exec (str "xz -9 " oldname)))
                (.end @logfile-stream)))
            (ensure-dir logpath)
            (reset! logfile-stream (.createWriteStream fs logname #js{:flags "a"}))
            (reset! logfile-name logname)))
        (.write @logfile-stream (str msg "\n"))))
    (.log js/console msg)))

(defn exit [errcode]
  (go
    (<! (timeout 1000))
    (log 'system 'exit errcode)
    (if is-nodejs
      (js/process.exit errcode))))

(comment log application start)
(def source-file 
  (cond
    (exists? js/__filename) js/__filename 
    (and (exists? js/location) (= "file" (.slice js/location.href 0 4))) "solsort.js" 
    :else "/solsort.js"))
(def is-worker (and (not is-nodejs) (not is-browser)))
(log 'system 'boot (str (if is-nodejs "node") (if is-browser "browser")) hostname source-file)
(defn dev-server []
  (if is-nodejs (.watch fs source-file (memoize (fn [] (log 'system 'source-change 'restarting) (exit 0))))))
