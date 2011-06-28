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
(def insert-status-io-field (force-insert-io-field! 'status 'option-list)) ; there are other similarly named 'status fields
(println (:sql insert-status-io-field))
(create-io-field-option (:uuid insert-status-io-field)
                        "document_status_option-list_options"
                        'options
                        'option-list
                        "{{1},{draft},{2},{published},{3},{obselete}}")

(create-io-object "document"
                  ;Attributes (fields are 'searched' by name & type in io_fields and assigned automatically)
                  {:name 'string
                   :status (:uuid insert-status-io-field) ;hack here to allow passing in UUID of io_field in concern
                   :subjects 'text ; FIXME:should be text[]
                   :description 'text
                   :format 'string
                   :licenses 'string
                   ;:type 'option-list //FIXME: DCMI type
                   :coverage 'text ; FIXME: should be text[]
                   :published-on 'timestamp
                   :valid-on 'timestamp
                   :expired-on 'timestamp
                   :uuid 'uuid
                   ;path 'path ; ;FIXME: when path datatype is ready
                   :application 'relationship ; should be a relationship when 'Application object is ready?
                   :package 'relationship ; should be a relationship when 'Package object is ready?
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
                   ;:document-languages ['multiple '- 'language] ; 'Language object not available yet? Or should this be a field
                   :document-dmsfolder ['referential 'dmsfolder 'dmsfolder]
                   :document-owner ['referential 'owner 'user]
                   :document-created-by ['referential 'created-by 'user]
                   :document-updated-by ['referential 'updated-by 'user]
                   }
                  "{sql}")


;################################################################
; Create 'version' object - this should be in NoSQL though
;################################################################
(println (insert-io-field! 'file-name 'string))
(println (insert-io-field! 'encoding 'string))
(println (insert-io-field! 'major 'boolean))
(println (insert-io-field! 'number 'integer))
(println (insert-io-field! 'branch 'relationship))
(println (insert-io-field! 'related-record 'relationship))

(create-io-object "version"
                  ;Attributes
                  {:name 'string
                   :number 'integer
                   :label 'string
                   :description 'text
                   :major 'boolean
                   ;type 'option-list ; FIXME: construct the [Content, File] type io_field
                   :content 'text
                   ;:file 'file ; FIXME: File not ready yet?
                   :file-name 'string
                   :encoding 'string
                   :size 'file-size
                   :uuid 'uuid
                   :created-on 'timestamp
                   }
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
