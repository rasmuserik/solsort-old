(ns solsort.test
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.registry :refer [testcases route]]
    [solsort.system :refer [log]]
    [solsort.util :refer [chan?]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

(defn run-tests []
  (go
    (<! (timeout 1000)) ; wait for system to start up
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
    ))

(route "test" 
       #(go
          (<! (run-tests))
          (solsort.system/exit 0)))
