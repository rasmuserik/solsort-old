(ns solsort.mbox
  (:require-macros 
    [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.registry :refer [testcase]]
    [solsort.system :refer [is-nodejs is-browser log pid set-immediate]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))


(def incoming (chan))
(def handlers (atom {}))

(go
  (loop []
    (let [msg (<! incoming)
          f (@handlers (aget msg "type"))]
      (log 'mbox msg)
      (if f
        (f msg)
        (log 'mbox 'unhandled msg)))
    (recur)))

(defn handle [msg-type f]
  (swap! handlers assoc msg-type f))
