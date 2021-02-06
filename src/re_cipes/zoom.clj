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
        sum "292bfb9f7baad32e2320949efbb6742ef1bbee3152e81a4edf61941d16e45d9f"
        dest  "/tmp/zoom_amd64.deb"]
    (download url dest sum)
    (package dest :present)))
