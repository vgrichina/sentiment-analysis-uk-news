(ns sentiment-analysis
  [:require
   [clojure.string :as string]
   [sentiment-analysis.analyzer :as analyzer]])


(analyzer/determine-accuracy analyzer/model analyzer/test-samples)


