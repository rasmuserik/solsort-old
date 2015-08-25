(ns solsort.bibdata
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.sys.mbox :refer [route log]]
    [solsort.sys.util :refer [go<!-seq]]
    [solsort.lib.kvdb :refer [fetch store]]
    [solsort.lib.net :refer [ajax]]
    [solsort.apps.bib-related :refer [get-related]]
    [solsort.sys.platform :refer [is-browser is-nodejs read-file-sync]]
    [solsort.sys.util :refer [parse-json-or-nil run-once]]
    [clojure.string :as string]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))


(defn bibobj [lid]
  (go
    (let [o (js->clj (<! (fetch :bibdata lid)))]
      (or o
        (let [url (str
                    "https://dev.vejlebib.dk/ting-visual-relation/get-ting-object/870970-basis:"
                    lid)
              data (js->clj (or (parse-json-or-nil (<! (ajax url))) []))
              data
              (reduce
                (fn [mm [k v]]
                  (if (mm k)
                    (update-in mm [k] conj v)
                    (assoc mm k [v])))

                {}
                (for [{p "property" v "value"} data] [p v]))
              ]
          (log 'bib-data 'update url data)
          (when (data "isbn")
            (doall (map #(store :isbn % lid) (data "isbn"))))
          (<! (store :bibdata lid (clj->js data)))
          data
          )))))

(def sample
  {:title ["Title"]
   :creator ["Creator1" "Creator2"]})


(defn related-link [lid]
  (go
    (let [o (<! (bibobj lid))]
      (when o
        [:li
         [:a {:href (str "/bibdata/lid/" lid)
              }
          (first (or (o "title") [""]))
          (conj (into [:span " ("]
                      (interpose " & " (or (o "creator") [])))
                ")")
          ]]))))


(def biblioteker
  [["bibliotek.dk" "http://bibliotek.dk/linkme.php?rec.id=870970-basis:"]
   ;["Horsens" "https://horsensbibliotek.dk/ting/object/870970-basis:"]
   ;["Vejle" "https://vejlebib.dk/ting/object/870970-basis:"]
   ])

(defn html-for-type [[k vs o]]
  (go
    (case k
      "title" [:h1 {:itemProp "name"} (first vs)]
      "creator" (into [:h2 "af "]
                      (interpose
                        " & "
                        (map (fn [v] [:span {:itemProp "creator"} v])
                             vs)))
      "date" [:div (first (o "type")) " udgivet " [:span {:itemProp "datePublished"} (first vs)]]
      "classification" [:div "DK5: " (string/join " & " vs)]
      "type" [:div "type: " (first vs)]
      "isbn" [:div "ISBN: " [:span {:itemProp "isbn"} (first vs)]]
      "lid" (into [:div.spaceabove "links: "
                   (if (o "isbn")
                     (let [isbn (first (o "isbn"))]
                     [:span
                      [:a {:href (str "http://www.worldcat.org/isbn/" isbn)
                           :itemProp "sameAs"} "WorldCat"] " "
                      [:a {:href (str "http://www.bogpriser.dk/Search/Result?isbn=" isbn)} "bogpriser.dk" ] " "
                      [:a {:href (str "https://books.google.dk/books?vid=ISBN" isbn)
                           :itemProp "sameAs"} "GoogleBøger"] " "])
                     " ")]
                  (interpose " "
                             (map (fn [[bib url]]
                                    [:a {:href (str url (first vs))
                                         :itemProp "sameAs"}
                                     bib])
                                  biblioteker)
                             ))
      "related" [:div.spaceabove "Anbefalinger: " (into [:ul]
                                                   (<! (go<!-seq (map related-link (take 30 (rest vs))))))]
      [:div k (str vs)])))

(defn itemtype [t]
  (str "http://schema.org/"
       (case (first t)
         "Bog" "Book"
         "Billedbog" "Book"
         "Dvd" "Movie"
         "Tidskriftasaf" "Article"
         (do
           (log 'bibdata 'warning-missing-itemtype t)
           "CreativeWork"))))
(defn entry [lid]
  (go
    (let [obj (or (<! (bibobj lid)) {})
          obj (conj obj ["lid" [lid]])
          obj (conj obj ["related" (map
                                     #(% "lid")
                                     (js->clj (<! (get-related lid))))])
          ks (filter obj ["title" "creator" "date" "classification"
                          ;"serieTitle"
                          "isbn" "lid" "related"])
          ]
      {:type "html"
       :title (str (first (obj "title"))
                   " "
                   (seq (obj "creator"))
                   " - bibdata - solsort.com")
       :css {"body" {"margin" "5%"}
             ".spaceabove" {"margin-top" "1ex"}
             "ul" {"margin-top" "0"}}
       :html [:div
              (concat [:div {:itemScope "itemscope"
                           :itemType (itemtype (obj "type"))}]
                    (filter identity
                            (<! (go<!-seq
                                  (map html-for-type
                                       (map #(list % (obj %) obj) ks)))))
                    [[:hr]
                     [:div [:small
                            "Dette er et eksperiment med at lægge data om bøger online med semantisk opmarkering. Grunddata er en del af de nationalbibliografiske data som Kulturstyrelsen og Kulturministeriet stiller til fri brug. Anbefalingerne er baseret på lånstatistik som DBC frigjorde på hackathonen Hack4DK. Dette site, kildekode og anbefalingsalgoritme er lavet af solsort.com" ]]]
                    )
              ;[:hr]
              ;[:div (string/join " " ks)]
              ;[:div (string/join " " (keys obj))]
              ;[:div (str (js->clj related))]
              ;[:div (str obj)]
              ]})))

(def sample-lids
  ["28511663" "28902239" "27999441" "27541062" "25862031"
   "20411724" "23917076" "29541167" "20476079" "29815860"
   "27594506" "25523911" "07203659" "44764873"])
(defn sample-lid [lid]
  (go
    [:li [:a {:href (str "/bibdata/lid/" lid)} lid]
     " " (first ((<! (bibobj lid)) "title"))]))

(defn default []
  (go
    {:type "html"
     :title " bibdata - solsort.com"
     :css {"body" {"margin" "5%"}
           ".spaceabove" {"margin-top" "1ex"}
           "ul" {"margin-top" "0"}}
     :html [:div
            [:h1 "BibData"]
            "Eksempler:"
            (into [:ul] (<! (go<!-seq (map sample-lid sample-lids))))
            [:small "Eksemplerne er udvalgt som 1., 10., 100., 1.000., 10.000., 20.000., 30.000., 40.000., 50.000., 60.000., 70.000., 80.000., 90.000., og 100.000. mest populære bog."]
            ]}))

(route "bibdata"
       (fn [kind id]
         (go
           (case kind
             "isbn" (<! (entry (<! (fetch :isbn id))))
             "lid" (<! (entry id))
             "info" (or (clj->js (<! (bibobj id))) #js {})
             (<! (default))))))
(def process
  (run-once
    (fn []
      (go
        (log 'bibdata 'processing-data)
        (let [lids (seq (.split (str (read-file-sync "misc/lids")) "\n"))]
          (loop [lid (first lids)
                 lids (rest lids)]
            (<! (bibobj lid))
            (when (rest lids)
              (recur (first lids) (rest lids)))))))))

(route "bibdata-process" process)
