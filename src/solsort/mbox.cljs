(ns solsort.mbox
  (:require-macros 
    [cljs.core.async.macros :refer [go alt!]])
  (:require
    [cljs.core.async.impl.channels :refer [ManyToManyChannel]]
    [solsort.registry :refer [testcase mbox-incoming mbox-handlers register unregister register-fn call-local]]
    [solsort.system :refer [is-nodejs is-browser log set-immediate]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(comment msg handle loop)
(go
  (loop []
    (let [msg (<! mbox-incoming)
          f (@mbox-handlers (aget msg "mbox"))]
      (log 'mbox (aget msg "pid") (aget msg "mbox") (aget msg "info"))
      (if f (f msg)))
    (recur)))

(def handle register)
(def unhandle unregister)
(def call call-local)
