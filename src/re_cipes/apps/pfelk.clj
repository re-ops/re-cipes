(ns re-cipes.apps.pfelk
  "Utilities for setting up pfsense ELK"
  (:require
   [re-cipes.hardening]
   [re-cipes.docker.nginx]
   [re-cipes.docker.server]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.sysctl :refer (reload)]
   [re-cog.resources.nginx :refer (site-enabled)]
   [re-cog.resources.ufw :refer (add-rule)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.file :refer (line line-set template file)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline geoipupdate
  "geoipupdate utlity"
  []
  (let [version "4.3.0"
        deb (<< "geoipupdate_~{version}_linux_amd64.deb")
        url (<< "https://github.com/maxmind/geoipupdate/releases/download/v~{version}/~{deb}")
        sum "7247574f30f79d18240a86ff235ceca2fdc88fdea39acba111c78837fe9dcbc3"]
    (download url (<< "/tmp/~{deb}") sum)
    (package (<< "/tmp/~{deb}") :present)))

(def-inline {:depends #'re-cipes.apps.pfelk/geoipupdate} geoip-config
  "Configure logstash inputs"
  []
  (let [{:keys [pfelk]} (configuration)
        dest (<< "/etc/GeoIP.conf")
        {:keys [account-id license-key]} (pfelk :geoip)
        target "EditionIDs GeoLite2-Country GeoLite2-City"
        with   "EditionIDs GeoLite2-City GeoLite2-Country GeoLite2-ASN"]
    (set-file-acl "re-ops" "rwX" dest)
    (line-set dest "AccountID" account-id " ")
    (line-set dest "LicenseKey" license-key " ")
    (line dest target :replace :with with)))

(def-inline limits
  "Enabling ELK limits"
  []
  (let [target "/etc/sysctl.d/10-elk.conf"]
    (set-file-acl "re-ops" "rwX" "/etc/sysctl.d")
    (file target :present)
    (line target "vm.max_map_count=262144" :present)
    (reload target)))

(def-inline {:depends #'re-cipes.docker.server/services} get-source
  "Setting up pfelk"
  []
  (let [{:keys [user]} (configuration)
        repo "https://github.com/narkisr/docker-pfelk.git"
        dest "/etc/docker/compose/pfelk"]
    (clone repo dest {})
    (on-boot "docker-compose@pfelk.service" :enable)))

(def-inline {:depends [#'re-cipes.apps.pfelk/get-source]} auth
  "Setting up Elastisearch auth"
  []
  (let [dest "/etc/docker/compose/pfelk"
        conf (<< "~{dest}/logstash/conf.d/50-outputs.conf")
        env (<< "~{dest}/.env")
        {:keys [password]} (configuration :elasticsearch)]
    (file env :present)
    (line env (<< "ELASTIC_PASSWORD=~{password}") :present)
    (line conf "      password => 'changeme'" :replace :with (<< "      password => '~{password}'"))))

(def-inline {:depends #'re-cipes.apps.pfelk/get-source} inputs-config
  "Configure logstash inputs"
  []
  (let [{:keys [pfelk]} (configuration)
        dest (<< "/etc/docker/compose/pfelk/logstash/conf.d/02-types.conf")
        {:keys [ip]} pfelk
        target "#    if [host] == \"10.0.0.1\" { ### Adjust to match the IP address of pfSense or OPNSense ###"
        with (<< "    if [host] == \"~{ip}\" {")]
    (line dest target :replace :with with)
    (line dest (fn [i _] (= i 4)) :uncomment :with "#")
    (line dest (fn [i _] (= i 9)) :uncomment :with "#")))

(def-inline {:depends #'re-cipes.apps.pfelk/get-source} ports
  "Configure logstash inputs"
  []
  (let [{:keys [pfelk]} (configuration)
        dest (<< "/etc/docker/compose/pfelk/docker-compose.yml")
        {:keys [ip]} pfelk]
    (line dest "      - \"9200:9200\"" :replace :with "      - \"127.0.0.1:9200:9200\"")
    (line dest "      - \"5601:5601\"" :replace :with "      - \"127.0.0.1:5601:5601\"")))

(def-inline {:depends #'re-cipes.apps.pfelk/get-source} firewall-config
  "Configure logstash inputs"
  []
  (let [{:keys [user pfelk]} (configuration)
        file "05-firewall.conf"
        dest (<< "/etc/docker/compose/pfelk/logstash/conf.d/~{file}")]
    (template (<< "/tmp/resources/templates/pfelk/~{file}.mustache") dest pfelk)))

(def-inline {:depends [#'re-cipes.docker.nginx/get-source #'re-cipes.hardening/firewall]} nginx
  "Enabling site"
  []
  (let [kibana-port 5602
        logstash-tcp-port 5141
        {:keys [nginx]} (configuration)]
    (site-enabled nginx "pfelk" kibana-port 5601 {})
    (add-rule kibana-port :allow {})
    (add-rule logstash-tcp-port :allow {})))
