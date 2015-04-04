(ns solsort.router
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.mbox :refer [local-mbox? call local local-mboxes]]
    [solsort.util :refer [chan? parse-path]]
    [solsort.html :refer [render-html]]
    [solsort.system :as system :refer [log is-browser]]
    ))

(enable-console-print!)


(defn dispatch []
  (go
    (let [args
          (or
            (and (exists? js/global) js/global.process (.slice js/global.process.argv 2))
            (and (exists? js/window) js/window js/window.location (parse-path js/window.location.hash)))]
      (log 'routes 'starting args)
      (if (local-mbox? (get args 0))
        (let [result (apply call local args)
              result (if (chan? result) (<! result) result)]
          (if (and is-browser (= "html" (:type result)))
            (render-html result))
          result)
        (log 'routes 'no-such-route args (local-mboxes))))))

(system/set-immediate dispatch)
(aset js/window "onhashchange" dispatch)
