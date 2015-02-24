(ns solsort.rasmuserik
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require
    [solsort.registry :refer [route]]
    [solsort.mbox :refer [handle]]
    [solsort.system :as system :refer [log is-browser fs source-file exit is-nodejs]]
    [solsort.router :refer [call-raw]]
    [solsort.test :refer [run-tests]]
    [solsort.ws :refer [broadcast]]
    [cljs.core.async :refer [>! <! chan put! take! timeout close! pipe]]))


(def img-height 120)
(def small-size 12)
(def activity-height 130)
(defn rasmuserik-html []
  [:div {:style {:text-align :center}}
   [:div {:style {:font-size 16 :margin-top (/ img-height 2) :margin-bottom (/ img-height 2)}}
    [:img {:src "/icons/rasmus-erik-voel-jensen"
           :style {:height img-height
                   :width img-height
                   :border-radius (/ img-height 2)
                   :vertical-align :middle
                   :margin-right (/ img-height 4)
                   :margin-left (/ img-height 4)
                   :box-shadow "0px 0px 2px #000"
                   }}]
    [:div {:style {:display :inline-block
                   :vertical-align :middle
                   :text-align :center
                   :margin 4
                   }}

     [:style (str "h1{font-weight:normal;margin-top:0}"
                  "h2{font-size:" small-size "px;font-weight:normal;text-align:left}")]
     [:h1 "Rasmus\xa0Erik Voel\xa0Jensen"]
     [:div {:style {:font-size "100%"}}
      "Tingskrivervej\xa021\xa03tv \xa0 2400\xa0København\xa0NV \xa0 \xa0" [:br]
      "+45\xa060703081 \xa0 hej@solsort.com" 
      ]]
    [:div {:style {:display :inline-block :width (* img-height 1.5) :height 1}}]
    ]
   [:div 
    [:div {:style {:display :inline-block
                   :width 320
                   :vertical-align :top
                   :text-align :left
                   }}
     [:h1 "Professional"]
     [:h2 "Current"]
     [:ul {:style {:min-height activity-height}}
      [:li "Write " [:a {:href "//github.com/rasmuserik"} "open source code"] 
       ", focus on HTML5 Apps in ClojureScript"]
      [:li "Create and design solutions in collaboration with non-technical stakeholders"]
      [:li "Run " [:a {:href "//solsort.com"} "my own company"]
       " with all the aspects of that"]]
     [:h2 "Experience"]
     [:div  {:style {:margin-bottom 60}}
      "Work as developer and project manager • Conference talks • Taught/created university level courses • Master degree in Computer Science"]]
    [:div {:style {:display :inline-block
                   :width 320
                   :vertical-align :top
                   :text-align :left
                   }}
     [:h1 "Personal"]
     [:h2 "Current"]
     [:ul {:style {:min-height activity-height}}
      [:li "Fatherhood - I am the father of a wonderful 2½ year old boy."]
      [:li "Dance - "
       [:a {:href "http://ci-cph.dk"} "Contact Impro"] ", "
       [:a {:href "http://www.swingshoes.dk/kalender-swingarrangementer/"} "Lindy Hop"] ", "
       [:a {:href "http://kglteater.dk/det-sker/forestillinger/sason-2014-2015/gratis/gamle-scene/milonga"} "Argentinsk Tango"]]
      [:li
       [:a {:href "http://junto.dk"} "Junto"] ", "
       [:a {:href "http://tinkuy.dk"} "Tinkuy"] ", "
       "rollespil(D&D3.5), bicycle, vegetarian"
       ]]
     [:h2 "Experience"]
     [:div {:style {:margin-bottom 60}}
      "Toastmasters • photography • kbhff • mythology/religion/mysticism • events • nature • couchsurfing • yoga/energy • music/jam • massage • mensa • højskole"]]
    ]
   [:div {:style {:font-size small-size}} 
    "I very rarely use  social media (linkedin/facebook/twitter/...) so catch me in real life instead :)" 
    [:br] "Updated Winter/Spring 2015"]]
  )

(route "rasmuserik"
       (fn []
         (go
           (clj->js {:type "json-html"
                     :title "Rasmus Erik"
                     :json-html (rasmuserik-html)}))))
