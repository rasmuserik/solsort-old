(ns solsort.hello
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.test :refer [testcase]]
    [solsort.system :refer [exec each-lines is-nodejs log is-browser]]
    [solsort.kvdb :as kvdb]
    [solsort.webserver :as webserver]
    [solsort.util :refer [print-channel kvdb-store-channel by-first transducer-status group-lines-by-first swap-trim transducer-accumulate parse-json-or-nil]]
    [clojure.string :as string :refer [split]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))


(defn form [a]
  [:div {}
   [:h1 {} "Hello " a]
   [:div {:style {:background "red"}} "hello " 
    [:b {} "hullo"] " hi"]
   [:form
    [:input {:id "blah"}]]
   ])

(defn clj->react [o]
  (if (vector? o)
    (if (map? (second o))
      (apply js/React.createElement (name (first o)) (clj->js (second o)) (map clj->react (rest (rest o))))
      (apply js/React.createElement (name (first o)) nil (map clj->react (rest o))))
    (str o)))

(defn start []
  (if is-browser
    (do
      (go
        (loop [i 0]
          (js/React.render (clj->react (form i) ) js/document.body)
          (<! (timeout 100))
          (recur (inc i))
          ) 
      ))))

