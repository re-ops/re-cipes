(ns re-cipes.apps.aptcache
  "Setting up aptcacheng"
  (:require
   [re-cipes.hardening]
   [re-cog.resources.ufw :refer (add-rule)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.package :refer (package set-selection)]))

(require-recipe)

(def-inline {:depends [#'re-cipes.hardening/firewall]} setup
  "Setting apt cache ng server"
  []
  (set-selection "apt-cacher-ng" "tunnelenable" "boolean" "false")
  (package "apt-cacher-ng" :present)
  (add-rule 3142 :allow {}))
