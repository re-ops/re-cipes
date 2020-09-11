(ns re-cipes.docker.pfelk
  "Utilities for setting up pfsense ELK"
  (:require
   [re-cipes.docker.nginx]
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

(def-inline {:depends #'re-cipes.docker.pfelk/geoipupdate} geoip-config
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
        repo "https://github.com/3ilson/docker-pfelk.git"
        dest (<< "/etc/docker/compose/pfelk")]
    (clone repo dest)
    (on-boot "docker-compose@pfelk.service" :enable)))

(def-inline {:depends #'re-cipes.docker.pfelk/get-source} inputs-config
  "Configure logstash inputs"
  []
  (let [{:keys [pfelk]} (configuration)
        dest (<< "/etc/docker/compose/pfelk/logstash/conf.d/01-inputs.conf")
        {:keys [ip]} pfelk
        target "  if [host] =~ /172\\.22\\.33\\.1/ {"
        with (<< "  if [host] =~ /~{ip}/ {")]
    (line dest target :replace :with with)
    (line dest (fn [i _] (= i 33)) :uncomment :with "#")
    (line dest (fn [i _] (= i 30)) :comment :with "#")))

(def-inline {:depends #'re-cipes.docker.pfelk/get-source} firewall-config
  "Configure logstash inputs"
  []
  (let [{:keys [user pfelk]} (configuration)
        file "05-firewall.conf"
        dest (<< "/etc/docker/compose/pfelk/logstash/conf.d/~{file}")]
    (template (<< "/tmp/resources/templates/pfelk/~{file}.mustache") dest pfelk)))

(def-inline {:depends [#'re-cipes.docker.nginx/get-source #'re-cipes.hardening/firewall]} nginx
  "Enabling site"
  []
  (let [external-port 5602
        {:keys [nginx]} (configuration)]
    (site-enabled nginx "pfelk" external-port 5601 false)
    (add-rule external-port :allow {})))
