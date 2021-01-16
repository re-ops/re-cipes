(ns re-cipes.apps.tiddlywiki
  (:require
   [re-cipes.hardening]
   [re-cipes.docker.nginx]
   [re-cog.resources.exec :refer (run)]
   [re-cog.resources.systemd :refer (set-service)]
   [re-cog.resources.nginx :refer (site-enabled)]
   [re-cog.resources.ufw :refer (add-rule)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.file :refer (symlink directory)]))

(require-recipe)

(def-inline node
  "Setting up nodejs"
  []
  (package "nodejs" :present)
  (package "npm" :present))

(def-inline {:depends #'re-cipes.apps.tiddlywiki/node} tiddlywiki
  "Setting up tiddlywiki"
  []
  (let [{:keys [home user]} (configuration)
        bin (<< "~{home}/bin/tiddlywiki")
        wiki (<< "~{home}/~{user}")
        exec (<< "~{bin} ~{wiki} --listen host=0.0.0.0")]
    (letfn [(install []
              (script ("/usr/bin/npm" "install" "tiddlywiki")))
            (init []
                  (script
                   (~bin ~wiki "--init" "server")))]
      (run install)
      (directory (<< "~{home}/bin/") :present)
      (symlink bin (<< "~{home}/node_modules/tiddlywiki/tiddlywiki.js"))
      (when-not (fs/exists? wiki)
        (run init))
      (set-service "tiddlywiki" "Tiddlywiki service" exec
                   {:restart true :user user :cwd home
                    :hardening {:private-tmp 1 :no-new-privileges 1 :private-users 1}}))))

(def-inline {:depends [#'re-cipes.docker.nginx/get-source #'re-cipes.hardening/firewall]} nginx
  "Nginx site enable"
  []
  (let [external-port 8443
        {:keys [nginx]} (configuration)]
    (site-enabled nginx "tiddlywiki" external-port 8080 {:basic-auth true})
    (add-rule external-port :allow {})))
