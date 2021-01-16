(ns re-cipes.apps.grafana
  "Dockerized Elastisearch only"
  (:require
   [re-cipes.hardening]
   [re-cipes.docker.nginx]
   [re-cipes.docker.server]
   [re-cipes.docker.re-dock]
   [re-cog.resources.ufw :refer (add-rule)]
   [re-cog.resources.nginx :refer (site-enabled)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.file :refer (copy symlink)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends [#'re-cipes.docker.server/services #'re-cipes.docker.re-dock/volume #'re-cipes.docker.re-dock/repo]}
  setup
  "setup grafana"
  []
  (let [repo "/etc/docker/re-dock"
        dest "/etc/docker/compose/grafana"
        env (<< "~{dest}/.env")
        {:keys [password]} (configuration :elasticsearch)]
    (copy (<< "~{repo}/grafana.yml") (<< "~{repo}/docker-compose.yml"))
    (symlink dest repo)
    (on-boot "docker-compose@grafana" :enable)))

(def-inline {:depends [#'re-cipes.docker.nginx/get-source #'re-cipes.hardening/firewall #'re-cipes.apps.grafana/setup]} nginx
  "Enabling site"
  []
  (let [external-port 3001
        {:keys [nginx]} (configuration)]
    (site-enabled nginx "grafana" external-port 3000 {})
    (add-rule external-port :allow {})))
