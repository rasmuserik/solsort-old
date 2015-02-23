(ns solsort.mbox
  (:require-macros 
    [cljs.core.async.macros :refer [go alt!]])
  (:require
    [cljs.core.async.impl.channels :refer [ManyToManyChannel]]
    [solsort.registry :refer [testcase]]
    [solsort.system :refer [is-nodejs is-browser log pid set-immediate]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(def unique-mbox-counter (atom 0))
(defn unique-mbox [] (str "mbox" (swap! unique-mbox-counter inc)))
(testcase 'mbox-unique-1 #(= "mbox1" (unique-mbox)))
(testcase 'mbox-unique-2 #(= "mbox2" (unique-mbox)))


(def incoming (chan))
(def handlers (atom {}))

(comment msg handle loop)
(go
  (loop []
    (let [msg (<! incoming)
          f (@handlers (aget msg "mbox"))]
      (log 'mbox msg)
      (if f (f msg)))
    (recur)))


(defn handle [msg-type f] (swap! handlers assoc msg-type f))
(defn unhandle [msg-type] (swap! handlers dissoc msg-type))


(defn register-fn [fname f]
  (handle
    (name fname)
    (fn [o]
      (go
        (let [result-chan (apply f (aget o "args"))
              result (if (instance? ManyToManyChannel result-chan)
                       (<! result-chan)
                       result-chan)]
          (put! 
            incoming
            (clj->js {:result result
                      :mbox (aget o "rbox")})))))))

(defn call [fname & args]
  (let [ c (chan)
        rbox (unique-mbox)]
    (handle rbox (fn [o]
                   (unhandle rbox)
                   (if (aget o "result")
                     (put! c (aget o "result"))
                     (close! c))))

    (put! incoming 
          #js{:mbox (name fname)
              :args (clj->js args)
              :rbox rbox})
    c))

(register-fn :echo (fn [a] (go a)))
(testcase 'mbox-call
          #(go (= "hello" (<! (call :echo "hello")))))
