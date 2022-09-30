(ns re-cipes.security.bitwarden
  (:require
   [re-cog.resources.file :refer (chmod)]
   [re-cog.resources.archive :refer (unzip)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.download :refer (download)]))

(require-recipe)

(def-inline {:depends #'re-cipes.access/permissions} install
  "Setting up bw cli"
  []
  (let [version "2022.9.0"
        expected "9a6687dd33aec02239619b23bcede31195028bb01e022b82505031da638d5a29"
        artifact (<< "bw-linux-~{version}.zip")
        dest (<< "/usr/src/~{artifact}")
        url (<< "https://github.com/bitwarden/clients/releases/download/cli-v~{version}/~{artifact}")]
    (download url dest expected)
    (unzip dest "/usr/local/bin/")
    (chmod  "/usr/local/bin/bw" "+x" {})))
