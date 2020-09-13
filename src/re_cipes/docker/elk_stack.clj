(ns re-cipes.docker.elk-stack
  "Dockerized ELK OSS full stack"
  (:require
   [re-cipes.hardening]
   [re-cipes.docker.server]
   [re-cipes.docker.common]
   [re-cipes.docker.nginx]
   [re-cog.resources.file :refer (symlink line)]
   [re-cog.resources.ufw :refer (add-rule)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.nginx :refer (site-enabled)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends [#'re-cipes.docker.server/services #'re-cipes.docker.common/volume #'re-cipes.docker.common/re-dock]}
  get-source
  "Setting up service"
  []
  (let [repo "/etc/docker/re-dock"
        dest "/etc/docker/compose/elk"]
    (symlink dest repo)
    (on-boot "docker-compose@elk" :enable)))

(def-inline {:depends [#'re-cipes.docker.nginx/get-source #'re-cipes.hardening/firewall]} nginx
  "Enabling site"
  []
  (let [external-port 5602
        {:keys [nginx]} (configuration)]
    (site-enabled nginx "kibana" external-port 5601 false)
    (add-rule external-port :allow {})))

(def-inline {:depends [#'re-cipes.docker.common/re-dock]} logstash-pipeline
  "Setting up logstash pipeline"
  []
  (let [conf "/etc/docker/re-dock/logstash/pipeline/logstash.conf"
        {:keys [password]} (configuration :elasticsearch)]
    (line conf "    password => 'changeme'" :replace :with (<< "    password => '~{password}'"))))
