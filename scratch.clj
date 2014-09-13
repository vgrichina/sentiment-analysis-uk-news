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


(defn tokenize [s]
  (->> (string/split s #"[^\p{L}]+")
       (map string/lower-case)
       (filter #(pos? (count %)))
  ))


(->> "У Маріуполі чути залпи, під містом стріляють \"Гради\""
     (tokenize)
     (frequencies))

(def samples
  (->> "data/all-labeled.csv"
       (slurp)
       (string/split-lines)
       (map #(string/split % #"\t"))
       (map (fn [row] {:title (row 1) :class (row 2)}))
       (shuffle)
       ))

(def train-count (quot (* 80 (count samples)) 100))

(def train-samples (take train-count samples))

(def test-samples (drop train-count samples))



(def positive (filter #(= "p" (:class %)) train-samples))
(def negative (filter #(= "n" (:class %)) train-samples))

(count positive)
(count negative)

(defn make-bigrams [tokens]
  (map vector tokens (rest tokens)))

(make-bigrams [1 2 3])

(defn prepare-sample [sample]
  (let [tokens (->> sample (:title) (tokenize))
        bigrams (make-bigrams tokens)]
    (frequencies (concat tokens bigrams))))

(defn train [samples total-count]
  (let [terms (->> samples
                   (map prepare-sample)
                   (apply (partial merge-with + )))
        terms-total (reduce + (.values terms))
        prior (/ (count samples) total-count)
        divisor (+ terms-total (count terms))
        log-probabilities (map (fn [[term freq]] [term (Math/log (/ (inc freq) divisor))]) terms)
        ]

    {:log-prior (Math/log prior)
     :log-probabilities (into {} log-probabilities)}
    ))

(def model {:positive (train positive (count samples))
            :negative (train negative (count samples))})

(defn evaluate [class-model sample]
  (->> (prepare-sample sample)
       (map (fn [[term freq]] (* freq (get-in class-model [:log-probabilities term] 0))))
       (concat [(class-model :log-prior)])
       (reduce +)))

(defn best-class [model sample]
  (->> model
       (map (fn [[class model]] [class (evaluate model sample)]))
       (apply min-key second)
       (first)

       ))

(def class-map {"p" :positive "n" :negative})

(defn determine-accuracy [model test-samples]
  (let [predicted-classes (map (partial best-class model) test-samples)
        actual-classes (map #(class-map (:class %)) test-samples)
        matches-count (->> (map = predicted-classes actual-classes) (filter identity) (count))]
    (* 1.0 (/ matches-count (count test-samples))
    )))

(determine-accuracy model test-samples)









