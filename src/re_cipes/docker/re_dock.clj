(ns re-cipes.docker.re-dock
  "Dockerized nginx revese proxy support"
  (:require
   [re-cipes.docker.server]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends #'re-cipes.docker.server/services} get-source
  "Grabbing source"
  []
  (let [repo "https://github.com/re-ops/re-dock.git"
        dest "/etc/docker/compose/re-dock"]
    (clone repo dest)))
