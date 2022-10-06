(ns re-cipes.desktop.adb
  (:require
   [re-cog.resources.package :refer (package)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline setup
  "Setting up ADB"
  []
  (package "android-tools-fastboot" :present)
  (package "android-tools-adb" :present))
