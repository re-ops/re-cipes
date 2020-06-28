(ns re-cipes.docker.elk-stack
  "Dockerized ELK OSS full stack"
  (:require
   [re-cog.resources.service :refer (on-boot)]
   [re-cipes.docker.server]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends #'re-cipes.docker.server/services} get-source
  "Grabbing source"
  []
  (let [repo "https://github.com/re-ops/re-dock.git"
        dest "/etc/docker/compose/elk"]
    (clone repo dest)
    (on-boot "docker-compose@elk" :enable)))
