(ns solsort.uccorg-monitor
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.system :refer [exec]]
    [solsort.util :refer [parse-json-or-nil]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(defn start []
  (print "starting uccorg monitor")
  (go
    (while true
      (let [status  (parse-json-or-nil (<! (exec "ssh uccorganism@93.165.158.107 'curl -s localhost:8080/status'")))]
        (if status
          (<! (timeout 10000))
          (do
            (print "uccorg restart service")
            (print (js/Date.))
            (print (<! (exec  "ssh uccorganism@93.165.158.107 'killall VBoxHeadless; launchctl load Library/LaunchAgents/apiserver.plist; launchctl start apiserver'")))
            (<! (timeout 60000))
            (print "uccorg status:")
            (print (js/Date.))
            (print (<! (exec "ssh uccorganism@93.165.158.107 'curl -s localhost:8080/status'")))
            ))))))
