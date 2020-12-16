(ns re-cipes.security.osquery
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
  (let [archive "osquery_4.5.1_1.linux.amd64.deb"
        sum "9efe2e7bb6be2de5a89b121ff0f23c60615275b854644f59a8382e79385fffd1"]
    (download (<< "https://pkg.osquery.io/deb/~{archive}") (<< "/tmp/~{archive}") sum)
    (package (<< "/tmp/~{archive}") :present)))

(def-inline {:depends #'re-cipes.security.osquery/install} configure
  "Configure osquery"
  []
  (set-file-acl "re-ops" "rwX" "/etc/osquery")
  (copy "/tmp/resources/templates/osquery/osquery.conf" "/etc/osquery/osquery.conf")
  (set-file-acl "re-ops" "rwX" "/usr/share/osquery/packs")
  (copy "/tmp/resources/templates/osquery/fim.conf" "/usr/share/osquery/packs/fim.conf")
  (service "osqueryd" :restart))
