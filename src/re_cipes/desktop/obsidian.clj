(ns re-cipes.desktop.obsidian
  (:require
   [re-cipes.access :refer (permissions)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.file :refer (directory chmod template copy)]))

(require-recipe)

(def-inline install
  "Set up Obsidian"
  []
  (let [{:keys [home obsidian]} (configuration)
        version "0.11.9"
        binary (<< "Obsidian-~{version}.AppImage")
        url (<< "https://github.com/obsidianmd/obsidian-releases/releases/download/v~{version}/~{binary}")]
    (directory (<< "~{home}/bin") :present)
    (download url (<< "~{home}/bin/Obsidian") "b4ec9b68aac57a67479b689542cdb683c62f3160934122e67c7ee23643baf3cd")
    (chmod  (<< "~{home}/bin/Obsidian") "+x" {})))

(def-inline desktop-entry
  "Set up desktop entry"
  []
  (let [{:keys [home]} (configuration)
        share (<< "~{home}/.local/share")
        applications (<< "~{share}/applications")
        icon-url "https://avatars.githubusercontent.com/u/65011256?s=200&v=4"
        icon (<< "~{home}/.config/obsidian.png")
        obsidian {:name "Obsidian" :bin (<< "~{home}/bin/Obsidian %u") :icon icon :categories "Office"}]
    (directory share :present)
    (directory applications :present)
    (download icon-url icon "7663d1284daafdbcd61ac9e6955bd3720961cf71cad6275aa8d696b47416cb02")
    (template "/tmp/resources/templates/desktop/desktop-entry.mustache" (<< "~{applications}/obsidian.desktop") obsidian)
    (copy (<< "~{applications}/obsidian.desktop") (<< "~{home}/Desktop/obsidian.desktop"))
    (chmod (<< "~{home}/Desktop/obsidian.desktop") "+x" {})))
