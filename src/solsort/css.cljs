(ns solsort.css
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.registry :refer [testcase route]]
    [solsort.system :refer [log]]
    [clojure.string :refer [join]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))


(defn handle-rule [[k v]]
  (str (name k) ":" (if (number? v) (str v "px") (name v))))
(defn handle-block [[id rules]]
  (str (name id) "{" (join ";" (map handle-rule (seq rules))) "}"))
(defn clj->css [o]
  (join (map str (seq o))) (join (map handle-block (seq o))))
(defn js->css [o] (clj->css (js->clj o)))

(testcase 'clj->css
          #(= (clj->css {:h1 {:font-weight :normal :font-size 14} :.div {:background :blue}})
              "h1{font-weight:normal;font-size:14px}.div{background:blue}"))


(def default-style 
  (atom { "@font-face" {:font-family "Ubuntu"
                        :font-weight "400"
                        :src "url(/font/ubuntu-latin1.ttf)format(truetype)"}
         :body {:margin 0 :padding 0 :font-family "Ubuntu, sans-serif"}
         :div {:margin 0 :padding 0} }))

(route "style" 
       #(go (clj->js {:http-headers {:Content-Type "text/css"}
                  :content (clj->css @default-style)})))