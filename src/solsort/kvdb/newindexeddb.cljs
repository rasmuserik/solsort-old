(ns solsort.kvdb.newindexeddb
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [cljs.reader :refer [read-string]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(def cache (atom #js{}))
(def next-cache (atom #js{}))
(def writes (atom #js{}))
(def write-channels (atom (list)))
(def reads (atom #js{}))
(def needs-update (chan 1))
(defn add-triple [obj a b c]
  (if (not (aget obj a))
    (aset obj a #js{}))
  (aset (aget obj a) b c))

(defn get-triple [obj a b]
  (let [o (aget obj a)]
    (if o (aget o b))))

(defn do-cache [storage id value]
  (add-triple @cache storage id value)
  (add-triple @next-cache storage id value))

(defn store [storage id value]
  (let [c (chan 1)]
    (add-triple @writes storage id value)
    (do-cache storage id value)
    (swap! write-channels conj c)
    (if (< (count @write-channels) 1000)
      (put! c true))
    (put! needs-update true)
    c))

(defn fetch [storage id]
  (let [result (get-triple @cache storage id)]
    (if result
      (go result)
      (let [c (chan 1)]
        (add-triple @writes storage id c)
        (put! needs-update true)
        c))))

(defn switch-buffers []
  (let [result [[(chan) :store :todo :key1 :val1]
                [(chan) :fetch :todo :key2 nil]]]
    (reset! cache @next-cache)
    (reset! next-cache #js{})
    (reset! writes #js{})
    (reset! write-channels (list))
    (reset! reads #js{})
    (go result)))

(def stores (atom {}))
(def db (atom nil))
(defn open-db []
  (go
    (if @db (.close @db))
    (let [c (chan)
          store-list (seq (read-string (.getItem js/localStorage "keyval-db")))
          req (.open js/indexedDB "keyval-db" (inc (count store-list)))
          ]
      (set! (.-onupgradeneeded req)
            (fn [req]
              (print 'upgrade-needed-start)
              (let [db (.-result (.-target req))]
                (doall (for [store store-list]
                         (if (not (.contains (.-objectStoreNames db) store))
                           (.createObjectStore db store)))))))
      (set! (.-onerror req) #(do
                               (js/console.log 'error %)))
      (set! (.-onsuccess req) 
            (fn [req]
              (reset! db (.-result (.-target req)))
              (close! c)))
      (<! c))))

(defn ensure-store [storage]
  (go
    (if (not (@stores storage)) 
      (let [store-list (read-string (or (.getItem js/localStorage "keyval-db") "#{}"))]
        (swap! stores assoc storage {})
        (.setItem js/localStorage "keyval-db" (str (conj store-list storage)))
        (<! (open-db))))))

(defn commit [actions]
  (let [c (chan 1)
        storages (clj->js (distinct (map (fn [[_ __ store]] store) actions)))
        mode (if (contains? (map (fn [[_ action]] action)) :write) "readwrite" "readonly")
        trans (.transaction @db storages mode)]
    (set! (.-oncomplete trans)  (fn [] (put! c true)))
    (set! (.-onerror trans)  (fn []
                               (print "transaction error") 
                               (close! c)))
    (set! (.-onabort trans)  (fn []
                               (print "transaction abort") 
                               (close! c)))
    (doall (for [storage storages]
             (let [object-storage (.objectStore trans (str storage))
                   storage-actions (filter (fn [[_ store]] (= store storage)))]
               (doall (map (fn [[c action _ k v]]
                             (let [request (if (= action :store)
                                             (.put object-storage v k)
                                             (.get object-storage k))]
                               (set! (.-onsuccess request) 
                                     (fn [] (put! c (.-result request))))))
                           storage-actions)))))
    c))

(def main-loop 
  (go-loop 
    [_ nil]
    (<! needs-update)
    (let [actions (switch-buffers)
          stores (distinct (map (fn [[_ __ store]] store) actions))]
      (loop [store (first stores) stores (rest stores)]
        (if store
          (do
            (<! (ensure-store store))
            (recur (first stores) (rest stores)))))
      (<! (commit actions)))
    (recur [])))
