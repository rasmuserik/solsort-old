(ns solsort.example
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.mbox :refer [route]]
    [solsort.system :as system :refer [log is-browser fs source-file exit is-nodejs]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

(defprotocol TestProtocol
  (hello [this]))
(extend-protocol TestProtocol
  string
  (hello [this] (str this "hullo" this)))

(deftype Blah [x]
  TestProtocol
  (hello [this] (str this "huhu" x))
  )
(defrecord Blap [x]
  TestProtocol
  (hello [this] (str this "hihi" x))
  )

(route "hello" 
       (fn [a b]
         {:type "html"
          :html [:div
                 "hello " 
                 (hello "world")
                 (hello (Blah. "foo"))
                 (hello (Blap. "foo"))
                 [:div (str "this is: " a)]
                 [:div [:a {:href "#hello/foo"} "foo"]]
                 [:div [:a {:href "#hello/bar"} "bar"]]
                 ]}))
