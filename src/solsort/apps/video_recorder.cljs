(ns solsort.video-recorder
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.sys.mbox :refer [route log]]
    [solsort.sys.platform :refer [is-browser fs exit is-nodejs]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

(defn video-record []
  (let [user-media (js/navigator.mediaDevices.getUserMedia #js{:audio true :video true})]
    (.then user-media 
           (fn [stream]
             (let [video (js/document.getElementById "video")
                   src (js/URL.createObjectURL stream)]
             (js/console.log stream src)
             (aset video "src" src)
             (.play video)
             )))))

(route "video-recorder" 
       (fn [a b]
         (when is-browser
           (go (<! (timeout 200))
               (video-record)))
         {:type "html"
          :html [:div
                 [:h1 "firefox test video recorder"]
                 [:video#video]
                 ]}))
