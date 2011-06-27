(import 'java.util.UUID)
(use '[clojure.contrib.string :only (replace-char as-str)])
(require 'objects-db)
(require 'objects-sql)

(defn insert-object [namespace-uuid identifier]
  (let [new-record-uuid (UUID/nameUUIDFromBytes (.getBytes (str "io_" identifier)))]
    (add-object! {:io_uuid new-record-uuid
                 :io_identifier identifier}); Side effect here
    (to-sql-insert "io_object" {:io_namespace namespace-uuid
                                :io_uuid new-record-uuid
                                :io_datastore "{sql}"
                                :io_identifier identifier
                                :io_name identifier
                                :io_status "{published}"})))

(defn insert-object-field-relation [namespace-uuid source-uuid target-uuid]
  (let [new-record-uuid (UUID/randomUUID)]
    (to-sql-insert "rel_io_object_fields" {:io_uuid new-record-uuid
                                            :io_namespace namespace-uuid
                                            :io_source_record source-uuid
                                            :io_target_record target-uuid})))

(defn insert-referential-or-parental-relation [identifier namespace-uuid related-field-identifier source-uuid target-object-identifier type]
  (let [new-record-uuid (UUID/randomUUID)
        related-field (get-io-field related-field-identifier 'relationship)
        target-object (get-io-object-by-identifier target-object-identifier)]
    (to-sql-insert "io_relationship" {:io_uuid new-record-uuid
                                      :io_active true
                                      :io_identifier identifier
                                      :io_name identifier
                                      :io_namespace namespace-uuid
                                      :io_related_field (:io_uuid related-field)
                                      :io_source_object source-uuid
                                      :io_target_object (:io_uuid target-object)
                                      :io_type (str "{" type "}")})))

(defn insert-multiple-relation [identifier namespace-uuid source-uuid target-object-identifier type]
  (let [new-record-uuid (UUID/randomUUID)
        target-object (get-io-object-by-identifier target-object-identifier)]
    (to-sql-insert "io_relationship" {:io_uuid new-record-uuid
                                      :io_identifier identifier
                                      :io_name identifier
                                      :io_namespace namespace-uuid
                                      :io_source_object source-uuid
                                      :io_target_object (:io_uuid target-object)
                                      :io_type (str "{" type "}")})))

(defn create-io-object-table [io_identifier fields relations]
  (letfn [(remove-multiple-relation [relations]
            (filter (fn [[_ [type %]]] (not= type 'multiple)) relations))
          (to-sql-type [datatype-identifier]
            (:io_database_type (get-io-datatype datatype-identifier)))
          (to-field-decl [[k v]]
            (str "io_" (as-str k) " " (to-sql-type v)))
          (to-fields-decl [fields]
            (join ", " (map to-field-decl fields)))
          (to-rel-fields-decl [fields]
            (join ", " (map (fn [[k [type related-field target-object-identifier]]] (str "io_" (str related-field) " uuid")) fields)))]
    (let [fields-decl (to-fields-decl fields)
          rel-fields-decl (to-rel-fields-decl (remove-multiple-relation relations))]
      (str "CREATE TABLE io_" (str io_identifier) "("
           (replace-char \- \_ fields-decl)
           ", "                              
           (replace-char \- \_ rel-fields-decl)    
           ");"))))

(defn create-io-object [identifier fields relations]
  (let [the-namespace-uuid (:io_uuid (get-namespace "io"))]
    (println (insert-object the-namespace-uuid identifier)) ;Side effect here of adding in new object to memory
    (println (create-io-object-table identifier fields relations))
    (let [the-obj-uuid (:io_uuid (get-io-object-by-identifier identifier))]
      ; 'Normal attirbutes' - create rel_io_object_fields records here
      (doseq [[identifier data-type] fields]
        (let [field (get-io-field identifier data-type)]
          (println (insert-object-field-relation the-namespace-uuid the-obj-uuid (:io_uuid field)))))
      ; 'Relationships' - create io_relationship (including rel_io_object_fields for 'referential and 'parental) records here
      (doseq [[rel-identifier [type field-identifier object-identifier]] relations]
        (cond
         (contains? #{'referential, 'parental} type) (do
                                                       (println (insert-referential-or-parental-relation
                                                                 rel-identifier
                                                                 the-namespace-uuid
                                                                 field-identifier
                                                                 the-obj-uuid
                                                                 object-identifier type))
                                                       (println (insert-object-field-relation
                                                                 the-namespace-uuid
                                                                 the-obj-uuid
                                                                 (:io_uuid (get-io-field field-identifier 'relationship)))))
         (= type 'multiple) (println (insert-multiple-relation
                                      rel-identifier
                                      the-namespace-uuid
                                      the-obj-uuid
                                      object-identifier
                                      type))
         true (throw (IllegalArgumentException. (str "Do not know how to handle relationship - " type))))))))

(defn insert-io-field! [identifier datatype] ; Should not need to use this unless we have fancifully named fields for the type we are creating
  (let [new-record-uuid (UUID/randomUUID)
        datatype-uuid (:io_uuid (get-io-datatype datatype))]
    (add-io-field! {:io_uuid new-record-uuid
                    :io_datatype datatype-uuid
                    :io_identifier (str identifier)}) ; SIDE EFFECT!
    (to-sql-insert "io_field" {:io_uuid new-record-uuid
                               :io_active true
                               :io_datatype datatype-uuid
                               :io_identifier identifier
                               :io_name identifier
                               :io_namespace (:io_uuid (get-namespace 'io))})))

