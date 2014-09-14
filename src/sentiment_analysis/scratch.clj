(ns sentiment-analysis
  [:require
   [clojure.string :as string]
   [sentiment-analysis.analyzer :as analyzer]
   [sentiment-analysis.scraper :as scraper]])


(analyzer/determine-accuracy analyzer/model analyzer/test-samples)
