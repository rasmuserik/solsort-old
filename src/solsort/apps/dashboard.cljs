(ns solsort.dashboard
    (:require-macros 
           [cljs.core.async.macros :refer [go go-loop alt!]]
           )
    (:require
           [solsort.sys.mbox :as mbox :refer [route log local parent call]]
           [solsort.sys.platform :refer [is-browser fs exit is-nodejs]]
           [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

(defn view []
  (go
    (<! (timeout 1000))
  [:div
   [:h1 "Dashboard"]
   [:div [:b "uccorg:"] (str (<! (call (or @parent local) "uccorg-status")))]
  ]))

(route 
  "dashboard"
  (fn []
    (go
    {:type "html"
     :html (<! (view))})))
