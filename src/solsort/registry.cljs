(ns solsort.registry)

(def routes (atom {}))
(defn route [path f]
  (swap! routes assoc path f))

(def testcases (atom []))
(defn testcase [id f]
  (swap! testcases conj [id f]))
