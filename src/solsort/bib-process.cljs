(ns solsort.bib-process
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.node :refer [exec each-lines]]
    [solsort.keyval-db :as kvdb]
    [solsort.webserver :as webserver]
    [solsort.config :as config]
    [solsort.util :refer [parse-json-or-nil]]
    [clojure.string :as string :refer [split]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

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
         (if (< 60000 (- (.now js/Date) @prev-time))
           (do
             (reset! prev-time (.now js/Date))
             (print s @cnt)))
         (xf result input))))))

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

(defn start []
  (let [transducer
        (comp
          (map #(string/split % #","))
          (map (fn [a] (map string/trim a)))
          ;(map (fn [a] [(nth 5 a) a]))
          (map #(print 'a %)))
        c (chan 1 transducer)]
    (pipe (each-lines "../final_adhl.sorted.csv") c)))
