(ns solsort.server
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]
    [solsort.manager]
    [solsort.relvis-server]
    [solsort.bib-process]
    [solsort.webserver]
    ))

(enable-console-print!)

(def commands (atom {}))
(defn register [cmd f] (swap! commands assoc cmd f))

(register "manager" solsort.manager.start)
(register "relvis-server" solsort.relvis-server.start)
(register "bib-process" solsort.bib-process.start)
(register "hello" (fn [] (solsort.webserver/add "hello" (fn [info] 
                                                          (print 'muyhaha)
                                                          (go info)))))
(solsort.webserver/add :default #(go :default))

(def arg
  (or
    (and js/window.process (get js/window.process.argv 2))
    (and (exists? js/window) js/window js/window.location (.slice js/window.location.hash 1))))

(apply (or (get @commands arg)
           (fn []
             (print "possible arguments:")
             (doall (map (fn [[a b]] (print a)) @commands)))))

