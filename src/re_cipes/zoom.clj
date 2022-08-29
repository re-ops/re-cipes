(ns re-cipes.zoom
  "Setting up zoom"
  (:require
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.package :refer (package)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline install
  "Installing zoom"
  []
  (let [url "https://zoom.us/client/latest/zoom_amd64.deb"
        sum "562d12f05d0651b49ce39c2041c10e7d1a41083973e8d203052a1df165da80f9"
        dest  "/tmp/zoom_amd64.deb"]
    (download url dest sum)
    (package dest :present)))
