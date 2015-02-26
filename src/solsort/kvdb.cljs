(ns solsort.kvdb
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.registry :refer [testcase]]
    [solsort.system :refer [is-browser ensure-dir]]
    [cljs.reader :refer [read-string]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))


(comment browser/indexeddb)
(if is-browser
  (do
    (def stores (atom {}))
    (def db (atom nil))
    (def locked (atom false))
    (defn lock [id]
      (go
        ;  (print 'locking id)
        (while @locked 
          (<! (timeout 100)))
        ;  (print 'lock id)
        (reset! locked true)))
    (defn unlock [id] 
      ; (print 'unlock id)
      (reset! locked false))
    (defn open-db []
      (go
        (if @db (.close @db))
        (<! (lock 'a))
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
                                   (unlock 'a1)
                                   (js/console.log 'error %)))
          (set! (.-onsuccess req) 
                (fn [req]
                  (unlock 'a2)
                  (reset! db (.-result (.-target req)))
                  (close! c)))
          (<! c))))

    (defn ensure-store [storage]
      (go
        (if (not (@stores storage)) 
          (let [store-list (read-string (or (.getItem js/localStorage "keyval-db") "#{}"))]
            (swap! stores assoc storage {})
            (.setItem js/localStorage "keyval-db" (str (conj store-list storage)))
            (<! (open-db)))
          (while (not @db) (<! (timeout 100))))))

    (defn commit [storage]
      (go 

        (if (< 0 (count (@stores storage)))
          (do
            (<! (lock 'b))
            (let [c (chan 1)
                  trans (.transaction @db #js[storage] "readwrite")
                  objStore (.objectStore trans storage)]
              (doall (for [[k v] (@stores storage)]
                       (.put objStore v k)))
              (set! (.-oncomplete trans)  
                    #(do (unlock 'b1) 
                         (put! c true)))
              (set! (.-onerror trans)  
                    #(do 
                       (unlock 'b2)
                       (print "commit error") 
                       (close! c)))
              (set! (.-onabort trans)  
                    #(do 
                       (unlock 'b3)
                       (print "commit abort") 
                       (close! c)))
              (swap! stores assoc storage {})
              (<! c))))))

    (defn multifetch [storage ids] 
      (go 
        (<! (ensure-store storage))
        (<! (commit storage))
        (<! (lock 'c))
        (let [c (chan)
              result (atom #js{})
              transaction (.transaction @db #js[storage] "readonly")
              object-store (.objectStore transaction storage)]
          (doall (for [id ids]
                   (let [request (.get object-store id)]
                     (set! (.-onsuccess request) 
                           (fn [] (aset @result id (.-result request)))))))
          (set! (.-oncomplete transaction)  
                (fn [] 
                  (put! c @result)))
          (let [return-value (<! c)]
            (unlock 'c)
            return-value)
          )))

    (defn fetch [storage id] 
      (go 
        (aget (or (<! (multifetch storage #js[id])) #js{}) id)
        ))

(defn store [storage id value] 
  (go
    (if (< 1000 (count (@stores storage))) (<! (commit storage)))
    (<! (ensure-store storage))
    (swap! stores assoc storage (assoc (@stores storage) id value))
    value))
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
)


(do (comment leveldb)
  (def dbs (atom {}))
  (defn get-db [id]
    (or (get @dbs id)
        (do
          (ensure-dir "./dbs")
          (get 
            (reset! dbs (assoc @dbs id
                               ((js/require "levelup") 
                                (str "./dbs/" (.replace (str id) #"[^a-zA-Z0-9]" "_") ".leveldb")
                                #js{"valueEncoding" "json"}))) 
            id))))

  (defn commit [storage] (go))
  (defn fetch [storage id] 
    (let [c (chan 1)]
      (.get (get-db storage) 
            id
            (fn [err value]
              (if err
                (close! c)
                (put! c value))))
      c))

  (defn multifetch [storage ids]  
    (let [c (chan 1)
          result #js{}
          cnt (atom (count ids))]
      (if (= 0 @cnt)
        (close! c)
        (doall (for [id ids]
                 (take! (fetch storage id)
                        (fn [value]
                          (aset result id value)
                          (if (<= (swap! cnt dec) 0)
                            (put! c result)))))))
      c))

  (defn store [storage id value]
    (let [c (chan 1)]
      (.put (get-db storage) 
            id
            value
            (fn [err]
              (if err (print 'leveldb-store-error err storage id value))
              (close! c)))
      c))
  ))


(comment new indexed-db

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
)


(testcase 'store
          #(go (or (<! (store :testdb "hello" "world")) true)))
(testcase 'fetch
          #(go (= "world" (<! (fetch :testdb "hello")))))
