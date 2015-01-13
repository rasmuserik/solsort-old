(ns solsort.kvdb
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.system :refer [browser]]
    [solsort.kvdb.indexeddb :as idb]
    [solsort.kvdb.leveldb :as leveldb]
    [cljs.reader :refer [read-string]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))


(print 'isBrowser browser)
(def store (if browser idb/store leveldb/store))
(def fetch (if browser idb/fetch leveldb/fetch))
(def multifetch (if browser idb/multifetch leveldb/multifetch))
(def commit (if browser idb/commit leveldb/commit))
