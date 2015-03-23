(ns solsort.bib-main
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.mbox :refer [route]]
    [solsort.system :refer [log global is-browser]]
    [solsort.kvdb :as kvdb]
    [solsort.util :refer [parse-json-or-nil unique-id]]
    [solsort.bib-related :refer [get-related]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

(if (not (exists? js/XMLHttpRequest))
  (aset global "XMLHttpRequest" (aget (js/require "xmlhttprequest") "XMLHttpRequest")))

(defn -call-jsonp [url]
  (let [c (chan)
        id (unique-id)]
    (aset global id 
          (fn [o]
            (log 'call-jsonp id o)
            (if o 
              (put! c (js/JSON.stringify o))
              (close! c))
            (goog.object.remove global id)))
    (let [tag (js/document.createElement "script")]
      (aset tag "src" (str url id))
      (js/document.head.appendChild tag))
    c))

(defn ajax [url & {:keys [post-data CORS jsonp]}]
  (if (and jsonp is-browser)
    (-call-jsonp (str url "?callback="))
  (let [c (chan)
        req (js/XMLHttpRequest.)
        ]
    (.open req (if post-data "POST" "GET") url true)
    (if CORS (aset req "withCredentials" true))
    (aset req "onreadystatechange"
          (fn []
            (if (= (aget req "readyState") (or (aget req "DONE") 4))
              (let [text (aget req "responseText")]
                (if text
                  (put! c text)
                  (close! c))))))
    (.send req)
    c)))

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
