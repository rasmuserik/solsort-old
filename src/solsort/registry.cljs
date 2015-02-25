(ns solsort.registry
  (:require-macros 
    [cljs.core.async.macros :refer [go alt!]])
  (:require
    [cljs.core.async.impl.channels :refer [ManyToManyChannel]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))


(def testcases (atom []))
(defn testcase [id f]
  (swap! testcases conj [id f]))


(def pid (if (exists? js/process) js/process.pid (bit-or 0 (+ 65536 (* (js/Math.random) (- 1000000 65536))))))


(def unique-id-counter (atom 0))
(defn unique-id [] (str "id" (swap! unique-id-counter inc)))
(testcase 'id-unique-1 #(= "id1" (unique-id)))
(testcase 'id-unique-2 #(= "id2" (unique-id)))


(def mbox-incoming (chan))
(def mbox-handlers (atom {}))
(defn register [msg-type f] (swap! mbox-handlers assoc msg-type f))
(defn unregister [msg-type] (swap! mbox-handlers dissoc msg-type))
(defn post [msg] (put! mbox-incoming msg))
(defn post-local [mbox data]
  (post #js{:data data :info #js{} :pid pid :mbox mbox}))
(defn local-mbox? [mbox] (contains? @mbox-handlers (name mbox)))
(defn local-mboxes [] (keys @mbox-handlers))
(defn register-fn [fname f]
  (register
    (name fname)
    (fn [o]
      (go
        (let [result-chan (apply f (or (aget o "data") []))
              result (if (instance? ManyToManyChannel result-chan)
                       (<! result-chan)
                       result-chan)
              info (or (aget o "info") #js{})]
          (js/console.log o)
          (post (clj->js {:data result
                          :info {}
                          :pid (aget info "rpid")
                          :mbox (aget info "rbox")})))))))

(defn call-local [fname & args]
  (let [ c (chan)
        rbox (unique-id)]
    (register 
      rbox 
      (fn [o]
        (unregister rbox)
        (if (aget o "data")
          (put! c (aget o "data"))
          (close! c))))
    (post #js{:data (clj->js args)
              :info #js{:rbox rbox
                        :rpid pid}
              :pid pid
              :mbox (name fname)})
    c))

(register-fn :echo (fn [a] (go a)))
(testcase 'call-local
          #(go (= "hello" (<! (call-local :echo "hello")))))



(def routes #js {})
(defn route [path f] 
  (register-fn path f)
  (aset routes path f))

