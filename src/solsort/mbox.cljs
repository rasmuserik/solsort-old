(ns solsort.mbox
  (:require-macros 
    [cljs.core.async.macros :refer [go alt!]])
  (:require
    [cljs.core.async.impl.channels :refer [ManyToManyChannel]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(declare -route-error)
(declare route-error-fn)
(declare post)
(declare local)
(declare processes)


;; private utility functions
(defn -route-error [msg]
  (js/console.log "route-error" msg)
  (let [info (aget msg "info")
        rbox (aget info "rbox")]
    (if rbox
      (post (aget info "rpid") rbox nil))))
(def -mboxes "mboxes for local process" (atom {}))
(def -unique-id-counter (atom 0))
(defn -unique-id [] (str "id" (swap! -unique-id-counter inc)))
(defn -local-handler [msg] ((get @-mboxes (aget msg "mbox") @route-error-fn) msg))


;; internal message passing / low level api
(def route-error-fn (atom -route-error))
(defn msg "construct a message object"
  ([pid mbox data] (msg pid mbox data #js{}))
  ([pid mbox data info] 
   (js/console.log "create-message" pid, mbox, data, info)
   #js{:pid pid :mbox mbox :data data :info info}))
(defn post "send a message to a mbox"
  ([msg] (let [pid (aget msg "pid")
               handler (if (= pid local)
                         -local-handler
                         (get @processes pid @route-error-fn))]
           (handler msg)))
  ([pid mbox data] (post (msg pid mbox data)))
  ([pid mbox data info] (post (msg pid mbox data info))))
(defn handle "register a local handler for messages"
  [mbox handler] (swap! -mboxes assoc mbox handler))
(defn unhandle [mbox] (swap! -mboxes dissoc mbox))
(defn handled? [mbox] (contains? @-mboxes mbox))
(defn handled [mbox] (keys @-mboxes))


;; processes / pid-list
(def local (if (exists? js/process) js/process.pid (bit-or 0 (+ 65536 (* (js/Math.random) (- 1000000 65536))))))
(def parent (atom 0))
(def clients (atom #{}))
(def workers (atom #{}))
(def peers (atom #{}))
(def processes "mapping from from reachable pids to function that receive messages" 
  (atom {local -local-handler}))


;; high level api
(defn call-timeout [max-wait pid mbox & args] 
  (let [c (chan)
        rbox (-unique-id)
        handler 
        (fn [msg]
          (unhandle rbox)
          (let [data (aget msg "data")]
            (if (nil? data)
              (close! c)
              (put! c data))))]
    (handle rbox handler)
    (post pid mbox (clj->js args) #js{:rpid local :rbox rbox})
    (if max-wait (go (<! (timeout max-wait)) (handler #js{})))
    c))
(defn call [pid mbox & args] (apply call-timeout false pid mbox args))
(defn route [mbox f]
  (handle 
    mbox 
    (fn [msg]
      (go
        (let [result (apply f (or (aget msg "data") []))
              result (if (instance? ManyToManyChannel result) (<! result) result)
              info (aget msg "info")]
          (post (aget info "rpid") (aget info "rbox") (clj->js result)))))))

