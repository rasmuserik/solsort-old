(ns solsort.router
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.registry :as registry :refer [route routes]]
    [solsort.system :as system :refer [log]]
    ))

(enable-console-print!)


(defn call-raw [route-name jsobj]
  (log 'call-raw (@routes route-name))
  (if (contains? @routes route-name)
    ((@routes route-name) jsobj)
    (go nil)))

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
