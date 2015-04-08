(ns solsort.bib-main
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.sys.mbox :refer [route log]]
    [solsort.lib.net :refer [ajax]]
    [solsort.lib.kvdb :as kvdb]
    [solsort.sys.util :refer [parse-json-or-nil unique-id]]
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
