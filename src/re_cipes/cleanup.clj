(ns re-cipes.cleanup
  (:require
   [re-cog.resources.file :refer (directory)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.facts.config :refer (configuration)]))

(require-recipe)

(def-inline purge-folders
  "Clearing un-used folders in ~"
  []
  (let [{:keys [home]} (configuration)]
    (if (ubuntu-desktop?)
      (doseq [lib ["Music" "Pictures" "Public" "Templates" "Videos"]]
        (directory (<< "~{home}/~{lib}") :absent))
      {})))
