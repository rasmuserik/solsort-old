(ns solsort.test)
(def testcases (atom []))
(defn testcase [id f]
  (swap! testcases conj [id f]))
