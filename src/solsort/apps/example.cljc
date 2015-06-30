(ns solsort.example
  #?(:cljs
      
    (:require-macros 
           [cljs.core.async.macros :refer [go go-loop alt!]]
          ; [solsort.example :refer [hello-swap]]
           ))
    #?(:cljs
    (:require
           [solsort.sys.mbox :refer [route log]]
           [solsort.sys.platform :refer [is-browser fs exit is-nodejs]]
           [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]])))

#?(:clj (defmacro hello-swap [a b c]
  (a c b)))
#?(:clj (macroexpand (hello-swap str 'a 'b)))

#?(:cljs
    (do
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

  (route "example" 
         (fn [a b]
           {:type "html"
            :html [:div
                   "Does it work? " 
                   (hello "world")
                   (hello (Blah. "foo"))
                   (hello (Blap. "foo"))
                   (hello-swap str "Aaa" "B")
                   [:div (str "this is: " a)]
                   [:div [:a {:href "#hello/foo"} "foo"]]
                   [:div [:a {:href "#hello/bar"} "bar"]]
                   ]}))
))
