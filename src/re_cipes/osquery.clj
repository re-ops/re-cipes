(ns re-cipes.osquery
  "Setting up osquery"
  (:require
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cog.resources.file :refer (copy)]
   [re-cog.resources.service :refer (service)]
   [re-cog.resources.package :refer (package)]))

(require-recipe)

(def-inline install
  "Installing osquery"
  []
  (let [archive "osquery_3.3.2_1.linux.amd64.deb"
        sum "6c54a55df1feaf410064bc87696f9829d0787fb5b9c7beabeb4f5cca7ce6c3fb"]
    (download (<< "https://pkg.osquery.io/deb/~{archive}") (<< "/tmp/~{archive}") sum)
    (package (<< "/tmp/~{archive}") :present)))

(def-inline {:depends #'re-cipes.osquery/install} configure
  "Configure osquery"
  []
  (set-file-acl "re-ops" "rwX" "/etc/osquery")
  (copy "/tmp/resources/templates/osquery/osquery.conf" "/etc/osquery/osquery.conf")
  (set-file-acl "re-ops" "rwX" "/usr/share/osquery/packs")
  (copy "/tmp/resources/templates/osquery/fim.conf" "/usr/share/osquery/packs/fim.conf")
  (service "osqueryd" :restart))
