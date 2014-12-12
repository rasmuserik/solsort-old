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

(defn multifetch [storage ids]
  (let [c (chan 1)]
    (go
      (<! (commit))
      (let [result (atom #js{})
            trans (.transaction @db #js["keyvals"] "readonly")
            objStore (.objectStore trans "keyvals")]
        (loop [i 0]
          (if (< i (.-length ids))
            (let [id (aget ids i)
                  req (.get objStore (str storage ":" id))]
              (set! (.-onsuccess req) #(if (.-result req) (aset @result id (.-result req))))

              (recur (inc i)))))
        (set! (.-oncomplete trans)  #(put! c @result)))
      (<! c))))




(defn fetch [storage id]
  (let [c (chan 1)
        id (str storage ":" id)]
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
        (let [id (.pop ids)
              req (.put objStore (aget @cache id) id)]
          (set! (.-onerror trans)  
                (fn [e]
                  (print e)
                  (close! c)))
          (recur ids))))
    (reset! cacheCount 0)
    (reset! cache #js{})
    (set! (.-oncomplete trans)  #(put! c true))
    (set! (.-onerror trans)  #(close! c))
    c))

(defn store [storage id value]
  (aset @cache (str storage ":" id) value)
  (swap! cacheCount inc)
  (if (< 1000 @cacheCount)
    (commit)
    (go)))

