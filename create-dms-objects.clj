(require 'objects)

;################################################################
; Create 'document' object
;################################################################

;######## CONSTRUCT NON SHARED FIELDS ########
(def insert-document-related-to (force-insert-io-field! 'related-to 'relationship))
(println (:sql insert-document-related-to))

(def insert-document-name (force-insert-io-field! 'name 'string))
(println (:sql insert-document-name))

; This series of inserts will crash if similarly named fields exist in database, so guarantees non shared
(println (insert-io-field! 'subjects 'text)) 
(println (insert-io-field! 'coverage 'text))
(println (insert-io-field! 'valid-on 'timestamp)) 
(println (insert-io-field! 'expired-on 'timestamp)) 
(println (insert-io-field! 'documentation 'code))
(println (insert-io-field! 'published-on 'timestamp))
(println (insert-io-field! 'licenses 'string))
(println (insert-io-field! 'format 'string))
(println (insert-io-field! 'publisher 'relationship))
(println (insert-io-field! 'source 'relationship))
(println (insert-io-field! 'dmsfolder 'relationship))

(def insert-document-status-io-field (force-insert-io-field! 'status 'option-list)) ; there exists other "status" fields
(println (:sql insert-document-status-io-field))
(create-io-field-option (:uuid insert-document-status-io-field)
                        "document_status_option-list_options"
                        'options
                        'option-list
                        {:1 "draft"
                         :2 "published"
                         :3 "obselete"})

; construct Document 'type options
(def insert-document-type-io-field (force-insert-io-field! 'type 'option-list)) ; there are other similarly named 'status fields
(println (:sql insert-document-type-io-field))
(create-io-field-option (:uuid insert-document-type-io-field)
                        "document_type_option-list_options"
                        'options
                        'option-list
                        {:1 "collection"
                         :2 "dataset"
                         :3 "event"
                         :4 "image"
                         :5 "interactive_resource"
                         :6 "moving_image"
                         :7 "physical_object"
                         :8 "service"
                         :9 "software"
                         :10 "sound"
                         :11 "still_image"
                         :12 "text"})



(create-io-object "document"
                  ;Attributes (fields are 'searched' by name & type in io_fields and assigned automatically)
                  {:name (:uuid insert-document-name) ;NS
                   :status (:uuid insert-document-status-io-field);NS
                   :subjects 'text ;NS TODO: should be text[]
                   :description 'text
                   :format 'string ;NS
                   :licenses 'string ;NS
                   :type (:uuid insert-document-type-io-field) ;NS DCMI list of types
                   :coverage 'text ;NS TODO: should be text[]
                   :published-on 'timestamp ;NS
                   :valid-on 'timestamp ;NS
                   :expired-on 'timestamp ;NS
                   :uuid 'uuid
                   ;path 'path ; FIXME:
                   :application 'relationship
                   :package 'relationship
                   :tags 'text
                   :created-on 'timestamp
                   :updated-on 'timestamp
                   :active 'boolean
                   :deleted 'boolean
                   :private 'boolean
                   :documentation 'code
                   :notes 'text}
                  ;Relationships
                  {:document-related-to ['referential (:uuid insert-document-related-to) 'object] ;NS
                   :document-publisher ['referential 'publisher 'user] ;NS
                   :document-contributors ['multiple '- 'user] ; NS
                   :document-source ['referential 'source 'document]; NS
                   ;document-languages ['multiple '- 'language]
                   :document-dmsfolder ['referential 'dmsfolder 'dmsfolder]
                   :document-owner ['referential 'owner 'user]
                   :document-created-by ['referential 'created-by 'user]
                   :document-updated-by ['referential 'updated-by 'user]
                   }
                  "{sql}")




;################################################################
; Create 'version' object - this should be in NoSQL though
;################################################################
(def insert-version-file (force-insert-io-field! 'file 'file))
(println (:sql insert-version-file))

(def insert-version-name (force-insert-io-field! 'name 'string))
(println (:sql insert-version-name))

(def insert-version-label (force-insert-io-field! 'label 'string))
(println (:sql insert-version-label))

(def insert-version-content (force-insert-io-field! 'content 'text))
(println (:sql insert-version-content))

(def insert-version-size (force-insert-io-field! 'size 'file-size))
(println (:sql insert-version-size))

(def insert-version-related-object (force-insert-io-field! 'related-object 'relationship))
(println (:sql insert-version-related-object))

(def insert-version-related-record (force-insert-io-field! 'related-record 'relationship))
(println (:sql insert-version-related-record))

; NON SHARED FIELDS
(println (insert-io-field! 'file-name 'string))
(println (insert-io-field! 'encoding 'string))
(println (insert-io-field! 'major 'boolean))
(println (insert-io-field! 'number 'integer))
(println (insert-io-field! 'branch 'relationship))


; construct Version 'status options
(def insert-version-type-io-field (force-insert-io-field! 'type 'option-list)) ; there are other similarly named 'status fields
(println (:sql insert-version-type-io-field))
(create-io-field-option (:uuid insert-version-type-io-field)
                        "version_type_option-list_options"
                        'options
                        'option-list
                        {:1 "content"
                         :2 "file"})

(create-io-object "version"
                  ;Attributes
                  {:name (:uuid insert-version-name) ;NS
                   :number 'integer ;NS
                   :label (:uuid insert-version-label) ;NS
                   :major 'boolean ;NS
                   :type (:uuid insert-version-type-io-field)
                   :content (:uuid insert-version-content) ;NS
                   :file (:uuid insert-version-file) ;NS
                   :file-name 'string ;NS
                   :encoding 'string ;NS
                   :size (:uuid insert-version-size) ;NS
                   :uuid 'uuid
                   :created-on 'timestamp
                   :deleted 'boolean} ;WARN: code changes require this?!
                  ;Relationships
                  {:version-related-object ['referential (:uuid insert-version-related-object) 'object] ;NS
                   :version-related-record ['referential (:uuid insert-version-related-record) 'object] ;NS
                   :version-branch ['referential 'branch 'version] ;NS
                   ;version-media-type ['referential 'media-type 'media-type] ; Media type not ready yet
                   :version-created-by ['referential 'created-by 'user]
                   }
                  "{nosql}")

;NOTE: rel_... tables necessary for exporting .xml, so we do not create them.
;NOTE: why are there multiple 'uuid 'uuid type rows in database?
;ALL relationships , 1-N, N-N should have a record within io_relationship right?, if so the io_application of some records does nto appear to have it?
; whereas for io_created_by => these fields has a io_relationship associated?
;NOTE: io_fields can be 'shared' by io_objects (in rel_io_object_fields)
;package and application 1-N, but does not show up in io_relationship linkages? eg. for io_task?
;Observation: latest Exporter exports io_field name="null" if the name field in io_field is not speciifed in the database (whereas old exporter exports name as uuid?)
