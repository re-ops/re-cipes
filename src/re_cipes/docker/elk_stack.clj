(ns re-cipes.docker.elk-stack
  "Dockerized ELK OSS full stack"
  (:require
   [re-cog.facts.datalog :refer (fqdn)]
   [re-cipes.hardening]
   [re-cipes.docker.server]
   [re-cipes.docker.re-dock]
   [re-cipes.docker.nginx]
   [re-cog.resources.file :refer (symlink line template yaml-set)]
   [re-cog.resources.ufw :refer (add-rule)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.nginx :refer (site-enabled)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends [#'re-cipes.docker.server/services #'re-cipes.docker.re-dock/volume #'re-cipes.docker.re-dock/repo]}
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
    (site-enabled nginx "kibana" external-port 5601 {})
    (add-rule external-port :allow {})))

(def-inline {:depends [#'re-cipes.docker.re-dock/repo]} logstash-pipeline
  "Setting up logstash pipeline"
  []
  (let [conf "logstash.conf"
        docker "/etc/docker/re-dock/"
        pipeline (<< "~{docker}/logstash/pipeline/~{conf}")
        compose (<< "~{docker}/docker-compose.yml")
        {:keys [password]} (configuration :elasticsearch)
        {:keys [beats]} (configuration :logstash)
        m {:password password :fqdn (fqdn) :beats beats}]
    (template (<< "/tmp/resources/templates/logstash/~{conf}") pipeline m)
    (when beats
      (yaml-set compose [:services :logstash :ports] ["5044:5044"])
      (yaml-set compose [:services :logstash :volumes 2] "./logstash/certs:/opt/certs:ro")
      (add-rule 5044 :allow {}))))
