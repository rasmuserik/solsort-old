(ns solsort.server
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(.log js/console "hello")
