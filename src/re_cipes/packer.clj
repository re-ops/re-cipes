(ns re-cipes.packer
  "Build tools"
  (:require
   [re-cipes.access :refer (permissions)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.file :refer (symlink directory chmod)]
   [re-cog.resources.archive :refer (unzip)]))

(require-recipe)

(def-inline {:depends #'re-cipes.access/permissions} packer
  "Setting up://www.packer.io/"
  []
  (let [version "1.4.3"
        dest (<< "/tmp/packer_~{version}_linux_amd64.zip")
        sum "c89367c7ccb50ca3fa10129bbbe89273fba0fa6a75b44e07692a32f92b1cbf55"
        url (<< "https://releases.hashicorp.com/packer/~{version}/packer_~{version}_linux_amd64.zip")]
    (download url dest sum)
    (unzip dest "/opt/")
    (symlink "/usr/local/bin/packer" "/opt/packer")))
