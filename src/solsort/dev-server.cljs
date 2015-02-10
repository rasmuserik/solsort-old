(ns solsort.dev-server
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.registry :refer [route]]
    [solsort.system :as system :refer [log is-browser]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))


(enable-console-print!)

#_((if (exists? js/Worker) 
  (do
    (def worker (js/Worker. system/source-file))
    (set! (.-onmessage 
            worker)
          (fn [e] 
            (log "message from worker")
            (js/console.log e)
            ))
    ))

(if system/is-worker
  (js/postMessage "halo")))


(defn form [a]
  [:div {}
   [:h1 {} "Hello " a]
   [:div {:style {:background "red"}} "hello " 
    [:b {} "hullo"] " hi"]
   [:form
    [:input {:id "blah"}]]
   ])

(defn clj->react [o]
  (if (vector? o)
    (if (map? (second o))
      (apply js/React.createElement (name (first o)) (clj->js (second o)) (map clj->react (rest (rest o))))
      (apply js/React.createElement (name (first o)) nil (map clj->react (rest o))))
    (str o)))

(defn start []
  (if is-browser
    (do
      (go
        (loop [i 0]
          (js/React.render (clj->react (form i) ) js/document.body)
          (<! (timeout 100))
          (recur (inc i))
          ) 
      ))))

(route "dev-server"
       (fn []
         (log 'dev-server 'start)
         (start)
         (system/autorestart)))
