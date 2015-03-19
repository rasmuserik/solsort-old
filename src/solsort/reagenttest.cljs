(ns solsort.reagenttest
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.mbox :refer [route]]
    [solsort.system :refer [is-browser]]
    [reagent.core :as reagent]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))


(def html
  [:div
   [:h1 "hello blah"]
   [:div.foo.bar "class" [:small "test"]]])

(defn reagenttest []
  (js/console.log "reagent-test" (reagent/render-to-static-markup html))
  (if is-browser
  (reagent/render html (.-body js/document)))
  {:type "html"
   :title "hello"
   :html html})

(route "reagenttest" reagenttest)
