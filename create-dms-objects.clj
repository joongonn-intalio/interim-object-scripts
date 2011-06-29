(require 'objects)

;################################################################
; Create 'document' object
;################################################################
; These fields are NOT available yet (in bootstrap payloads), so we create them
(println (insert-io-field! 'subjects 'text))
(println (insert-io-field! 'coverage 'text))
(println (insert-io-field! 'valid-on 'timestamp)) 
(println (insert-io-field! 'expired-on 'timestamp)) 
(println (insert-io-field! 'documentation 'code))
(println (insert-io-field! 'published-on 'timestamp)) ; we have published:boolean
(println (insert-io-field! 'licenses 'string))
(println (insert-io-field! 'format 'string))
(println (insert-io-field! 'publisher 'relationship))
(println (insert-io-field! 'source 'relationship))
(println (insert-io-field! 'dmsfolder 'relationship))
(println (insert-io-field! 'related-to 'relationship)) ; we have a similar 'related-object

; construct Document 'status options
(def insert-document-status-io-field (force-insert-io-field! 'status 'option-list)) ; there are other similarly named 'status fields
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
                  {:name 'string
                   :status (:uuid insert-document-status-io-field) ; Note: hack here to allow passing in UUID of io_field in concern
                   :subjects 'text ; TODO: should be text[]
                   :description 'text
                   :format 'string
                   :licenses 'string
                   :type (:uuid insert-document-type-io-field) ; DCMI list of types
                   :coverage 'text ; TODO: should be text[]
                   :published-on 'timestamp
                   :valid-on 'timestamp
                   :expired-on 'timestamp
                   :uuid 'uuid
                   ;path 'path ; ;TODO: when path datatype is ready
                   :application 'relationship ; tbc
                   :package 'relationship ; tbc
                   :tags 'text
                   :created-on 'timestamp
                   :updated-on 'timestamp
                   :active 'boolean
                   :deleted 'boolean
                   :private 'boolean
                   :documentation 'code
                   :notes 'text}
                  ;Relationships
                  {:document-related-to ['referential 'related-to 'object]
                   :document-publisher ['referential 'publisher 'user]
                   :document-contributors ['multiple '- 'user] ; Note: There is no related-field for N-N
                   :document-source ['referential 'source 'document]
                   ;document-languages ['multiple '- 'language] ; FIXME: How to model N-N languages? or should this be a field
                   :document-dmsfolder ['referential 'dmsfolder 'dmsfolder]
                   :document-owner ['referential 'owner 'user]
                   :document-created-by ['referential 'created-by 'user]
                   :document-updated-by ['referential 'updated-by 'user]
                   }
                  "{sql}")


;################################################################
; Create 'version' object - this should be in NoSQL though
;################################################################
(println (insert-io-field! 'file 'file)) ; WARN: 'file is currently defined as 'text in database
(println (insert-io-field! 'file-name 'string))
(println (insert-io-field! 'encoding 'string))
(println (insert-io-field! 'major 'boolean))
(println (insert-io-field! 'number 'integer))
(println (insert-io-field! 'branch 'relationship))
(println (insert-io-field! 'related-record 'relationship))

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
                  {:name 'string
                   :number 'integer
                   :label 'string
                   :description 'text
                   :major 'boolean
                   :type (:uuid insert-version-type-io-field)
                   :content 'text
                   :file 'file ; WARN: 'file is currently defined as 'text in database
                   :file-name 'string
                   :encoding 'string
                   :size 'file-size
                   :uuid 'uuid
                   :created-on 'timestamp}
                  ;Relationships
                  {:version-related-record ['referential 'related-record 'object]
                   :version-branch ['referential 'branch 'version]
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

