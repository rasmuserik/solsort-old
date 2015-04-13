(ns solsort.video-recorder
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.sys.mbox :refer [route log]]
    [solsort.sys.platform :refer [is-browser fs exit is-nodejs global]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))

(defn video-record []
  (let [user-media (js/navigator.mediaDevices.getUserMedia #js{:audio true :video true})]
    (.then user-media 
           (fn [stream]
             (let [video (js/document.getElementById "video")
                   src (js/URL.createObjectURL stream)
                   recorder (js/MediaRecorder. stream)]
               (js/console.log stream src recorder)
               (aset video "src" src)
               (.play video)
               (go
                 (set! (.-ondataavailable recorder)
                       (fn [e]
                         (js/console.log "dataavailable" recorder (aget e "data"))
                         (aset global "e" e)
                         (aset global "r" r)
                         ;(aset video "loop" true)
                         ;(aset video "src" (js/URL.createObjectURL (aget e "data")))
                         (let [a (js/document.createElement "a")
                               blob (js/Blob. #js[(.-data e)] #js{:type (.-type (.-data e))})
                               video (js/document.getElementById "video2")
                               src (js/URL.createObjectURL blob)
                               ]
                           (aset video "src" src)
                           (.play video)
                           (aset a "href" src)
                           (aset a "download" "video.webm")
                           (js/document.body.appendChild a)
                           (js/console.log a)
                           ;(.click a)
                           )
                         ))
                 (.start recorder)
                 (log 'video 'recording)
                 (<! (timeout 10000))
                 (.stop recorder)
                 (log 'video 'stopped)
                 )
               )))))

(defn supported-platform []
  (and (exists? js/window)
       (exists? js/MediaRecorder)
       (exists? js/URL)
       (exists? js/navigator)
       (exists? js/navigator.mediaDevices)
       ))

(def unsupported-info
  [:div
   [:h2 "Unsupported platform"]
   [:div "Unfortunately your browser doesn't support video recording with the MediaRecorder API, and thus this app will not work."]
   [:div "The MediaRecorder and navigator.mediaDevices API are emerging HTML5 capabilities, - currently(April 2015) only available on Firefox.  \"MediaStream Recording\" is a working draft of W3C"]])

(route "video-recorder" 
       (fn []
         (log 'video 'supported-platform (supported-platform))
         (when is-browser
           (go (<! (timeout 200))
               (video-record)))
         {:type "html"
          :html [:div
                 [:h1 "firefox test video recorder"]
                 (if (supported-platform)
                   [:div
                  [:video#video]
                  [:video#video2 {:loop "true"}]]
                   unsupported-info)
                 ]}))
