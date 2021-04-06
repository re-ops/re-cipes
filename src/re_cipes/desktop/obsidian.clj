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
        icon-url "https://pbs.twimg.com/profile_images/1269490744609341442/MaweGLMN_400x400.png"
        icon (<< "~{home}/Pictures/obsidian.png")
        obsidian {:name "Obsidian" :bin (<< "~{home}/bin/Obsidian %u") :icon icon :categories "Office"}]
    (directory share :present)
    (directory applications :present)
    (download icon-url icon "7ed4b0c4eb7a4d3a42ac533f22f25e29ad7f9b42fde15b87e9928a6e1db2c8a3")
    (template "/tmp/resources/templates/desktop/desktop-entry.mustache" (<< "~{applications}/obsidian.desktop") obsidian)
    (copy (<< "~{applications}/obsidian.desktop") (<< "~{home}/Desktop/obsidian.desktop"))
    (chmod (<< "~{home}/Desktop/obsidian.desktop") "+x" {})))
