(ns re-cipes.apps.wazuh
  "Wazuh server on docker using:
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
   [re-cog.resources.ufw :refer (add-rule)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.git :refer (clone checkout)]
   [re-cog.resources.file :refer (line file copy yaml-set)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends #'re-cipes.docker.server/services} get-source
  "Grabing Wazuh docker source"
  []
  (let [{:keys [user]} (configuration)
        repo "https://github.com/wazuh/wazuh-docker.git"
        dest "/etc/docker/compose/wazuh"]
    (clone repo dest {})
    (copy (<< "~{dest}/production-cluster.yml") (<< "~{dest}/docker-compose.yml"))
    (on-boot "docker-compose@wazuh.service" :enable)))

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

(def-inline max-map
  "Increasing max-map count"
  []
  (letfn [(sysctl-reload [target]
            (fn []
              (script ("sudo" "/usr/sbin/sysctl" "-e" "-p" ~target))))]
    (let [target "/etc/sysctl.d/10-max-count.conf"]
      (set-file-acl "re-ops" "rwX" "/etc/sysctl.d")
      (file target :present)
      (line target "vm.max_map_count = 262144" :present)
      (run (sysctl-reload target)))))

(def-inline {:depends #'re-cipes.apps.wazuh/get-source} wazuh-creds
  "Setting up API/Elasticsearch creds"
  []
  (let [dest "/etc/docker/compose/wazuh/docker-compose.yml"
        {:keys [api elasticsearch]} (configuration :wazuh :passwords)
        sets {[:wazuh-master 2] (<< "ELASTIC_PASSWORD=~{elasticsearch}")
              [:wazuh-master 8] (<< "API_PASSWORD=~{api}")
              [:wazuh-worker 2] (<< "ELASTIC_PASSWORD=~{elasticsearch}")
              [:kibana 1] (<< "ELASTICSEARCH_PASSWORD=~{elasticsearch}")
              [:kibana 7] (<< "API_PASSWORD=~{api}")}]
    (doseq [[[service k] v] sets]
      (yaml-set dest [:services service :environment k] v))))

(def-inline {:depends #'re-cipes.apps.wazuh/get-source} elastic-creds
  " Setup Elasticsearch password, the following method is used to generate the hash:

     $ docker exec -it <container-id> chmod +x /usr/share/elasticsearch/plugins/opendistro_security/tools/hash.sh
     $ docker exec -it <container-id> /usr/share/elasticsearch/plugins/opendistro_security/tools/hash.sh -p <password>

    Once the password hass been chaged delete the existing volumes:

     $ docker-compose down -v

    See also https://aws.amazon.com/blogs/opensource/change-passwords-open-distro-for-elasticsearch/"
  []
  (let [dest "/etc/docker/compose/wazuh"
        users (<< "~{dest}/production_cluster/elastic_opendistro/internal_users.yml")
        {:keys [admin infra]} (configuration :wazuh :hashes)]
    (yaml-set users [:admin :hash] admin)
    (doseq [k [:kibanaserver :kibanaro :logstash :readall :snapshotrestore]]
      (yaml-set users [k :hash] infra))))

#_(def-inline {:depends #'re-cipes.apps.wazuh/get-source} auth
    "set up manager auth see:
     https://documentation.wazuh.com/4.0/user-manual/registering/password-authorization-registration.html"
    [])
