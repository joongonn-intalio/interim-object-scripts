(require 'objects)

;################################################################
; Create 'document' object
;################################################################
; These fields are NOT available yet (in bootstrap payloads), so we create them
(println (insert-io-field! 'subjects 'option-list)) 
(println (insert-io-field! 'coverage 'option-list))
(println (insert-io-field! 'valid-on 'timestamp)) 
(println (insert-io-field! 'expired-on 'timestamp)) 
(println (insert-io-field! 'documentation 'code))
(println (insert-io-field! 'published-on 'timestamp)) ; we have published:boolean
(println (insert-io-field! 'licenses 'string))
(println (insert-io-field! 'format 'string))
(println (insert-io-field! 'tags 'option-list)) ; we have tags:text
(println (insert-io-field! 'publisher 'relationship))
(println (insert-io-field! 'source 'relationship))
(println (insert-io-field! 'dmsfolder 'relationship))

(create-io-object "document"
                  ;Attributes
                  {:name 'string
                   ;related-to ; ?
                   :status 'option-list
                   :subjects 'option-list ; text[]
                   :description 'text
                   :format 'string
                   :licenses 'string
                   :type 'option-list
                   :coverage 'option-list
                   :published-on 'timestamp
                   :valid-on 'timestamp
                   :expired-on 'timestamp
                   :uuid 'uuid
                   ;path 'path ; ?
                   :application 'relationship ; FIXME: this should be a relationship when 'Application object is ready
                   :package 'relationship ; FIXME: this should be a relationship when 'Package object is ready
                   :tags 'option-list
                   :created-on 'timestamp
                   :updated-on 'timestamp
                   :active 'boolean
                   :deleted 'boolean
                   :private 'boolean
                   :documentation 'code
                   :notes 'text}
                  ;Relationships
                  {:document-publisher ['referential 'publisher 'user]
                   :document-contributors ['multiple '- 'user] ; Note: There is no related-field for N-N
                   :document-source ['referential 'source 'document]
                   ;:document-languages ['multiple '- 'language] ; 'Language object not available yet
                   :document-dmsfolder ['referential 'dmsfolder 'dmsfolder]
                   :document-owner ['referential 'owner 'user]
                   :document-created-by ['referential 'created-by 'user]
                   :document-updated-by ['referential 'updated-by 'user]
                   })


;################################################################
; Create 'version' object
;################################################################
(println (insert-io-field! 'file-name 'string))
(println (insert-io-field! 'encoding 'string))
(println (insert-io-field! 'major 'boolean))
(println (insert-io-field! 'number 'integer))
(println (insert-io-field! 'branch 'relationship))

(create-io-object "version"
                  ;Attributes
                  {:name 'string
                   ;:related-record ?
                   :number 'integer
                   :label 'string
                   :description 'text
                   :major 'boolean
                   :type 'option-list
                   :content 'text
                   ;:file 'file ;File not ready yet?
                   :file-name 'string
                   :encoding 'string
                   :size 'file-size
                   :uuid 'uuid
                   :created-on 'timestamp
                   }
                  ;Relationships
                  {:version-branch ['referential 'branch 'version]
                   ;version-media-type ['referential 'media-type 'media-type] ; Media type not ready yet
                   :version-created-by ['referential 'created-by 'user]
                   })

;NOTE: rel_... tables necessary for exporting .xml, so we do not create them.
;NOTE: why are there multiple 'uuid 'uuid type rows in database?
;ALL relationships , 1-N, N-N should have a record within io_relationship right?, if so the io_application of some records does nto appear to have it?
; whereas for io_created_by => these fields has a io_relationship associated?
;NOTE: io_fields can be 'shared' by io_objects (in rel_io_object_fields)
;package and application 1-N, but does not show up in io_relationship linkages? eg. for io_task?
