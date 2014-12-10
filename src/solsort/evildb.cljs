(ns solsort.evildb
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(def db (atom nil))
(defn init [] 
  (let [c (chan)]
    (let [req (.open js/indexedDB "evildb" 1)]
      (set! (.-onupgradeneeded req)
            #(let [db (.-result (.-target %))]
               (if (.contains (.-objectStoreNames db) "keyvals")
                 (.deleteObjectStore db "keyvals"))
               (.createObjectStore db "keyvals")))
      (set! (.-onsuccess req) 
            #(do 
               (reset! db (.-result (.-target %)))
               (close! c))))
    c))

(def cache (atom #js{}))
(def cacheCount (atom 0))

(defn fetch [id]
  (let [c (chan 1)]
    (if (aget @cache id)
      (put! c (aget @cache id))
      (let [trans (.transaction @db #js["keyvals"] "readonly")
            objStore (.objectStore trans "keyvals")
            req (.get objStore id)]
        (set! (.-onsuccess req) 
              (fn []
                (if (.-result req)
                  (put! c (.-result req))
                  (close! c))))
        (set! (.-onerror req) #(close! c))))
    c))

(defn commit []
  (let [c (chan 1)
        trans (.transaction @db #js["keyvals"] "readwrite")
        objStore (.objectStore trans "keyvals")]
    (loop [ids (js/Object.keys @cache)]
      (if (< 0 (.-length ids))
        (let [id (.pop ids)]
          (.put objStore (aget @cache id) id)
          (recur ids))))
    (reset! cacheCount 0)
    (reset! cache #js{})
    (set! (.-oncomplete trans)  #(put! c true))
    (set! (.-onerror trans)  #(close! c))
    c))

(defn store [id value]
  (if (< @cacheCount 1000)
    (go
      (aset @cache id value)
      (swap! cacheCount inc))
    (commit)))

