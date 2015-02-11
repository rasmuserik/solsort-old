(ns solsort.router
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.uccorg-monitor]
    [solsort.bib-related]
    [solsort.bib-process]
    [solsort.test]
    [solsort.registry :as registry :refer [route routes]]
    [solsort.system :as system :refer [log]]
    ))

(enable-console-print!)


(defn call-raw [route-name jsobj]
  (log 'call-raw (@routes route-name))
  (if (contains? @routes route-name)
    ((@routes route-name) jsobj)
    (go nil)))

(route "uccorg-monitor" solsort.uccorg-monitor/start)
(route "bib-related" solsort.bib-related/start)
(route "bib-process" solsort.bib-process/start)
(route "server" 
       (fn []
         (solsort.uccorg-monitor/start)
         (solsort.bib-related/start)))
(def arg
  (or
    (and (exists? js/global) js/global.process (get js/global.process.argv 2))
    (and (exists? js/window) js/window js/window.location (.slice js/window.location.hash 1))))

(system/set-immediate 
  (fn []
    (if (contains? @routes arg)
      (do
        (log 'routes 'starting arg)
        ((@routes arg) #js{}))
      (log 'routes 'no-such-route arg (keys @registry/routes)))))
