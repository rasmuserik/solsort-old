(ns solsort.dev-server
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.sys.mbox :refer [route handle log]]
    [solsort.sys.platform :refer [is-browser fs exit is-nodejs]]
    [solsort.lib.test-runner :refer [run-tests]]
    [solsort.lib.net :refer [broadcast]]
    [solsort.apps.uccorg-monitor]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))


(defn autorestart []
  (if is-nodejs (.watch fs js/__filename (memoize (fn [] 
                                                  (broadcast "reload" nil)
                                                  (log 'system 'source-change 'restarting) (exit 0))))))
(route "dev-server"
       (fn []
         (go 
           (log 'dev-server 'start)
           (autorestart)
           (solsort.apps.uccorg-monitor/start)
           (<! (timeout 1000))
           (run-tests)
           true)))

(if is-browser (handle "reload" #(go (<! (timeout 800)) (js/location.reload))))
