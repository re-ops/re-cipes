(ns re-cipes.development.go
  "Go lang setup"
  (:require
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.file :refer (copy directory chmod)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.archive :refer (untar)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline install-go
  "Setting up go lang"
  []
  (let [{:keys [home]} (configuration)
        archive "go1.18.linux-amd64.tar.gz"
        url (<< "https://go.dev/dl/~{archive}")
        dest (<< "/usr/src/~{archive}")]
    (download url dest "e85278e98f57cdb150fe8409e6e5df5343ecb13cebf03a5d5ff12bd55a80264f")
    (untar dest home)))
