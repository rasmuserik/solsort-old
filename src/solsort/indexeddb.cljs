(ns solsort.indexeddb
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(defn open [id version upgradeFn] 
  (let [c (chan)
        req (.open js/indexedDB id version)]

    (set! (.-onupgradeneeded req)
          (fn [e]
            (set! (.-onerror (.-transaction (.-target e))) #(print "indexeddb upgrade error" id))
            (upgradeFn (.-result (.-target e)))
            true
            ))
    (set! (.-onsuccess req)
          #(put! c (.-result (.-target %))))
    (set! (.-onerror req) #(close! c))
    c))

(defn singleGet [db store id]
  (let [c (chan 1)
        trans (.transaction db #js[store] "readonly")
        objStore (.objectStore trans store)
        req (.get objStore id)]
    (set! (.-onsuccess req) 
          (fn []
            (if (.-result req)
              (put! c (.-result req))
              (close! c))))
    (set! (.-onerror req) #(close! c))
    c))


; todo: for performance reasons have several puts in single transaction 
; make an in-memory cache
(defn singlePut [db store id obj]
  (let [c (chan 1)
        trans (.transaction db #js[store] "readwrite")
        objStore (.objectStore trans store)
        req (.put objStore obj id)]
    (set! (.-onsuccess req) #(put! c true))
    (set! (.-onerror req) #(close! c))
    c))


(defn hello [] 
  (go
    (print "hello")
    (let [db (<! (open "foo" 2 
              (fn [db] 
                (print "creating blah")
                (if (.contains (.-objectStoreNames db) "blah")
                  (.deleteObjectStore db "blah"))
                (.createObjectStore db "blah" #js{:keypath "id"})
                )))]
      (print (<! (singleGet db "blah" "foo")))
      (print (<! (singlePut db "blah" "foo" #js{:id "ho ho"})))
      )
    (print "opened")
    ))

