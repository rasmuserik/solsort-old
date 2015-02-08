(ns solsort.bib-process
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.system :refer [exec each-lines]]
    [solsort.kvdb :as kvdb]
    [solsort.webserver :as webserver]
    [solsort.util :refer [print-channel by-first transducer-status]]
    [clojure.string :as string :refer [split]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

(defn get-random [obj]
  (let [total (reduce + (vals obj))
        pos (* (js/Math.random) total)
        elems (seq obj)]
    (loop [elem (first elems)
           elems (rest elems)
           w 0]
      (let [w (+ w (second elem))]
        (if (or (<= pos w) (empty? elems))
          (first elem)
          (recur (first elems) (rest elems) w))))))

(defn noise [atm elem]
  (swap! atm assoc elem (inc (get @atm elem 0)))
  (if (< (js/Math.random) 0.01)
    (get-random @atm)
    elem))

(defn gender [a] (nth a 2))
(defn age [line] (- (js/parseInt (nth line 7)) (js/parseInt (nth line 3))))

(def gender-age-stat (atom {}))
(defn gender-age [elems]
  (into (sorted-map )(frequencies (map #(noise gender-age-stat (str (gender %) (age %))) elems))))

(def libraries-stat (atom {}))
(defn libraries [elems]
  (into (sorted-map )(frequencies (map #(noise libraries-stat (second %)) elems))))

(defn dates [elems]
  (into (sorted-map )(frequencies (map #(nth % 7) elems))))

(defn stat [[lid elems]]
  (let [[user library gender birthyear id _lid cluster date title author type1] (first elems)]
    (into {}
    (concat
      {:lid lid
       :count (count elems)
       :id id
       :cluster cluster}
      (if (= "\"\"" title)
        {} {
       :title (if title (.slice title 1 -1) "")
       :author (if author (.slice author 1 -1) "")
       :kind type1})
      (if (< 9 (count elems))
        {
         :genderAge (gender-age elems)
         :libraries (libraries elems)
         :dates (dates elems)
         } {})
      ))))

(defn into-file [filename channel]
  (go 
    (let [fs (js/require "fs")
          out (.createWriteStream fs filename)
          ]
      (loop [msg (<! channel)]
        (if msg (do (.write out (str (js/JSON.stringify (clj->js msg)) "\n")) (recur (<! channel)))))
      (.end out))))


(defn start []
  (let [transducer
        (comp
          (transducer-status "writing stats.jsonl")
          (map #(string/split % #","))
          (map (fn [a] (map string/trim a)))
          (map (fn [a] (concat (list (nth a 5)) a)))
          by-first
          (map stat)
          )
        c (chan 1 transducer)]
    (pipe (each-lines "../final_adhl.sorted.csv") c)
    (into-file "stats.jsonl" c)
    (print "done stats.jsonl")
    ))
