(ns solsort.bib-main
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.mbox :refer [route]]
    [solsort.system :refer [log global]]
    [solsort.kvdb :as kvdb]
    [solsort.util :refer [parse-json-or-nil]]
    [solsort.bib-related :refer [get-related]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

(if (not (exists? js/XMLHttpRequest))
  (aset global "XMLHttpRequest" (aget (js/require "xmlhttprequest") "XMLHttpRequest")))

(defn ajax [url & {:keys [post-data]}]
  (let [c (chan)
        req (js/XMLHttpRequest.)
        ]
    (.open req (if post-data "POST" "GET") url true)
    (aset req "onreadystatechange"
          (fn []
            (if (= (aget req "readyState") (or (aget req "DONE") 4))
              (let [text (aget req "responseText")]
                (if text
                  (put! c text)
                  (close! c))))))
    (.send req)
    c))

(defn ting [lid bibid]
  (go
    (let [info (js->clj (or (parse-json-or-nil 
                              (<! (ajax (str "https://dev.vejlebib.dk/ting-visual-relation/get-ting-object/" bibid ":" lid))))
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
