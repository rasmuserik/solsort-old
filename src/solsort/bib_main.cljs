(ns solsort.bib-main
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.mbox :refer [route]]
    [solsort.system :refer [log ajax]]
    [solsort.platform :refer [XHR is-browser global]]
    [solsort.kvdb :as kvdb]
    [solsort.util :refer [parse-json-or-nil unique-id]]
    [solsort.bib-related :refer [get-related]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

(defn ting [lid bibid]
  (go
    (let [info (js->clj (or (parse-json-or-nil 
                              (<! (ajax (str "https://dev.vejlebib.dk/ting-visual-relation/get-ting-object/" bibid ":" lid)
                                        :jsonp true)))
                            #js[]))]
      {:type "html"
       :html 
       [:div 
        [:div (str info)]]})))

(defn bib [sub & args]
  (log 'bib sub)
  (case sub
    "info" (apply kvdb/fetch :bibinfo args)
    "related" (apply get-related args)
    "ting" (apply ting args)
    (go #js{:unimplemented "bib-fn"})))

(route "bib" bib)
