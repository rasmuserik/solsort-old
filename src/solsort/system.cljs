(ns solsort.system
  (:require-macros 
    [solsort.system :refer [defapi]]
    [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.registry :refer [testcase pid]]
    [clojure.string :as string]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))
(comment enable-print)
(enable-console-print!)
(declare log)


(def route solsort.registry/route)
(def global 
  (cond
    (exists? js/window) js/window
    (exists? js/global) js/global
    (exists? js/self) js/self
    :else ((fn [] js/this))))
(aset global "window" global)


(def is-browser (and (exists? js/window) (exists? js/window.document)))
(def is-nodejs (and
                 (exists? js/global)
                 (.hasOwnProperty js/global "process")
                 (.hasOwnProperty js/global.process "title")))

(def is-worker (and (not is-nodejs) (not is-browser)))
(def is-server (and (not is-browser) (not is-worker)))
(def hostname (if is-nodejs (.hostname (js/require "os")) "browser"))
(def pid pid)

(def source-file 
  (cond
    (exists? js/__filename) js/__filename 
    (and (exists? js/location) (= "file" (.slice js/location.href 0 4))) "solsort.js" 
    :else "/solsort.js"))



(def origin (if is-nodejs "http://localhost:9999" js/location.origin))

(def XHR (if is-nodejs (aget (js/require "xmlhttprequest") "XMLHttpRequest") js/XMLHttpRequest))

(route "xhr-test" (fn [arg] (go (log 'xhr-test arg) (str "hi " arg))))
(testcase 'xhr
          (fn []
            (let [c (chan)
                  xhr (XHR.)
                  json (js/JSON.stringify #js{:args #js["world"]})]
              (.open xhr "POST" (str origin "/xhr-test") true)
              (set! (.-onload xhr) 
                    (fn []
                      (put! c (= (js/JSON.parse (.-responseText xhr)) "hi world"))))
              (set! (.-onerror xhr) #(close! c))
              (.setRequestHeader xhr "Content-Type" "application/json")
              (.send xhr json) 
              c)))


(def fs (if is-nodejs (js/require "fs")))
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
(defn ensure-dir [dirname]
  (if (not (.existsSync fs dirname)) (.mkdirSync fs dirname)))
(defn exec [cmd]
  (let [c (chan)]
    (.exec (js/require "child_process") cmd
           (fn [err stdout stderr]
             (if (= err nil)
               (put! c stdout)
               (close! c)
               )))
    c))


(comment window-React-Worker-etc)
(if (and is-nodejs (not is-browser)) 
  (do 
    (aset global "Worker" (aget (js/require "webworker-threads") "Worker"))
    (aset global "React" (js/require "react"))))
(testcase 'react
          #(= "<h1>Hello</h1>"
              (.renderToStaticMarkup
                js/React
                (.createElement
                  js/React
                  "h1" nil "Hello"))))


(def set-immediate ; "execute function immediately after event-handling"
  (if (exists? js/setImmediate)
    js/setImmediate ; node.js and IE (IE might be buggy)
    (fn [f] (js/setTimeout f 0))))

(defn exit [errcode]
  (go
    (<! (timeout 300))
    (log 'system 'exit errcode)
    (if is-nodejs
      (js/process.exit errcode))))



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



(log 'system 'boot (str (if is-nodejs "node") (if is-browser "browser")) hostname source-file)

;(defapi server server-pid [] pid)
;(log 'server-pid (server-pid))
