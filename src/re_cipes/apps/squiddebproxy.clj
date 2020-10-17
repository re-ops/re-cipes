(ns re-cipes.apps.squiddebproxy
  "Setting up squid-deb-proxy"
  (:require
   [re-cipes.hardening]
   [re-cog.resources.ufw :refer (add-rule)]
   [re-cog.resources.service :refer (on-boot service)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.package :refer (package set-selection)]))

(require-recipe)

(def-inline {:depends [#'re-cipes.hardening/firewall]} setup
  "Setting squid deb proxy server"
  []
  (package "squid-deb-proxy" :present)
  (service "squid" :stop)
  (on-boot "squid" :disable)
  (add-rule 8000 :allow {}))
