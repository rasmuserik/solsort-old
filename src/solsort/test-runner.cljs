(ns solsort.test-runner
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.test :refer [testcases]]
    [solsort.system :refer [log]]
    [solsort.util :refer [chan?]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

(defn start []
  (go
    (loop [[id f] (first (seq @testcases))
           tests (rest (seq @testcases))]
      (log 'test id)
      (let [v (f)]
        (if (not (if (chan? v) (<! v) v))
          (do
            (log 'test id 'failed)
            (solsort.system.exit 1))))
      (if (first tests)
        (recur (first tests) (rest tests))))
    (log 'test "tests done")
    (solsort.system/exit 0)))
