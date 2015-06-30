(ns solsort.hello
    (:require-macros
           [cljs.core.async.macros :refer [go go-loop alt!]]
          ; [solsort.example :refer [hello-swap]]
           )
    (:require
           [solsort.sys.mbox :refer [route log]]
           [solsort.sys.platform :refer [is-browser fs exit is-nodejs]]
           [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

(route
  "hello"
  (fn []
    (log 'hello 'here)
    {:type "html"
     :html
     [:form
       [:input {:name "hello"}]
       [:textarea {:name "here"}]
       [:select {:name "hoo"}
        [:option {:value "a"} "a"]
        [:option {:value "n"} "n"]
        [:option {:value "b"} "b"]]
       [:input {:name "blah"}]]}))
;(events "hello" (fn [state kvs] [state kvs]))
;(render "hello" (fn [state] [:div "hello"]))
