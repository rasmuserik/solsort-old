(ns solsort.bibdata
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.sys.mbox :refer [route log]]
    [solsort.sys.platform :refer [is-browser fs exit is-nodejs]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))


(def sample
  {:title ["Title"]
   :creator ["Creator1" "Creator2"]})

(defn entry [obj]
  (log 'bibentry sample)
  [:div
   [:h1 (first (obj :title))]
   ])

(route "bibdata"
       (fn []
         {:type "html"
          :title "bibdata - solsort.com"
          :css {}
          :html (entry sample)}))
