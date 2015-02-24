(ns solsort.rasmuserik
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.registry :refer [route]]
    [solsort.mbox :refer [handle]]
    [solsort.system :as system :refer [log is-browser fs source-file exit is-nodejs]]
    [solsort.router :refer [call-raw]]
    [solsort.test :refer [run-tests]]
    [solsort.ws :refer [broadcast]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))


(defn rasmuserik-html []
  [:div {:style {:text-align :center}}
   [:div {:style {:margin "32px 0 64px 0" :font-size 16}}
    [:img {:src "/icons/solsort.png"
           :style {:height 64 :width 64}}]
    [:div
     [:span {:style {:font-size "150%"}} 
      " solsort.com "]
     "ApS"]
    [:div
     "Open Source • Agile • Full Stack • ClojureScript"]
    [:div {:style {:font-size "300%" :margin "0.5ex 0 1ex 0"}}
     "HTML5 Apps &\xa0Backend"]
    [:div
     "kontakt: Rasmus Erik Voel Jensen" [:br]
     "+45 60703081 hej@solsort.com"
     ]
    ]
   ])

(route "rasmuserik"
       (fn []
         (go
           (clj->js {:type "json-html"
            :title "Rasmus Erik"
            :json-html (rasmuserik-html)}))))
