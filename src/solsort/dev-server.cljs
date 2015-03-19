(ns solsort.dev-server
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.mbox :refer [route handle msg]]
    [solsort.system :as system :refer [log is-browser fs source-file exit is-nodejs]]
    [solsort.test :refer [run-tests]]
    [solsort.ws :refer [broadcast]]
    [solsort.uccorg-monitor]
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
