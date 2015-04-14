(ns solsort.apps.update-server-from-webroot
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require
    [solsort.sys.platform :refer [exec exit]]
    [solsort.sys.mbox :refer [route log]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(def shell-commands
  (str
    "git pull &&"
    "cd ../webroot &&"
    "git pull &&"
    "cp solsort.js ../solsort/solsort.js"))

(route "update-server-from-webroot" #(exec shell-commands))
