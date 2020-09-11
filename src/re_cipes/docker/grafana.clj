(ns re-cipes.docker.grafana
  "Dockerized Elastisearch only"
  (:require
   [re-cipes.hardening]
   [re-cipes.docker.nginx]
   [re-cipes.docker.server]
   [re-cipes.docker.common]
   [re-cog.resources.ufw :refer (add-rule)]
   [re-cog.resources.nginx :refer (site-enabled)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.file :refer (copy symlink)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends [#'re-cipes.docker.server/services #'re-cipes.docker.common/volume #'re-cipes.docker.common/re-dock]}
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

(def-inline {:depends [#'re-cipes.docker.nginx/get-source #'re-cipes.hardening/firewall #'re-cipes.docker.grafana/setup]} nginx
  "Enabling site"
  []
  (let [external-port 3001
        {:keys [nginx]} (configuration)]
    (site-enabled nginx "grafana" external-port 3000 false)
    (add-rule external-port :allow {})))
