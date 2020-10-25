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
        sum "e13ac9b8b233d31b2f4015e7d187110d8d8c679c83e577b6dbeab4795a33956d"
        dest  "/tmp/zoom_amd64.deb"]
    (download url dest sum)
    (package dest :present)))
