(ns solsort.test-runner
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.registry :refer [testcases testcase]]
    [solsort.mbox :refer [log route]]
    [solsort.platform :refer [is-browser]]
    [solsort.util :refer [chan?]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

(defn run-tests []
  (go
    (loop [[id f] (first (seq @testcases))
           tests (rest (seq @testcases))]
      (log 'test id)
      (let [v (f)]
        (if (not (if (chan? v) (<! v) v))
          (do
            (log 'test id 'failed)
            (js/console.log "TEST FAIL" (name id))
            (solsort.system.exit 1))))
      (if (first tests)
        (recur (first tests) (rest tests))))
    (log 'test "tests done")
    true
    ))

(route "test-server" 
       (fn []
         (go
           (<! (run-tests))
           (<! (timeout 30000))
           (log 'test 'timeout)
           (solsort.system/exit 1)
           true)))
(route "test-ok" #(solsort.system/exit 0))
(route "test-client"
       (fn []
         (if is-browser
           (go (if (<! (run-tests))
                 (aset js/location "href" "/test-ok"))))
         true))
(route "solsort" 
       #(go (clj->js {:http-headers {:Content-Type "application/javascript"}
                      :content (.readFileSync (js/require "fs") "solsort.js" "utf8")})))
