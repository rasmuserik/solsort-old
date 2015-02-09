(ns solsort.main
  (:require
    [solsort.uccorg-monitor]
    [solsort.bib-related]
    [solsort.bib-process]
    [solsort.test-runner]
    [solsort.hello]
    [solsort.system :as system :refer [log]]
    ))

(enable-console-print!)
(def commands (atom {}))
(defn register [cmd f] (swap! commands assoc cmd f))

(register "uccorg-monitor" solsort.uccorg-monitor/start)
(register "bib-related" solsort.bib-related/start)
(register "bib-process" solsort.bib-process/start)
(register "test" solsort.test-runner/start)
(register "hello" solsort.hello/start)
(register "server" 
          (fn []
            (solsort.uccorg-monitor/start)
            (solsort.bib-related/start)))

(def arg
  (or
    (and (exists? js/global) js/global.process (get js/global.process.argv 2))
    (and (exists? js/window) js/window js/window.location (.slice js/window.location.hash 1))))

((or (get @commands arg)
     (fn []
       (print "possible arguments:")
       (doall (map (fn [[a b]] (print a)) @commands)))))

(log 'js-worker js/window.Worker)
(if (exists? js/Worker) 
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
  (js/postMessage "halo"))
