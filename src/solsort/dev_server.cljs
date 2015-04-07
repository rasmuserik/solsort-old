(ns solsort.dev-server
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.mbox :refer [route handle log]]
    [solsort.system :as system :refer [is-browser fs source-file exit is-nodejs]]
    [solsort.test-runner :refer [run-tests]]
    [solsort.ws :refer [broadcast]]
    [solsort.uccorg-monitor]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))


(enable-console-print!)
(defn autorestart []
  (if is-nodejs (.watch fs source-file (memoize (fn [] 
                                                  (broadcast "reload" nil)
                                                  (log 'system 'source-change 'restarting) (exit 0))))))

(route "dev-server"
       (fn []
         (go 
           (log 'dev-server 'start)
           (autorestart)
           (solsort.uccorg-monitor/start)
           (<! (timeout 1000))
           (run-tests)
           true)))

(if is-browser (handle "reload" #(go (<! (timeout 800)) (js/location.reload))))
