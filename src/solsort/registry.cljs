(ns solsort.registry)

(def routes #js {})
(defn route [path f] (aset routes path f))

(def testcases (atom []))
(defn testcase [id f]
  (swap! testcases conj [id f]))
