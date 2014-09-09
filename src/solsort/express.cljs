(ns solsort.express
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [cljs.core.async :refer [>! <! chan put! take! timeout close!]]))

(def express (js/require "express"))
(def app (express))
(def session-secret (str (.random js/Math))) ; TODO: FIXME

(def port (or js/process.env.PORT 4444))
(def host (or js/process.env.HOST "localhost"))
(def http-server (.listen app port host))


(doto app
  (.use (.logger express))
  (.use (.cookieParser express))
  (.use (.bodyParser express))
  (.use (.methodOverride express))
  (.use (.session express #js{:secret session-secret}))
  (.use (.static express "html")))

(defn shutdown []
  (go (.close http-server)))