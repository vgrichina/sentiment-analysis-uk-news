(ns adder.core
  (:use org.httpkit.server)
  (:use compojure.core)
  (:use hiccup.core)
  (:require [compojure.handler :as handler])
  )

(defn view-layout [& content]
  (html
   [:head
    [:title "Тільки добрі новини"]
    [:link {:rel "stylesheet" :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css"}]
    ]
   [:body [:div.container content]]))

(defn view-input []
  (view-layout
    [:h1 "Тільки добрі новини"]

   ))


(defroutes app
  (GET "/" []
    (view-input))
  )

(stop-server)
(def stop-server (run-server (handler/site #'app) {:port 3000}))
