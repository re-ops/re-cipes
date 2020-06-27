(ns re-cipes.docker.elasticsearch
  "Dockerized Elastisearch only"
  (:require
   [re-cipes.docker.server]
   [re-cog.resources.file :refer (copy)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends #'re-cipes.docker.server/services} get-source
  "Grabbing source"
  []
  (let [repo "https://github.com/re-ops/re-dock.git"
        dest "/etc/docker/compose/re-dock"]
    (clone repo dest)
    (copy (<< "~{dest}/elasticsearch.yml") (<< "~{dest}/docker-compose.yml"))))
