(ns solsort.registry)

(def routes (atom {}))
(defn add-route [o]
  (swap! routes assoc (:path o) o))
(defn route [path o]
  (add-route (if (map? o)
               (assoc o :path path)
               {:path path
                :type :function
                :function o}
               )))


(def testcases (atom []))
(defn testcase [id f]
  (swap! testcases conj [id f]))
