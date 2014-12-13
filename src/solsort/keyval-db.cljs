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
          store-list (seq (read-string (.getItem js/localStorage "keyval-db")))
          req (.open js/indexedDB "keyval-db" (inc (count store-list)))
          ]
      (set! (.-onupgradeneeded req)
            #(let [db (.-result (.-target %))]
               (doall (for [store store-list]
                        (if (not (.contains (.-objectStoreNames db) store))
                          (.createObjectStore db store))))))
      (set! (.-onerror req) #(js/console.log 'error %))
      (set! (.-onsuccess req) 
            #(do 
               (reset! db (.-result (.-target %)))
               (close! c)))
      (<! c))))

(defn ensure-store [storage]
  (go
    (if (not (@stores storage)) 
      (let [store-list (read-string (or (.getItem js/localStorage "keyval-db") "#{}"))]
        (swap! stores assoc storage {})
        (.setItem js/localStorage "keyval-db" (str (conj store-list storage)))
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
    (let [c (chan)
          result (atom #js{})
          transaction (.transaction @db #js[storage] "readonly")
          object-store (.objectStore transaction storage)]
      (doall (for [id ids]
               (let [request (.get object-store id)]
                 (set! (.-onsuccess request) 
                       (fn [] (aset @result id (.-result request)))))))
      (set! (.-oncomplete transaction)  (fn [] (put! c @result)))
      (<! c))))

(defn fetch [storage id] 
  (go (aget (<! (multifetch storage #js[id])) id)))

(defn store [storage id value] 
  (go
    (if (< 1000 (count (@stores storage))) (<! (commit storage)))
    (<! (ensure-store storage))
    (swap! stores assoc storage (assoc (@stores storage) id value))
    ))

(defn tryout []
  (go
    (print 'HERE (seq #js[1 2 3 4]))
    (<! (store :a "foo" "bar"))
    (<! (store :a "blah" "foop"))
    (<! (store :a "quux" "quuz"))
    (<! (store "b" "foo" "bar"))
    (<! (store "b" "bar" "baz"))
    (<! (store "b" "baz" "quux"))
    (print 'HERE2 (seq #js[1 2 3 4]))
    (print "stored")
    (print "A" (<! (fetch :a "blah")))
    (print "B" (<! (multifetch "b" #js["foo" "bar" "baz"])))))
