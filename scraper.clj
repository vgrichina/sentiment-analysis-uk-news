(ns sentiment-analysis
  [:require
   [crouton.html :as html]
   [clojure.string :as string]])

(defn scrape-news [url]
  (let [data (slurp url :encoding "CP1251")
        parsed (html/parse-string data)]
    (->>
     parsed
     (xml-seq)
     (filter #(= :dd (:tag %)))
     (map :content)
     (mapcat (partial filter #(= :a (:tag %))))
     (map (fn [a] {:href (get-in a [:attrs :href])
                   :title (apply str (:content a))
                   :summary ()
                   }))
     )))

(defn scrape-date [date]
  (let [news (scrape-news (str "http://www.pravda.com.ua/archives/date_" date))]
    (->> news
         (map (juxt :href :title))
         (map #(string/join "\t" %))
         (string/join "\n")
         (#(str % "\n"))
         (spit (str "data/" date ".tsv")))
    ))


(quote
(scrape-date "01092014")
(scrape-date "02092014")
(scrape-date "03092014")
(scrape-date "04092014")
(scrape-date "05092014")
 )

