(ns solsort.router
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.registry :as registry :refer [route routes]]
    [solsort.util :refer [chan?]]
    [solsort.html :refer [render-jsonhtml]]
    [solsort.system :as system :refer [log is-browser]]
    ))

(enable-console-print!)


(defn call-raw [route-name jsobj]
  (if (aget routes route-name)
    ((aget routes route-name) jsobj)
    (go nil)))

(def arg
  (or
    (and (exists? js/global) js/global.process (get js/global.process.argv 2))
    (and (exists? js/window) js/window js/window.location (.slice js/window.location.hash 1))))

(defn route-dispatch [route-name jsobj]
  (go
    (let [result-raw ((aget routes route-name) jsobj)
          result (if (chan? result-raw) (<! result-raw) result-raw)]
      (if (and is-browser (= "json-html" (aget result "type")))
        (render-jsonhtml result))
      result)))

          

(system/set-immediate 
  (fn []
    (if (aget routes arg)
      (do
        (log 'routes 'starting arg)
        (route-dispatch arg #js{})
      (log 'routes 'no-such-route arg (js/Object.keys registry/routes))))))
