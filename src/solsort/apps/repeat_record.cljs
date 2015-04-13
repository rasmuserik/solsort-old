(ns solsort.repeat-record
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.sys.mbox :refer [route log]]
    [solsort.sys.platform :refer [is-browser fs exit is-nodejs global]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))


(defn init-camera []
  (let [c (chan)]
    (.then (js/navigator.mediaDevices.getUserMedia #js{:audio true :video true})
           (fn [stream]
             (if stream 
               (put! c stream) 
               (log 'video 'error-getting-stream))))
    c))

(defn recorder-output [recorder]
  (let [c (chan)]
    (set! (.-ondataavailable recorder)
          (fn [e]
            (let [data (.-data e)
                  blob (js/Blob. #js[data] #js{:type (.-type data)})]
              (put! c blob))))
    c))

(defn create-url [o] (js/URL.createObjectURL o))

(defn download-blob [blob filename]
  (let [a (js/document.createElement "a")
        src (create-url blob)]
    (aset a "href" src)
    (aset a "download" filename)
    (js/document.body.appendChild a)
    (.click a)
    ;(js/document.removeChild a)
    ))


(def loop-time (atom 0))
(defn status-wait [text max-time]
  (go
    (loop [i 0]
      (when (< i (min @loop-time max-time))
        (aset (js/document.getElementById "info")
              "innerHTML"
              (str text " " (- (min @loop-time max-time) i) "s"))
        (<! (timeout 1000))
        (recur (inc i))))))

(defn video-record-non-reentrant []
  (go
    (let [stream (<! (init-camera))
          video (js/document.getElementById "video")]
      (loop []
        (let [src (js/URL.createObjectURL stream)
              recorder (js/MediaRecorder. stream)
              output (recorder-output recorder)]
          (aset video "src" src)
          (aset video "controls" false)
          (set! (.-volume video) 0)
          (.play video)
          (.start recorder)
          (log 'video 'recording)
          (<! (status-wait "recording" js/Number.POSITIVE_INFINITY))
          (.stop recorder)
          (log 'video 'stopped)
          (let [blob (<! output)]
            (log 'video 'output)
            (js/console.log blob)
            (aset video "src" (create-url blob))
            (aset video "controls" false)
            (set! (.-volume video) 1)
            (.play video)
            (aset (js/document.getElementById "save") "onclick" #(download-blob blob "foo.webm"))
            (<! (status-wait "playback" @loop-time))))
        (recur)))))
(defn run-once [f]
  (let [has-run (atom false)]
    (log 'here @has-run)
    (fn [] (when-not @has-run (reset! has-run true) (f)))))

(def video-record (run-once video-record-non-reentrant))

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

(route "repeat-record" 
       (fn [a]
         (log 'video 'supported-platform (supported-platform))
         (when is-browser
           (reset! loop-time (or (js/parseInt a 10) 10))
           (go (<! (timeout 200)) (video-record)))
         {:type "html"
          :html [:div.container
                 [:h1 "repeat record - utility for repeated practice"]
                 (if (supported-platform)
                   [:div
                    [:div
                     [:span#save.button "save previous"]
                     [:a.button {:href "#repeat-record/5"} "5s"]
                     [:a.button {:href "#repeat-record/10"} "10s"]
                     [:a.button {:href "#repeat-record/20"} "20s"]
                     [:a.button {:href "#repeat-record/30"} "30s"]
                     [:a.button {:href "#repeat-record/60"} "1min"]
                     [:a.button {:href "#repeat-record/90"} "1½min"]
                     [:a.button {:href "#repeat-record/120"} "2min"]
                     [:a.button {:href "#repeat-record/180"} "3min"]
                     [:a.button {:href "#repeat-record/300"} "5min"]
                     [:a.button {:href "#repeat-record/620"} "7min"]
                     [:span#info]
                     ]
                    [:br]
                    [:video#video]]
                   unsupported-info)
                 [:h2 "About"]
                 [:div "Code idea: repeat record a short movie (typically 1-7 minutes) and play it back."]
                 [:div "This is useful for practicing, for example when preparing toastmaster talks, or learning new dance moves."]
                 [:div "Base version features"
                  [:ul
                   [:li "just successive record and playback"]
                   [:li "choose time through buttons"]
                   [:li "option to save latest recording"]
                   [:li "about-info and sensible warning on unsupported platform"]]]
                 ]}))
