(ns re-cipes.apps.influxdb
  "Dockerized influxdb"
  (:require
   [re-cipes.hardening]
   [re-cipes.docker.server]
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.file :refer (copy directory chmod line file)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline volume
  "Setting up data volume"
  []
  (set-file-acl "re-ops" "rwx" "/var/local/")
  (directory "/var/local/data" :present)
  (chmod "/var/local/data" "a+wrx" {:recursive true}))

(def-inline {:depends [#'re-cipes.docker.server/services #'re-cipes.apps.influxdb/volume]} get-source
  "Grabbing source"
  []
  (let [repo "https://github.com/re-ops/re-dock.git"
        dest "/etc/docker/compose/influxdb"
        {:keys [username bucket password org]} (configuration :influxdb)
        env (<< "~{dest}/.env")]
    (clone repo dest {})
    (copy (<< "~{dest}/influxdb.yml") (<< "~{dest}/docker-compose.yml"))
    (file env :present)
    (line env (<< "BUCKET=~{bucket}") :present)
    (line env (<< "ORG=~{org}") :present)
    (line env (<< "USERNAME=~{username}") :present)
    (line env (<< "PASSWORD=~{password}") :present)
    (on-boot "docker-compose@influxdb" :enable)))
