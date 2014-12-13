(ns solsort.keyval-db
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [cljs.reader :refer [read-string]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(def stores (atom {}))

(def db (atom nil))

(defn open-db []
  (go
    (if @db (.close @db))
    ; BUG/WARNING  not multientrant
    (let [c (chan)
          store-list (read-string (.getItem js/localStorage "keyval-db"))
          req (.open js/indexedDB "keyval-db" (inc (count store-list)))
          ]
      (set! (.-onupgradeneeded req)
            #(let [db (.-result (.-target %))]
               (doall (for [store store-list]
                        (if (not (.contains (.-objectStoreNames db) store))
                          (.createObjectStore db store))))))
      (set! (.-onsuccess req) 
            #(do 
               (reset! db (.-result (.-target %)))
               (close! c)))
      (<! c))))
(defn ensure-store [storage]
  (go
    (if (not (@stores storage)) 
      (do
        (swap! stores assoc storage {})
        (.setItem js/localStorage "keyval-db" (str (keys @stores)))
        (<! (open-db))))))

(defn commit [storage]
  (if (< 0 (count (@stores storage)))
    (let [c (chan 1)
          trans (.transaction @db #js[storage] "readwrite")
          objStore (.objectStore trans storage)]
      (doall (for [[k v] (@stores storage)]
               (.put objStore v k)))
      (set! (.-oncomplete trans)  #(put! c true))
      (set! (.-onerror trans)  #(do (print "commit error") (close! c)))
      (swap! stores assoc storage {})
      c)
    (go)))
(defn multifetch [storage ids] 
  (go 
    (<! (commit storage))
    #js{}))
(defn fetch [storage id] 
  (go (aget (<! (multifetch storage #js[id])) id)))
(defn store [storage id value] 
  (go
    (<! (ensure-store storage))
    (swap! stores assoc storage (assoc (@stores storage) id value))
    (print stores)
    ))
(defn tryout []
  (go
    (<! (store :a "foo" "bar"))
    (<! (store :a "blah" "foop"))
    (<! (store :a "quux" "quuz"))
    (<! (store "b" "foo" "bar"))
    (<! (store "b" "bar" "baz"))
    (<! (store "b" "baz" "quux"))
    (print "stored")
    (print (<! (fetch :a "blah")))
    (print (<! (multifetch "b" #js["foo" "bar" "baz"])))))

(def comments-below nil)
(comment





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


  (defn fetch [storage id] (go (aget (<! (multifetch storage #js[id])) id)))

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
  )
