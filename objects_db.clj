(use 'clojure.contrib.sql)
(use '[clojure.contrib.properties :only (read-properties)])
(import 'java.util.UUID)

(def UUID-ADMIN (UUID/fromString "e6bf32c9-bdf0-33c8-8f4a-2390e174127d"))

(let [props (read-properties "db.properties")
      config (into {} (for [[k v] props] [(keyword k) v]))
      db-host (:db-host config)
      db-port (:db-port config)
      db-name (:db-name config)
      db-user (:db-user config)
      db-password (:db-password config)]
  (def db {:classname "org.postgres.Driver"
           :subprotocol "postgresql"
           :subname (str "//" db-host ":" db-port "/" db-name)
           :user db-user
           :password db-password}))

(def IO-OBJECTS (with-connection db
                  (with-query-results rs ["SELECT * FROM io_object"]
                    (let [objects (transient [])]
                      (doseq [r rs] (conj! objects r))
                      (persistent! objects)))))

(def IO-DATATYPES (with-connection db
                  (with-query-results rs ["SELECT * FROM io_datatype"]
                    (let [datatypes (transient [])]
                      (doseq [r rs] (conj! datatypes r))
                      (persistent! datatypes)))))

(def IO-FIELDS (with-connection db
                  (with-query-results rs ["SELECT * FROM io_field"]
                    (let [fields (transient [])]
                      (doseq [r rs] (conj! fields r))
                      (persistent! fields)))))

(defn add-object! [obj] ; SIDE EFFECT!
  (def IO-OBJECTS (conj IO-OBJECTS obj)))

(defn add-io-field! [field] ; SIDE EFFECT!
  (def IO-FIELDS (conj IO-FIELDS field)))

(defn throw-error [table fields]
  (throw (IllegalArgumentException. (str "No results for select " table " for " fields))))

(defn get-namespace [identifier]
  (with-connection db
    (with-query-results rs ["SELECT * FROM io_namespace WHERE io_identifier=?"
                            (str identifier)]
      (if (nil? rs)
        (throw-error "io_namespace" {:identifier identifier})
        (first rs)))))

(defn get-io-object-by-uuid [uuid]
  (let [object (first (filter (fn [m] (= (str uuid) (str (:io_uuid m)))) IO-OBJECTS))]
    (if (nil? object)
      (throw-error "io_object" {:uuid uuid})
      object)))
  
(defn get-io-object-by-identifier [identifier]
  (let [object (first (filter (fn [m] (= (str identifier) (:io_identifier m))) IO-OBJECTS))]
    (if (nil? object)
      (throw-error "io_object" {:identifier identifier})
      object)))

(defn get-io-datatype [identifier]
  (let [datatype (first (filter (fn [m] (= (str identifier) (:io_identifier m))) IO-DATATYPES))]
    (if (nil? datatype)
      (throw-error "io_datatype" {:identifier identifier})
      datatype)))

(defn get-io-field [identifier datatype]
  (let [field (first (filter (fn [m] (and (= (as-str identifier) (:io_identifier m))
                                          (= (:io_uuid (get-io-datatype datatype)) (:io_datatype m))))
                             IO-FIELDS))]
    (if (nil? field)
      (throw-error "io_field" {:identifier (as-str identifier) :datatype datatype})
      field)))
  
;(println (get-io-object-by-identifier 'task))
;(println (get-io-object-by-uuid "db13a9ad-2494-34bb-8a59-cb99fd308051"))
;(println (get-io-datatype 'string))
;(println (get-io-field 'updated-on 'timestamp))

