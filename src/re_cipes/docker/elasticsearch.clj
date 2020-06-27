(ns re-cipes.docker.elasticsearch
  "Dockerized Elastisearch only"
  (:require
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cipes.docker.server]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.file :refer (copy directory)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends #'re-cipes.docker.server/services} get-source
  "Grabbing source"
  []
  (let [repo "https://github.com/re-ops/re-dock.git"
        dest "/etc/docker/compose/elasticsearch"]
    (clone repo dest)
    (set-file-acl "re-ops" "rwX" "/var/")
    (directory "/var/data" :present)
    (copy (<< "~{dest}/elasticsearch.yml") (<< "~{dest}/docker-compose.yml"))
    (on-boot "docker-compose@elasticsearch" :enable)))
