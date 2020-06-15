(ns re-cipes.pfsense
  "Utilities for setting up pfsense ELK"
  (:require
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.download :refer (download)]
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
