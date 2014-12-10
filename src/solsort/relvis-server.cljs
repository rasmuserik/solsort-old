(ns solsort.relvis-server
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.node :refer [exec eachLines]]
    [solsort.evildb :as edb]
    [solsort.util :refer [parse-json-or-nil]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(def data-path "../_visual_relation_server")

(defn pput [db obj]
  (let [c (chan)]
    (.put db obj #(close! c))
    c))

(defn relvis-server []
  (go
    (<! (edb/init))
    (let [fs (js/require "fs")]
      (if (not (.existsSync fs "tmp")) (<! (exec "mkdir tmp")))
      (if (not (.existsSync fs "tmp/coloans.csv"))
        (do (print "generating coloans.csv" (js/Date.))
            (<! (exec (str "xzcat " data-path "/coloans/* | sed -e 's/,/,\t/' | sort -n > tmp/coloans.csv")))))
      (if (not (.existsSync fs "tmp/coloans-by-lid.csv"))
        (do (print "generating tmp/coloans-by-lid.csv" (js/Date.))
            (<! (exec  "cat tmp/coloans.csv | sort -k+2 > tmp/coloans-by-lid.csv"))))

      (print "traversing coloans" (js/Date.))
      (loop [lines (eachLines "tmp/coloans.csv")
             line (<! lines)
             prevLid nil
             loans []
             cnt 0
             ]
        ; whoops lid should be loan vice versa, anyhow want to make this generic/replace it with function so fix that later
        (if line
          (let
            [lineParts (.split line ",")
             lid (.trim (str (aget lineParts 0)))
             loan (.trim (str (aget lineParts 1)))
             ]
            (if (= lid prevLid)
              (recur lines (<! lines) lid (conj loans loan) cnt)
              (do
                (if prevLid
                  (<! (edb/store prevLid (clj->js loans)))
                  )
                (if (= 0 (rem cnt 100000))
                   (print cnt))
                (recur lines (<! lines) lid [] (inc cnt))
                ))
          )))
      (<! (edb/commit))
      (print "done preparing data for relvis-server" (js/Date.))
      )))

(defn start []
  (print "starting visual relation server")
  (relvis-server))