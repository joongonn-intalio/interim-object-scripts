(use 'clojure.contrib.sql)
(use '[clojure.contrib.properties :only (read-properties)])
(use '[clojure.contrib.string :only (as-str)])
(use '[clojure.set :only (intersection)])
(import 'java.util.UUID)

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

(def IO-DATATYPE-OPTIONS (with-connection db
                  (with-query-results rs ["SELECT * FROM io_datatype_option"]
                    (let [datatype_options (transient [])]
                      (doseq [r rs] (conj! datatype_options r))
                      (persistent! datatype_options)))))

(def IO-FIELDS (with-connection db
                  (with-query-results rs ["SELECT * FROM io_field"]
                    (let [fields (transient [])]
                      (doseq [r rs] (conj! fields r))
                      (persistent! fields)))))

(def REL-IO-OBJECT-FIELDS (with-connection db
                  (with-query-results rs ["SELECT * FROM rel_io_object_fields"]
                    (let [rel_obj_fields (transient [])]
                      (doseq [r rs] (conj! rel_obj_fields r))
                      (persistent! rel_obj_fields)))))


(def IO-CONTROL-TYPES (with-connection db
                  (with-query-results rs ["SELECT * FROM io_control_type"]
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

(defn get-io-field-by-uuid [uuid]
  (let [field (first (filter (fn [m] (= (str uuid) (str (:io_uuid m)))) IO-FIELDS))]
    (if (nil? field)
      (throw-error "io_field" {:uuid uuid})
      field)))

(defn get-io-datatype-by-uuid [uuid]
  (let [datatype (first (filter (fn [m] (= (str uuid) (str (:io_uuid m)))) IO-DATATYPES))]
    (if (nil? datatype)
      (throw-error "io_datatype" {:uuid uuid})
      datatype)))

(defn get-io-datatype [identifier] ; a quick hack here: if identifier instance? UUID, take it as UUID of io_field
  (if (instance? UUID identifier)  ; FIXME: will there be duplicates in io_datatype table? eg. localizable string & non-localizable string?
    (get-io-datatype-by-uuid (:io_datatype (get-io-field-by-uuid identifier)))
    (let [datatype (first (filter (fn [m] (= (str identifier) (:io_identifier m))) IO-DATATYPES))]
      (if (nil? datatype)
        (throw-error "io_datatype" {:identifier identifier})
        datatype))))

(defn get-io-field [identifier datatype]
  (let [field (first (filter (fn [m] (and (= (as-str identifier) (:io_identifier m))
                                          (= (:io_uuid (get-io-datatype datatype)) (:io_datatype m))))
                             IO-FIELDS))]
    (if (nil? field)
      (throw-error "io_field" {:identifier (as-str identifier) :datatype datatype})
      field)))

(defn get-io-datatype-option [identifier related-datatype-identifier]
  (let [datatype (get-io-datatype related-datatype-identifier)
        datatype-option (first (filter (fn [m] (and (= (str identifier) (:io_identifier m))
                                                    (= (:io_uuid (:io_uuid datatype))))) IO-DATATYPE-OPTIONS))]
    (if (nil? datatype-option)
      (throw-error "io_datatype_option" {:identifier identifier
                                         :related-datatype-identifier related-datatype-identifier})
      datatype-option)))

(defn get-io-control-type-by-identifier [identifier]
  (let [control-type (first (filter (fn [m] (= (str identifier) (:io_identifier m))) IO-CONTROL-TYPES))]
    (if (nil? control-type)
      (throw-error "io_control_type" {:identifier identifier})
      control-type)))

; refactor this, for fun when free
(defn get-io-field-for-object [object-identifier identifier datatype]
  (let [object (get-io-object-by-identifier object-identifier)
        object-uuid (:io_uuid object)
        object-fields (filter (fn [rel] (= (str object-uuid) (str (:io_source_record rel)))) REL-IO-OBJECT-FIELDS)
        fields (filter (fn [m] (and (= (as-str identifier) (:io_identifier m)) ;; all fields matching identifier & type
                                    (= (:io_uuid (get-io-datatype datatype)) (:io_datatype m))))
                       IO-FIELDS)
        field fields
        object-fields-uuids (set (map #(:io_target_record %) object-fields))
        field-uuids (set (map #(:io_uuid %) fields))
        target-io-field-uuids (intersection object-fields-uuids field-uuids) ; should throw exceptions if more than one match
        field (get-io-field-by-uuid (first target-io-field-uuids))] 
    (if (nil? field)
      (throw-error "io_field" {:object-identifier object-identifier :identifier (as-str identifier) :datatype datatype})
      field)))

;(println (get-io-field 'document 'status 'option-list))

;(println (get-io-object-by-identifier 'task))
;(println (get-io-object-by-uuid "db13a9ad-2494-34bb-8a59-cb99fd308051"))
;(println (get-io-datatype 'string))
;(println (get-io-field 'updated-on 'timestamp))
;(println (get-io-datatype-option 'options 'option-list))
