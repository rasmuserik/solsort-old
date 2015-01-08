(ns solsort.bib-related
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.system :refer [exec each-lines nodejs]]
    [solsort.keyval-db :as kvdb]
    [solsort.webserver :as webserver]
    [solsort.util :refer [print-channel kvdb-store-channel by-first transducer-status group-lines-by-first swap-trim transducer-accumulate]]
    [clojure.string :as string :refer [split]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

(defn get-related [lid]
  (go
    (let [cached (<! (kvdb/fetch :related lid))]
      (if cached
        cached
        (let [
              patrons (.slice (or (<! (kvdb/fetch :lids lid)) #js[]) 0 1000)
              coloans (->> (or (<! (kvdb/multifetch :patrons patrons)) #js{})
                           (js->clj)
                           (vals)
                           (mapcat identity)
                           (frequencies))
              coloans-with-total (loop [coloan (first coloans)
                                        coloans (rest coloans)
                                        acc []]
                                   (if coloan
                                     (recur (first coloans)
                                            (rest coloans)
                                            (conj acc [(second coloan) (first coloan) ]))
                                     acc))
              weighted (->> coloans
                            (map (fn [[[lid total] cooccur]] 
                                   [(bit-or (* 1000 (/ cooccur (.sqrt js/Math (+ 10 total)))) 0)
                                    lid cooccur total]))
                            (sort)
                            (reverse)
                            (take 100)
                            (map (fn [[weight lid cooccur total]] 
                                   {:lid lid :weight weight 
                                    ; :cooccur cooccur :total total
                                    }))
                            (clj->js))]
          (<! (kvdb/store :related lid weighted))
          weighted)))))

(def data-path "../_visual_relation_server")

(defn make-tmp-dir []
  (go 
    (if (not (.existsSync (js/require "fs") "tmp")) 
      (<! (exec "mkdir tmp")))))

(defn generate-coloans-by-lid-csv []
  (go
    (print "ensuring tmp/coloans-by-lid.csv")
    (if (not (.existsSync (js/require "fs") "tmp/coloans-by-lid.csv"))
      (<! (exec  "cat tmp/coloans.csv | sort -k+2 > tmp/coloans-by-lid.csv")))))

(defn generate-coloans-csv []
  (go
    (print "ensuring tmp/coloans.csv")
    (if (not (.existsSync (js/require "fs") "tmp/coloans.csv"))
      (<! (exec (str "xzcat " data-path "/coloans/* | sed -e 's/,/,\t/' | sort -n > tmp/coloans.csv"))))))

(defn generate-lids-csv []
  (go
    (print "ensuring tmp/lids.csv")
    (if (not (.existsSync (js/require "fs") "tmp/lids.csv"))
      (<! (exec  "cat tmp/coloans-by-lid.csv | sed -e 's/.*,[\t ]*/0, /' | uniq | sort -R > tmp/lids.csv")))))

(defn calculate-lid-counts []
  (let [transducer
        (comp
          (map #(string/split % #","))
          (map swap-trim)
          (transducer-status "finding lid-count")
          group-lines-by-first
          (map (fn [[k v]] [k (count v)]))
          (transducer-accumulate [])
          )
        c (chan 1 transducer)]
    (pipe (each-lines "tmp/coloans-by-lid.csv") c)
    c))

(defn transduce-file-to-db [file-name db-name transducer]
  (let [c (chan 1 transducer)]
    (pipe (each-lines file-name) c)
    (kvdb-store-channel db-name c)))

(defn create-patrons-db []
  (go
    (if (<! (kvdb/fetch :patrons "1205248"))
      (print "ensured patron-database")
      (let [lid-counts (clj->js (into {} (<! (calculate-lid-counts)))) ]
        (print 'lid-count-length (.-length (.keys js/Object lid-counts)))
        (<! (transduce-file-to-db
              "tmp/coloans.csv" :patrons
              (comp
                (map #(string/split % #","))
                (transducer-status "traversing patrons")
                (map (fn [[k v]] [k #js[(string/trim v) (aget lid-counts (string/trim v))]]))
                group-lines-by-first)))))))

(defn create-lids-db []
  (go
    (<! (create-lids-db))

    (if (<! (kvdb/fetch :lids "x8331046"))
      (print "ensured lids-database")
      (<! (transduce-file-to-db
            "tmp/coloans-by-lid.csv" :lids 
            (comp
              (map #(string/split % #","))
              (map swap-trim)
              (transducer-status "traversing lids")
              group-lines-by-first))))))

(defn cache-related []
  (if (not (<! (kvdb/fetch :related "x826375x")))
    (let [transducer
          (comp
            (map #(string/split % #","))
            (map swap-trim)
            (transducer-status "finding and caching related")
            group-lines-by-first
            (map (fn [[k v]] k)))
          c (chan 1 transducer)]
      (pipe (each-lines "tmp/lids.csv") c)
      (go
        (loop [lid (<! c)]
          (<! (kvdb/commit :related))
          (if lid
            (do
              (<! (get-related lid))
              (recur (<! c)))))))))

(defn prepare-data []
  (if (not nodejs) (throw "error: not on node"))
  (go
    (<! (make-tmp-dir))
    (<! (generate-coloans-csv))
    (<! (generate-coloans-by-lid-csv))
    (<! (generate-lids-csv))

    (<! (create-patrons-db))
    (<! (create-lids-db))

    (<! (cache-related))))

(defn start []
  (go
    (<! (prepare-data))
    ;   (kvdb/clear :related)
    (print "starting visual relation server")
    (<! (webserver/add "relvis-related" #(get-related (:filename %))))
    ))
