(ns solsort.bibdata
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.sys.mbox :refer [route log]]
    [solsort.lib.kvdb :refer [fetch store]]
    [solsort.lib.net :refer [ajax]]
    [solsort.apps.bib-related :refer [get-related]]
    [solsort.sys.platform :refer [is-browser is-nodejs read-file-sync]]
    [solsort.sys.util :refer [parse-json-or-nil run-once]]
    [clojure.string :as string]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))


(def sample
  {:title ["Title"]
   :creator ["Creator1" "Creator2"]})


(defn go<!-seq [cs]
  (go
    (loop [acc []
           cs cs]
      (if (first cs)
        (recur (conj acc (<! (first cs)))
               (rest cs))
        acc))))

(defn related-link [lid]
  (go
    (let [o (js->clj (<! (fetch :bibdata lid)))]
      (log 'related-link o)
      (if o
        [:li
      [:a {:href (str "/bibdata/lid/" lid)}
       (first (or (o "title") [""]))
       (conj (into [:span " ("]
             (interpose " & " (or (o "creator") [])))
       ")")
       ]]
      nil))))


(defn html-for-type [[k vs o]]
  (go
    (case k
      "title" [:h1 (first vs)]
      "creator" (into [:h2 "af "] (interpose " & " vs))
      "classification" [:div "DK5: " (string/join " & " vs)]
      "type" [:div "type: " (first vs)]
      "date" [:div (first (o "type")) " udgivet: " (string/join " & " vs)]
      "isbn" [:div "ISBN: " (first vs)]
      "lid" [:a {:href (str "http://bibliotek.dk/linkme.php?rec.id=870970-basis:" (first vs))}
             "bibliotek.dk"]
      "related" [:div.spaceabove "Related:" (into [:ul]
        (<! (go<!-seq (map related-link (take 100 (rest vs))))))]
      [:div k (str vs)])))

(defn entry [lid]
  (go
    (let [obj (or (js->clj (<! (fetch :bibdata lid))) {})
          obj (conj obj ["lid" [lid]])
          obj (conj obj ["related" (map 
                                     #(% "lid")
                                     (js->clj (<! (get-related lid))))])
          ks (filter obj ["title" "creator" "classification" "date"
                          ;"serieTitle"
                          "isbn" "lid" "related"])
          ]
      (log 'bibentry obj)
      {:type "html"
       :title (str (first (obj "title"))
                   " "
                   (seq (obj "creator"))
                   " bibdata - solsort.com")
       :css {"body" {"margin" "5%"}
             ".spaceabove" {"margin-top" "1ex"}
             "ul" {"margin-top" "0"}}
       :html [:div
              (into [:div]
                    (filter identity
                    (<! (go<!-seq
                          (map html-for-type
                               (map #(list % (obj %) obj) ks))))))
              ;[:hr]
              ;[:div (string/join " " ks)]
              ;[:div (string/join " " (keys obj))]
              ;[:div (str (js->clj related))]
              ;[:div (str obj)]
              ]})))

(route "bibdata"
       (fn [kind id] 
         (go
           (log 'bibdata kind id)
           (case kind
             "isbn" (<! (entry (<! (fetch :isbn id))))
             "lid" (<! (entry id))
             nil))))
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

