(ns sentiment-analysis.web
  (:use org.httpkit.server)
  (:use compojure.core)
  (:use hiccup.core)
  (:require [compojure.handler :as handler]
            [sentiment-analysis.analyzer :as analyzer]
            [sentiment-analysis.scraper :as scraper])
  (:gen-class)
  )

(defn view-layout [& content]
  (html
   [:head
    [:title "Тільки добрі новини"]
    [:link {:rel "stylesheet" :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css"}]
    ]
   [:body [:div.container content]]))


(defn get-good-news []
  (->> "http://www.pravda.com.ua/news/"
       (scraper/scrape-news)
       (map (juxt identity #(analyzer/best-class analyzer/model %)))
       (filter #(= :positive (second %)))
       (map first)))

(defn news-item [item]
  [:li [:a
        {:href (java.net.URL. (java.net.URL. "http://pravda.com.ua") (:href item))}
        (:title item)]]

  )

(defn view-input [news]
  (view-layout
    [:h1 "Тільки добрі новини"]
    [:ul.list-unstyled (map news-item news)]
   ))


(defroutes app
  (GET "/" []
    (->> (get-good-news) (view-input)))
  )

;(stop-server)
;(def stop-server (run-server (handler/site #'app) {:port 3000}))

(defn -main [port]
  (run-server (handler/site #'app) {:port (read-string port)})
  (println "Started server on port" port)
  )
