(ns re-cipes.restore
  "Restore drive setup"
  (:require
   [re-cipes.access :refer (permissions)]
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.disk :refer (partition- mount)]
   [re-cog.resources.file :refer (directory)]))

(require-recipe)

(def-inline {:depends #'re-cipes.access/permissions} prepare-disk
  "Preparing restore disk"
  []
  (let [device "/dev/vdb" root "/media" target "restore"]
    (partition- device)
    (set-file-acl "re-ops" "rwX" root)
    (directory (<< "~{root}/~{target}") :present)
    (mount device (<< "~{root}/~{target}"))
    (set-file-acl "re-ops" "rwX" (<< "~{root}/~{target}"))))
