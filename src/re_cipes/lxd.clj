(ns re-cipes.lxd
  "Setting up lxd"
  (:require
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.exec :refer [run]]))

(require-recipe)

(def-inline lxd
  "Installing lxd"
  []
  (letfn [(init []
            (script ("sudo" "/usr/bin/lxd" "init" "--auto")))]
    (package "lxd" :present)
    (package "zfsutils-linux" :present)
    (run init)))
