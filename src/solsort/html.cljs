(ns solsort.html
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))


(defn normalise-str [s] (.join (.split (.toLowerCase s) #"[^a-zA-Z0-9]+") "-"))
(defn hex-color [n] (str "#" (.slice (.toString (bit-or 0x1000000 (bit-and 0xffffff n)) 16) 1)))

(defn js->react [o]
  (if (js/Array.isArray o)
    (if (= (type (aget o 1)) js/Object)
      (apply js/React.createElement (name (aget o 0)) (aget o 1) (map js->react (.slice o 2)))
      (apply js/React.createElement (name (aget o 0)) nil (map js->react (rest o))))
    (str o)))

(defn clj->react [o]
  (js->react (clj->js o)))


(defn jsonhtml-to-http [o]
    #js{:http-headers #js{"Content-Type" "text/html;charset=UTF-8"}
        :content
        (str
          "<!DOCTYPE html><html><head><title>"
          (or (aget o "title") "solsort.com")
          "</title><meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\"><meta name=\"viewport\" content=\""
          "width=device-width, initial-scale=1.0"
          (if (aget o "noscale") ", minimum-scale=1.0, maximum-scale=1.0, user-scalable=0" "")
          "\"><meta name=\"format-detection\" content=\"telephone=no\">"
          "<style>
          @font-face {
          font-family: Ubuntu;
          font-weight: 400;
          src: url(/font/ubuntu-latin1.ttf) format(truetype);
          }

          body {
          margin: 0px;
          padding: 0px;
          font-family: Ubuntu, sans-serif
          }

          div {
          margin: 0;
          padding: 0
          }</style>"
          "</head><body>"
          (js/React.renderToStaticMarkup (js->react (aget o "json-html")))
          "<script src=\"/react.min.js\"></script>"
          "<script src=\"/solsort.js\"></script>"
          "</body></html>")})
