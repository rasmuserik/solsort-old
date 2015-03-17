(ns solsort.bib-main
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.registry :refer [testcase route]]
    [solsort.system :refer [log]]
    [solsort.kvdb :as kvdb]
    [solsort.bib-related :refer [get-related]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

(defn bib [sub arg]
  (log 'bib sub)
  (case sub
    "info" (kvdb/fetch :bibinfo arg)
    "related" (get-related arg)
    (go #js{:unimplemented "bib-fn"})))

(route "bib" bib)
