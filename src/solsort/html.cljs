(ns solsort.html
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.css :refer [js->css]]
    [solsort.mbox :refer [log]]
    [reagent.core :as reagent]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))


(defn normalise-str [s] (.join (.split (.toLowerCase s) #"[^a-zA-Z0-9]+") "-"))
(defn hex-color [n] (str "#" (.slice (.toString (bit-or 0x1000000 (bit-and 0xffffff n)) 16) 1)))


(defn html->http [o]
  #js{:http-headers #js{"Content-Type" "text/html;charset=UTF-8"}
      :content
      (str
        "<!DOCTYPE html><html><head>"
        "<title>" (or (:title o) "solsort.com") "</title>"
        "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\">"
        "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">"
        "<meta name=\"viewport\" content=\"" "width=device-width, initial-scale=1.0"
        (if (:noscale o) ", minimum-scale=1.0, maximum-scale=1.0, user-scalable=0" "") "\">"
        "<meta name=\"format-detection\" content=\"telephone=no\">"
        "<link href=/style.css rel=stylesheet>"
        "<style id=style>" (if (:css o) (js->css (clj->js (:css o)))) "</style>"
        "</head><body>"
        (or (:rawhtml o) (reagent/render-to-static-markup (:html o)))
        "<script src=\"/solsort.js\"></script>"
        "</body></html>")})


(defn render-html [o]
  (log 'render-html)
  (if (:css o)
    (let [style-elem 
          (or (js/document.getElementById "style")
              (let [elem (js/document.createElement "style")]
                (aset elem "id" "style")
                (js/document.head.appendChild elem)
                elem))
          css-str (js->css (clj->js (:css o)))
          ]
      ; TODO: actually change style rules in document.styleSheets
      (aset style-elem "innerHTML" css-str)))
  (if (:rawhtml o)
    (aset js/document.body "innerHTML" (:rawhtml o))
    (reagent/render (:html o) js/document.body))
  (if (:title o)
    (aset (aget (js/document.getElementsByTagName "title") 0) "innerHTML" (:title o)))
  true)
