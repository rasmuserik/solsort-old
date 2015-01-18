(ns solsort.kvdb.leveldb
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.system :refer [ensure-dir]]
    [cljs.reader :refer [read-string]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

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