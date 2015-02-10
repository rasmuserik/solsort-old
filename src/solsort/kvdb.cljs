(ns solsort.kvdb
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.registry :refer [testcase]]
    [solsort.system :refer [is-browser]]
    [solsort.kvdb.indexeddb :as idb]
    [solsort.kvdb.leveldb :as leveldb]
    [cljs.reader :refer [read-string]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))


(def store (if is-browser idb/store leveldb/store))
(testcase 'store
    #(go (or (<! (store :testdb "hello" "world")) true)))


(def fetch (if is-browser idb/fetch leveldb/fetch))
(testcase 'fetch
    #(go (= "world" (<! (fetch :testdb "hello")))))


(def multifetch (if is-browser idb/multifetch leveldb/multifetch))

(def commit (if is-browser idb/commit leveldb/commit))
