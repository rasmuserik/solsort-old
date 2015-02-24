(ns solsort.html
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.css :refer [js->css]]
    [solsort.system :refer [log]]
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


(defn jsonhtml->http [o]
  #js{:http-headers #js{"Content-Type" "text/html;charset=UTF-8"}
      :content
      (str
        "<!DOCTYPE html><html><head>"
        "<title>" (or (aget o "title") "solsort.com") "</title>"
        "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\">"
        "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">"
        "<meta name=\"viewport\" content=\"" "width=device-width, initial-scale=1.0"
        (if (aget o "noscale") ", minimum-scale=1.0, maximum-scale=1.0, user-scalable=0" "") "\">"
        "<meta name=\"format-detection\" content=\"telephone=no\">"
        "<link href=/style.css rel=stylesheet>"
        "<style id=style>" (if (aget o "json-css") (js->css (aget o "json-css"))) "</style>"
        "</head><body>"
        (js/React.renderToStaticMarkup (js->react (aget o "json-html")))
        "<script src=\"/react.min.js\"></script>"
        "<script src=\"/solsort.js\"></script>"
        "</body></html>")})


(defn render-jsonhtml [o]
  (log 'render-jsonhtml)
  (if (aget o "json-css")
    (let [style-elem 
          (or (js/document.getElementById "style")
              (let [elem (js/document.createElement "style")]
                (aset elem "id" "style")
                (js/document.head.appendChild elem)
                elem))
          css-str (js->css (aget o "json-css"))
          ]
      ; TODO: actually change style rules in document.styleSheets
      (aset style-elem "innerHTML" css-str)))
  (js/React.render (clj->react (aget o "json-html")) js/document.body)
  (if (aget o "title")
    (aset (aget (js/document.getElementsByTagName "title") 0) "innerHTML" (aget o "title")))
  true)
