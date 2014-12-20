(ns solsort.config
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [cljs.reader :refer [read-string]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(def nodejs (and (.hasOwnProperty js/window "process") 
                 (.hasOwnProperty js/window.process "title") 
                 (= js/window.process.title "node")))
