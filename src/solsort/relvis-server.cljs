(ns solsort.relvis-server
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
   [solsort.node :refer [exec eachLines]]
   [solsort.util :refer [parse-json-or-nil]]
   [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(def data-path "../_visual_relation_server")

(defn relvis-server []
  (let [fs (js/require "fs")]
    (go
     (if (not (.existsSync fs "tmp")) (<! (exec "mkdir tmp")))
     (if (not (.existsSync fs "tmp/coloans.csv"))
       (do (print "generating coloans.csv")
         (<! (exec (str "xzcat " data-path "/coloans/* | sed -e 's/,/,\t/' | sort -n > tmp/coloans.csv")))))
     (if (not (.existsSync fs "tmp/coloans-by-lid.csv"))
       (do (print "generating tmp/coloans-by-lid.csv")
         (<! (exec  "cat tmp/coloans.csv | sort -k+2 > tmp/coloans-by-lid.csv"))))
     (loop [lines (eachLines "tmp/coloans.csv")
            line (<! lines)]
       (if line
         (do
           ;(.log js/console "here" line)
           (recur lines (<! lines)))
         ))
      (print "done preparing data for relvis-server")
     )))

(defn start []
  (print "starting visual relation server")
  (relvis-server))
