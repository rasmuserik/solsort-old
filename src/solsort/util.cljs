(ns solsort.util
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.registry :refer [testcase]]
    [solsort.mbox :refer [log]]
    [clojure.string :as string :refer [split]]
    [cljs.core.async.impl.channels :refer [ManyToManyChannel]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

(defn parse-json-or-nil [str]
  (try
    (js/JSON.parse str)
    (catch :default _ nil)))

(testcase 'parse-json-or-nil-1 
          #(nil? (parse-json-or-nil "this is not json")))
(testcase 'parse-json-or-nil-2 
          #(= (js->clj #js{:hello "world"}) (js->clj (parse-json-or-nil "{\"hello\":\"world\"}"))))


(defn jsextend [target source]
  (let [ks (js/Object.keys source)]
    (while (< 0 (.-length ks))
      (let [k (.pop ks)] (aset target k (aget source k)))))
  target)

(testcase 'jsextend 
          #(= {"foo" 1 "bar" 2}
              (js->clj (jsextend #js{:foo 1 :bar 1} #js{:bar 2}))))


(defn print-channel [c]
  (go (loop [msg (<! c)]
        (if msg (do (print msg) (recur (<! c)))))))

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

(defn transducer-status [& s]
  (fn [xf]
    (let [prev-time (atom 0)
          cnt (atom 0)]
      (fn 
        ([result]
         (apply log (concat s (list 'done)))
         (xf result))
        ([result input]
         (swap! cnt inc)
         (if (< 60000 (- (.now js/Date) @prev-time))
           (do
             (reset! prev-time (.now js/Date))
             (apply log (concat s (list @cnt)))))
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

(defn chan? [c] (instance? ManyToManyChannel c))
(testcase 'chan?-1 #(chan? (chan)))
(testcase 'chan?-2 #(not (chan? true)))

(defn parse-path [path] (.split (.slice path 1) #"[/.]"))

(def -unique-id-counter (atom 0))
(defn unique-id [] (str "id" (swap! -unique-id-counter inc)))
