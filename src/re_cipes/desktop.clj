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

(def-inline mate-terminal
  "Mate terminal settings"
  []
  (if (ubuntu-desktop?)
    (let [pallet "#2E2E34343636:#CCCC00000000:#4E4E9A9A0606:#C4C4A0A00000:#34346565A4A4:#757550507B7B:#060698209A9A:#D3D3D7D7CFCF:#555557575353:#EFEF29292929:#8A8AE2E23434:#FCFCE9E94F4F:#72729F9FCFCF:#ADAD7F7FA8A8:#3434E2E2E2E2:#EEEEEEEEECEC"
          colors {:palette pallet
                  :bold-color "#000000000000"
                  :background-color "#000000000000"
                  :foreground-color "#FFFFFFFFFFFF"}
          profile {:visible-name "Default"
                   :scrollbar-position "hidden"
                   :palette pallet
                   :default-show-menubar "false"
                   :use-theme-colors "false"
                   :allow-bold "false"}
          settings {"keybindings" {:select-all "disabled"}
                    "profiles/default" (merge profile colors)}]
      (load- "/org/mate/terminal/" settings))
    {}))
