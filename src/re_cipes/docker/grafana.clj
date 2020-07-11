(ns re-cipes.docker.grafana
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
  (chmod "/var/data" "a+wrx" {:recursive true}))

(def-inline {:depends [#'re-cipes.docker.server/services #'re-cipes.docker.grafana/volume]} get-source
  "Grabbing source"
  []
  (let [repo "https://github.com/re-ops/re-dock.git"
        dest "/etc/docker/compose/grafana"]
    (clone repo dest)
    (copy (<< "~{dest}/grafana.yml") (<< "~{dest}/docker-compose.yml"))
    (on-boot "docker-compose@grafana" :enable)))
