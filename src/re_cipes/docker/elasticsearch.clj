(ns re-cipes.docker.elasticsearch
  "Dockerized Elastisearch only"
  (:require
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cipes.docker.server]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.file :refer (copy directory chmod)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline volume
  "Setting up data volume"
  []
  (set-file-acl "re-ops" "rwX" "/var/")
  (directory "/var/data" :present)
  (chmod "/var/data" "a+wrx" {}))

(def-inline {:depends [#'re-cipes.docker.server/services re-cipes.docker.elasticsearch/volume]} get-source
  "Grabbing source"
  []
  (let [repo "https://github.com/re-ops/re-dock.git"
        dest "/etc/docker/compose/elasticsearch"]
    (clone repo dest)
    (copy (<< "~{dest}/elasticsearch.yml") (<< "~{dest}/docker-compose.yml"))
    (on-boot "docker-compose@elasticsearch" :enable)))
