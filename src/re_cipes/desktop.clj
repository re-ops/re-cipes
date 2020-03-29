(ns re-cipes.desktop
  "Setting up an Ubuntu desktop"
  (:require
   [re-cog.resources.file :refer (directory)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.dconf :refer (load-)]))

(require-recipe)

(def-inline purge-folders
  "Clearing un-used folders in ~"
  []
  (let [{:keys [home]} (configuration)]
    (if (ubuntu-desktop?)
      (doseq [lib ["Music" "Pictures" "Public" "Templates" "Videos"]]
        (directory (<< "~{home}/~{lib}") :absent))
      {})))

(def-inline mate-shortcuts
  "Creating keyboard shortcuts"
  []
  (if (ubuntu-desktop?)
    (let [virt {:action "/usr/bin/virt-manager" :binding "<Primary><Alt>v" :name "virt-manager"}
          term {:action "/usr/bin/mate-terminal" :binding "<Primary><Alt>t" :name "terminal"}]
      (load- "/org/mate/desktop/keybindings/" {"custom1" virt})
      (load- "/org/mate/desktop/keybindings/" {"custom2" term}))
    {}))
