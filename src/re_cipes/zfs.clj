(ns re-cipes.zfs
  "Zfs setup"
  (:require
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.package :refer (package)]))

(require-recipe)

(def-inline zfs
  "Base zfs setup and tunning"
  []
  (package "zfsutils-linux" :present))
