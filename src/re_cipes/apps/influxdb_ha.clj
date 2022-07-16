(ns re-cipes.apps.influxdb-ha
  "Home asisstant Influx + Grafana setup"
  (:require
   [re-cipes.hardening]
   [re-cipes.docker.server]
   [re-cipes.docker.nginx]
   [re-cog.resources.nginx :refer (site-enabled)]
   [re-cog.resources.ufw :refer (add-rule)]
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

(def-inline {:depends [#'re-cipes.docker.server/services #'re-cipes.apps.influxdb-ha/volume]} get-source
  "Grabbing source"
  []
  (let [repo "https://github.com/re-ops/re-dock.git"
        dest "/etc/docker/compose/influxdb-ha"
        {:keys [username bucket password org]} (configuration :influxdb)
        env (<< "~{dest}/.env")]
    (clone repo dest {})
    (copy (<< "~{dest}/influx/influxdb-ha.yml") (<< "~{dest}/docker-compose.yml"))
    (file env :present)
    (on-boot "docker-compose@influxdb-ha" :enable)))

(def-inline {:depends [#'re-cipes.docker.nginx/get-source #'re-cipes.hardening/firewall]} influx-proxy
  "Enabling reverse proxy"
  []
  (let [external-port 8087
        {:keys [nginx]} (configuration)]
    (site-enabled nginx "influxdb-ha" external-port 8086 {})
    (add-rule external-port :allow {})))

(def-inline {:depends [#'re-cipes.docker.nginx/get-source #'re-cipes.hardening/firewall]}
  grafana-proxy
  "Grafana proxy"
  []
  (let [external-port 443
        {:keys [nginx]} (configuration)]
    (site-enabled nginx "grafana" external-port 3000 {})
    (add-rule external-port :allow {})))
