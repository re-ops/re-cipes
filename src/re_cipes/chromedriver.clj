(ns re-cipes.chromedriver
  "Chrome driver setup"
  (:require
   [clojure.core.strint :refer (<<)]
   [re-cog.resources.package :refer (package)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline install
  "Setting up chrome driver"
  []
  (package "chromium-chromedriver" :present))
