(ns solsort.main
  (:require
    [solsort.uccorg-monitor]
    [solsort.bib-related]
    [solsort.bib-process]
    [solsort.test-runner]
 ;   [solsort.system :refer [is-debug]]
 ;   [figwheel.client]
    ))

(enable-console-print!)
;(if is-debug (figwheel.client/start {}))
(def commands (atom {}))
(defn register [cmd f] (swap! commands assoc cmd f))

(register "uccorg-monitor" solsort.uccorg-monitor/start)
(register "bib-related" solsort.bib-related/start)
(register "bib-process" solsort.bib-process/start)
(register "test" solsort.test-runner/start)
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
