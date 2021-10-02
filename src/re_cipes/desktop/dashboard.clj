(ns re-cipes.desktop.dashboard
  "Dashboard automation"
  (:require
   [re-cog.resources.package :refer (package)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline x-automation
  "X automation tooling"
  []
  (package "xdotool" :present))
