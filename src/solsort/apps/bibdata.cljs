(ns solsort.bibdata
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.sys.mbox :refer [route log]]
    [solsort.lib.kvdb :refer [fetch store]]
    [solsort.lib.net :refer [ajax]]
    [solsort.sys.platform :refer [is-browser is-nodejs read-file-sync]]
    [solsort.sys.util :refer [parse-json-or-nil run-once]]
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

(def process 
  (run-once
    (fn []
      (go
        (log 'bibdata 'processing-data)
        (let [lids (seq (.split (str (read-file-sync "misc/lids")) "\n"))]
          (loop [lid (first lids)
                 lids (rest lids)]
            (when (not (<! (fetch :bibdata lid)))
              (let [url (str 
                          "https://dev.vejlebib.dk/ting-visual-relation/get-ting-object/870970-basis:" 
                          lid)
                    data (js->clj (parse-json-or-nil (<! (ajax url))))
                    data 
                    (reduce 
                      (fn [mm [k v]]
                        (if (mm k)
                          (assoc mm k (conj (mm k) v))
                          (assoc mm k [v])))

                      {}
                      (for [{p "property" v "value"} data] [p v]))
                    ]
                (log 'bib-data 'update data)
                (when (data "isbn")
                  (doall (map #(store :isbn % lid) (data "isbn"))))
                (<! (store :bibdata lid (clj->js data)))))
            (when (rest lids)
              (recur (first lids) (rest lids)))))))))

(route "bibdata-process" process)

