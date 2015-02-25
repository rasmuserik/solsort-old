(ns solsort.mbox
  (:require-macros 
    [cljs.core.async.macros :refer [go alt!]])
  (:require
    [cljs.core.async.impl.channels :refer [ManyToManyChannel]]
    [solsort.registry :refer [testcase mbox-incoming mbox-handlers register unregister call-local]]
    [solsort.system :refer [is-nodejs is-browser log set-immediate]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(comment msg handle loop)
(go
  (loop []
    (let [msg (<! mbox-incoming)
          f (@mbox-handlers (aget msg "mbox"))]
 ;     (log 'mbox (aget msg "pid") (aget msg "mbox") (aget msg "info") (keys @mbox-handlers))
      (if f (f msg)))
    (recur)))

