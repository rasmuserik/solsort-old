(ns solsort.system
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [clojure.string :as string]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(enable-console-print!)

(def browser (exists? js/window))
(def global (if browser js/window js/global)) ; various conditional global assignments below
(if (not browser) (aset global "window" global))
(defn exec [cmd]
  (let [c (chan)]
    (.exec (js/require "child_process") cmd
           (fn [err stdout stderr]
             (if (= err nil)
               (put! c stdout)
               (close! c)
               )))
    c))

(defn each-lines [filename]
  (let
    [c (chan 1)
     buf (atom "")
     stream (.createReadStream (js/require "fs") filename)]
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
(def nodejs (and
              (exists? js/global)
              (.hasOwnProperty js/global "process")
              (.hasOwnProperty js/global.process "title")
              (= js/global.process.title "node")))
(def pid (if nodejs js/process.pid (bit-or 0 (+ 65536 (* (js/Math.random) (- 1000000 65536))))))
(defn set-immediate [f] "execute function immediately after event-handling"
  (if (exists? js/setImmediate)
    js/setImmediate ; node.js and IE (IE might be buggy)
    (fn [f] (js/setTimeout f 0))))

(set-immediate #(print 'isBrowser browser))

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
(def fs (if nodejs (js/require "fs")))
(defn ensure-dir [dirname]
  (if (not (.existsSync fs dirname)) (.mkdirSync fs dirname)))
(defn log [& args]
  (let [msg (string/join " " (concat
                               [(six-digits pid)
                                (timestamp-string)]
                               (map pr-str args)))
        date (date-string)
        logpath "logs/"
        logname (str logpath date ".log")]
    (if nodejs
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
(log 'solsort-start)
