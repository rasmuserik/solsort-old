(ns solsort.relvis-server
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.node :refer [exec each-lines]]
    [solsort.keyval-db :as kvdb]
    [solsort.webserver :as webserver]
    [solsort.config :as config]
    [solsort.util :refer [parse-json-or-nil]]
    [clojure.string :as string :refer [split]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

(def data-path "../_visual_relation_server")
;(def fs (if (config/nodejs) (js/require "fs")))
(defn iterateLines [prefix fname swap]
  (go
    (print 'traversing prefix)
    (let [ids (atom #js{})]
      (loop [lines (each-lines fname)
             line (<! lines)
             prevLid nil
             loans []
             cnt 0
             amount 0
             ]
        ; whoops lid should be loan vice versa, anyhow want to make this generic/replace it with function so fix that later
        (if line
          (let
            [lineParts (.split line ",")
             lid (.trim (str (aget lineParts (if swap 1 0))))
             loan (.trim (str (aget lineParts (if swap 0 1))))
             ]
            (if (= lid prevLid)
              (recur lines (<! lines) lid (conj loans loan) cnt (inc amount))
              (do
                (if (and prevLid
                         (< 1 (count loans)))
                  (do
                    (<! (kvdb/store prefix prevLid (clj->js loans)))
                    (aset @ids prevLid amount)
                    ))
                (if (= 0 (rem cnt 100000))
                  (print cnt))
                (recur lines (<! lines) lid [] (inc cnt) 0)
                ))
            )))
      (<! (kvdb/commit prefix))
      @ids)))

(defn prepare-data-old []
  (if (not config/nodejs)
    (throw "error: not on node"))
  (go
    (if true ;(not (<! (kvdb/fetch :related "10000467")))
      (let [fs (js/require "fs")]
        (print 'prepare-data)
        (if (not (.existsSync fs "tmp")) (<! (exec "mkdir tmp")))
        (if (not (.existsSync fs "tmp/coloans.csv"))
          (do (print "generating coloans.csv" (js/Date.))
              (<! (exec (str "xzcat " data-path "/coloans/* | sed -e 's/,/,\t/' | sort -n > tmp/coloans.csv")))))
        (if (not (.existsSync fs "tmp/coloans-by-lid.csv"))
          (do (print "generating tmp/coloans-by-lid.csv" (js/Date.))
              (<! (exec  "cat tmp/coloans.csv | sort -k+2 > tmp/coloans-by-lid.csv"))))

        (if (not (<! (kvdb/fetch :patrons "000001")))
          (do
            (print "traversing coloans" (js/Date.))
            (print "added " (.-length (js/Object.keys (<! (iterateLines :patrons "tmp/coloans.csv" false)))) " elements")))
        (if (not (<! (kvdb/fetch :lid-count "all")))
          (let [lidCount (<! (iterateLines :lids "tmp/coloans-by-lid.csv" true))]
            (print "no lid-count")
            (<! (kvdb/store :lid-count "all" (js/JSON.stringify lidCount))))
          (print "has lid-count"))
        (let [lidCount (js/JSON.parse (or (<! (kvdb/fetch :lid-count "all")) "{}"))
              lids (js/Object.keys lidCount)
              ]
          (aset js/window "lidCount" lidCount)
          (print "generating coloans")
          (loop [i 0]
            (if (< i (.-length lids))
              (let [lid (aget lids i)
                    patrons (.slice (or (<! (kvdb/fetch :lids lid)) #js[]) 0 3000)
                    coloans (or (<! (kvdb/multifetch :patrons patrons)) #js{})
                    result (atom #js{})
                    ]
                (doall (for [patron (seq patrons)]
                         (doall (for [coloan (seq (aget coloans patron))]
                                  (aset @result coloan (inc (or (aget @result coloan) 0)))))))
                (<! (kvdb/store 
                      :related lid
                      (clj->js
                        (map 
                          (fn [[weight lid dnt total]] {:lid lid :weight (bit-or (- (* weight 1000)) 0)})
                          (take 
                            100
                            (sort
                              (map 
                                (fn [[lid cnt total]] 
                                  [(- (/ cnt (js/Math.log (+ 10 total)))) lid cnt total])
                                (filter (fn [[lid cnt total]] (< 1 cnt))
                                        (for [lid (seq (js/Object.keys @result))] 
                                          [lid (aget @result lid) (aget lidCount lid)])))))))))
                (if (= 0 (rem i 1000))
                  (print i (.-length lids) lid (aget lidCount lid)))
                (recur (inc i)))))
          (print "done preparing data for relvis-server" (js/Date.)))))))

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

(defn make-tmp-dir []
  (go 
    (if (not (.existsSync (js/require "fs") "tmp")) 
      (<! (exec "mkdir tmp")))))

(defn print-channel [c]
  (go (loop [msg (<! c)]
        (if msg (do (print msg) (recur (<! c)))))))

(defn kvdb-store-channel [db c]
  (go-loop 
    [key-val (<! c)]
    (if key-val
      (let [[k v] key-val]
        (<! (kvdb/store db k (clj->js v)))
        (recur (<! c)))
      (kvdb/commit db))))

(defn by-first [xf]
  (let [prev-key (atom nil)
        values (atom '())]
    (fn 
      ([result] 
       (if (< 0 (count @values)) 
         (do
           (xf result [@prev-key @values])
           (reset! values '())))
       (xf result))
      ([result input]
       (if (= (first input) @prev-key)
         (swap! values conj (rest input))
         (do 
           (if (< 0 (count @values)) (xf result [@prev-key @values]))
           (reset! prev-key (first input))
           (reset! values (list (rest input)))))))))

(defn transducer-status [s]
  (fn [xf]
    (let [prev-time (atom 0)
          cnt (atom 0)]
      (fn 
        ([result]
         (print s 'done)
         (xf result))
        ([result input]
         (swap! cnt inc)
         (if (< 6000 (- (.now js/Date) @prev-time))
           (do
             (reset! prev-time (.now js/Date))
             (print s @cnt)))
         (xf result input))))))

(defn transducer-accumulate [initial]
  (fn [xf]
    (let [acc (atom initial)]
      (fn 
        ([result]
         (if @acc (do
                    (xf result @acc)
                    (reset! acc nil)))
         (xf result))
        ([result input] 
         (swap! acc conj input))))))

(def group-lines-by-first
  (comp
    by-first
    (map (fn [[k v]] [k (map (fn [[s]] s) v)]))))
(defn swap-trim  [[a b]] [(string/trim b) (string/trim a)])

(defn transduce-file-to-db [file-name db-name transducer]
  (let [c (chan 1 transducer)]
    (pipe (each-lines file-name) c)
    (kvdb-store-channel db-name c)))

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

(defn prepare-data []
  (if (not config/nodejs) (throw "error: not on node"))
  (go
    (<! (make-tmp-dir))
    (<! (generate-coloans-csv))
    (<! (generate-coloans-by-lid-csv))
    #_(let [counts (<! (calculate-lid-counts))]
        (print 'a)
        (print 'lid-counts counts)
        (print 'b)
        )

    #_(if (not (<! (kvdb/fetch :loan-count "x8331046")))
        (<! (transduce-file-to-db
              "tmp/coloans-by-lid.csv" :loan-count
              (comp
                (map #(string/split % #","))
                (map swap-trim)
                (transducer-status "traversing loan-count")
                group-lines-by-first
                (map (fn [[k v]] [k (count v)]))
                ))))

    (if (not (<! (kvdb/fetch :patrons "000001")))
      (let [lid-counts 
            (clj->js (into {} (<! (calculate-lid-counts))))
            ]
        (print 'lid-count-length (.-length (.keys js/Object lid-counts)))
        (<! (transduce-file-to-db
              "tmp/coloans.csv" :patrons
              (comp
                (map #(string/split % #","))
                (transducer-status "traversing patrons")
                (map (fn [[k v]] [k #js[(string/trim v) (aget lid-counts (string/trim v))]]))
                group-lines-by-first)))))

    (if (not (<! (kvdb/fetch :lids "x8331046")))
      (<! (transduce-file-to-db
            "tmp/coloans-by-lid.csv" :lids 
            (comp
              (map #(string/split % #","))
              (map swap-trim)
              (transducer-status "traversing lids")
              group-lines-by-first))))

    ))

(def freqs (atom nil))
(defn get-related [lid]
  (go
    (let [cached (<! (kvdb/fetch :related lid))]
      (if cached
        cached
        (let [patrons (.slice (or (<! (kvdb/fetch :lids lid)) #js[]) 0 1000)
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
                                            (conj acc [(second coloan)
                                                       (first coloan) 
                                                       ; (<! (kvdb/fetch :loan-count lid))
                                                       ]))
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
                            (clj->js)
                            )]
          (print 'get-related lid (take 10 weighted))
          (<! (kvdb/store :related lid weighted))
          (<! (kvdb/commit :related))
          weighted)))))

(defn start []
  (go
    (<! (prepare-data))
    ;   (kvdb/clear :related)
    (<! (webserver/add "relvis-related" #(get-related (:filename %))))
    (print "starting visual relation server")
    ))
