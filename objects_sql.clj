(import 'java.util.UUID)
(use '[clojure.contrib.string :only (as-str join)])

(defn to-sql-insert [table-name field-values] ;FIXME: should handle escape quotes
  (letfn [(instance-in? [col val] (some (fn [c] (instance? c val)) col)) 
          (quote-value [val]
            (cond
              (instance-in? #{clojure.lang.Symbol, clojure.lang.Keyword, String, UUID} val) (str "'" (as-str val) "'")
              (instance-in? #{Number Boolean} val) val
              true (throw (IllegalArgumentException. (str "Do not know how to quote value - " val ":" (type val))))))]
    (str "INSERT INTO " table-name " "
         "(" (join ", " (map as-str (keys field-values))) ") "
         "VALUES "
         "(" (join ", " (map quote-value (vals field-values))) ");")))


;(println (to-sql-insert "io_object" {:io_identifier "document"
;                                     :uuid2 (UUID/randomUUID)
;                                     :io_uuid 1231}))
