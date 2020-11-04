(ns re-cipes.apps.letsencrypt
  "A letsencrypt client"
  (:require
   [re-cipes.access :refer (permissions)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.exec :refer (run)]
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cog.resources.file :refer (copy directory chmod)]
   [re-cog.resources.download :refer (download)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline dehydrated
  "Setting up dehydrated"
  []
  (let [repo "https://github.com/lukas2511/dehydrated.git"
        dest "/srv/dehydrated"]
    (doseq [dep ["openssl" "curl" "sed" "grep" "git"]]
      (package dep :present))
    (set-file-acl "re-ops" "rwx" "/srv")
    (directory dest :present)
    (clone repo dest)
    (chmod "/srv/dehydrated/dehydrated" "a+wrx" {})))

(def-inline {:depends [#'re-cipes.access/permissions #'re-cipes.apps.letsencrypt/dehydrated]} dns-lexicon
  "lexicon setup"
  []
  (letfn [(pip []
            (script
             ("/usr/bin/pip3" "install" "requests[security]")
             ("/usr/bin/pip3" "install" "dns-lexicon")))]
    (let [{:keys [home]} (configuration)
          hook "dehydrated.default.sh"
          url (<< "https://raw.githubusercontent.com/AnalogJ/lexicon/master/examples/~{hook}")
          dest "/srv/dehydrated"
          sum "36fc962978becb696a0f0980a08ec09827440355f6c2c520f96bd5c64831a87f"]
      (download url (<< "~{dest}/~{hook}") sum)
      (chmod  (<< "~{dest}/~{hook}") "a+wrx" {})
      (doseq [dep ["build-essential" "python-dev" "curl" "libffi-dev" "libssl-dev" "python3-pip"]]
        (package dep :present))
      (run pip)
      (symlink "/usr/local/bin/lexicon" (<< "~{home}/.local/bin/lexicon")))))
