(ns solsort.example
    (:require-macros 
           [cljs.core.async.macros :refer [go go-loop alt!]]
          ; [solsort.example :refer [hello-swap]]
           )
    (:require
           [solsort.sys.mbox :as mbox :refer [route log]]
           [solsort.sys.platform :refer [is-browser fs exit is-nodejs]]
           [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

;(events "hello" (fn [state kvs] [state kvs]))
;(render "hello" (fn [state] [:div "hello"]))
