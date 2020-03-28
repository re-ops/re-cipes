(ns re-cipes.python
  "Installing latest python versions"
  (:require
   [re-cog.resources.exec :refer [run]]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.package :refer (package repository update-)]))

(require-recipe)

(def-inline python-3.7
  "Setting up python 3.7"
  []
  (repository "ppa:deadsnakes/ppa" :present)
  (update-)
  (package "python3.7" :present))

(def-inline python-3
  "Installing system python 3"
  []
  (package "python3" :present)
  (package "python3-pip" :present))
