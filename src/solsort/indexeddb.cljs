(ns solsort.indexeddb
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(defn open [id version] 
  (let [c chan]
    (.log js/console "hereza")
    c))

(defn hello [] 
  (do
    (open "foo" 1)
    ))

