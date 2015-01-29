(ns solsort.tests
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.util :as util]
    [solsort.system :as system]
    [solsort.kvdb :as kvdb]
    [clojure.string :as string :refer [split]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

(defn test-kvdb []
  (go
    (<! (kvdb/store :testdb "hello" "world"))
    (assert (= "world" (<! (kvdb/fetch :testdb "hello"))))))

(defn test-util []
  (go
  (assert (nil? (util/parse-json-or-nil "this is not json")))
  (assert (= (js->clj #js{:hello "world"}) (js->clj (util/parse-json-or-nil "{\"hello\":\"world\"}"))))
  ))

(defn run-tests []
  (go
      (<! (test-kvdb))
      (<! (test-util))
      (system/exit 0)))

(run-tests)
