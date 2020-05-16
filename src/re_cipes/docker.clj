(ns re-cipes.docker
  "Docker setup"
  (:require
   [re-cipes.access :refer (permissions)]
   [re-cog.resources.exec :refer [run]]
   [re-cog.common.recipe :refer (require-recipe)]
   [clojure.core.strint :refer (<<)]
   [re-cog.resources.file :refer (file line)]
   [re-cog.resources.package :refer (package key-file update-)]
   [re-cog.resources.user :refer (group-add)]
   [re-cog.resources.download :refer (download)]
   [re-cog.facts.config :refer (configuration)]))

(require-recipe)

(def-inline prequisits
  "docker install prequisits"
  []
  (doseq [p ["apt-transport-https" "ca-certificates" "gnupg-agent" "software-properties-common"]]
    (package p :present)))

(def-inline {:depends [#'re-cipes.access/permissions #'re-cipes.docker/prequisits]} install
  "install docker"
  []
  (let [sources "/etc/apt/sources.list.d"
        listing (<< "~{sources}/docker-ce.list")
        url "https://download.docker.com/linux/ubuntu/gpg"
        keyrings "/usr/share/keyrings"
        key "docker-gpg"
        repo "deb [arch=amd64] https://download.docker.com/linux/ubuntu bionic stable"]
    (download url (<< "~{keyrings}/~{key}") "1500c1f56fa9e26b9b8f42452a553675796ade0807cdce11975eb98170b3a570")
    (key-file (<< "~{keyrings}/~{key}"))
    (file listing :present)
    (line listing repo :present)
    (update-)
    (doseq [p ["docker-ce" "docker-ce-cli" "containerd.io" "docker-compose"]]
      (package p :present))))

(def-inline {:depends #'re-cipes.docker/install} passwordless
  "Enable passwordless docker"
  []
  (let [{:keys [user]} (configuration)]
    (group-add "docker" user)))
