(ns re-cipes.docker.elk-stack
  "Dockerized ELK OSS full stack"
  (:require
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cog.resources.file :refer (directory chmod)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cipes.docker.server]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline volume
  "Setting up data volume"
  []
  (set-file-acl "re-ops" "rwX" "/var/")
  (directory "/var/data" :present)
  (directory "/var/data/elasticsearch" :present)
  (directory "/var/data/grafana" :present)
  (chmod "/var/data" "a+wrx" {:recursive true}))

(def-inline {:depends [#'re-cipes.docker.server/services #'re-cipes.docker.elk-stack/volume]} get-source
  "Grabbing source"
  []
  (let [repo "https://github.com/re-ops/re-dock.git"
        dest "/etc/docker/compose/elk"]
    (clone repo dest)
    (on-boot "docker-compose@elk" :enable)))
