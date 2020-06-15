(ns re-cipes.pfelk
  "Utilities for setting up pfsense ELK"
  (:require
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.file :refer (line-set)]
   [re-cog.resources.sysctl :refer (reload)]
   [re-cog.resources.ufw :refer (set-state add-rule reset)]))

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

(def-inline firewall
  "Enabling firewall"
  []
  (reset)
  (add-rule 5601 :allow {})
  (add-rule 22 :allow {})
  (set-state :enable))

(def-inline limits
  "Enabling ELK limits"
  []
  (let [target "/etc/sysctl.d/10-elk.conf"]
    (set-file-acl "re-ops" "rwX" "/etc/sysctl.d")
    (file target :present)
    (line target "vm.max_map_count=262144" :present)
    (reload target)))

(def-inline get-source
  "Setting up pfelk"
  []
  (let [{:keys [home user]} (configuration)
        repo "https://github.com/3ilson/docker-pfelk.git"
        dest (<< "~{home}/docker-pfelk")]
    (clone repo dest)
    (chown dest user user {:recursive true})))

(def-inline {:depends #'re-cipes.pfelk/get-source} logstash-configure
  "Configure logstash inputs"
  []
  (let [{:keys [home user]} (configuration)
        dest (<< "~{home}/docker-pfelk/logstash/conf.d/01-inputs.conf")]
    (line dest "  if [host] =~ /172\\.22\\.33\\.1/ {" :replace :with "  if [host] =~ /192\\.168\\.3\\.1/ {")
    (line dest (fn [i _] (= i 33)) :uncomment :with "#")
    (line dest (fn [i _] (= i 30)) :comment :with "#")))
