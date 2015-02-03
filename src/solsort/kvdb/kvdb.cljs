(ns solsort.kvdb
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.test :refer [testcase]]
    [solsort.system :refer [browser]]
    [solsort.kvdb.indexeddb-old :as idb]
    [solsort.kvdb.leveldb :as leveldb]
    [cljs.reader :refer [read-string]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(def store (if browser idb/store leveldb/store))
(testcase 'store
    #(go (or (<! (store :testdb "hello" "world")) true)))
(def fetch (if browser idb/fetch leveldb/fetch))
(testcase 'fetch
    #(go (= "world" (<! (fetch :testdb "hello")))))
(def multifetch (if browser idb/multifetch leveldb/multifetch))
(def commit (if browser idb/commit leveldb/commit))
