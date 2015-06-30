(ns solsort.system)

(defmacro defapi [scope id args & body]
  `(if ~(symbol (str "is-" scope))
     (do
       ~(concat `(defn ~id ~args) body)
       (route '~id ~id))
     (defn ~id ~args ~(concat `(callup '~id) args))))

(assert
  (= (macroexpand '(defapi a b [c] d e)))
  '(if is-a
     (do
       (defn b [c] d e)
       (route 'b b))
     (defn b [c] (callup 'b c))))
