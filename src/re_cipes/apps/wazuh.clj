(ns re-cipes.apps.wazuh
  "Wazuh single server on docker using:
     https://documentation.wazuh.com/4.0/docker/wazuh-container.html#production-deployment"
  (:require
   [re-cog.resources.exec :refer (run)]
   [re-cipes.hardening]
   [re-cipes.docker.nginx]
   [re-cipes.docker.server]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.archive :refer (unzip)]
   [re-cog.resources.sysctl :refer (reload)]
   [re-cog.resources.nginx :refer (site-enabled)]
   [re-cog.resources.ufw :refer (add-rule)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.git :refer (clone checkout)]
   [re-cog.resources.file :refer (line line-set template file)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends #'re-cipes.docker.server/services} get-source
  "Grabing Wazuh docker source"
  []
  (let [{:keys [user]} (configuration)
        repo "https://github.com/wazuh/wazuh-docker.git"
        dest "/etc/docker/compose/wazuh"]
    (clone repo dest {})
    (on-boot "docker-compose@pfelk.service" :enable)))

(def-inline {:depends #'re-cipes.apps.wazuh/get-source} certs
  "Set up self signed certs"
  []
  (let [version "1.8"
        archive (<< "search-guard-tlstool-~{version}.zip")
        url (<< "https://maven.search-guard.com/search-guard-tlstool/1.8/~{archive}")
        sum "f59f963c7ee28d557849ccde297660a3c593a6bf3531d7852fb9ab8b4fc7597e"
        docker "/etc/docker/compose/wazuh"
        production (<< "~{docker}/production_cluster")
        ssl_path (<< "~{production}/ssl_certs")
        certs_yml (<< "~{ssl_path}/certs.yml")
        nginx_gen (<< "~{production}/nginx/ssl/generate-self-signed-cert.sh")
        kibana_gen (<< "~{production}/kibana_ssl/generate-self-signed-cert.sh")]
    (letfn [(generate []
              (script
               ("/opt/tlstool/tools/sgtlstool.sh" "-c" ~certs_yml "-ca" "-crt" "-t" ~ssl_path)
               ("/usr/bin/bash" ~nginx_gen)
               ("/usr/bin/bash" ~kibana_gen)))]
      (download url (<< "/tmp/~{archive}") sum)
      (unzip (<< "/tmp/~{archive}") (<< "/opt/tlstool"))
      (run generate))))
