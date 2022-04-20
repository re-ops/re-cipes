(ns re-cipes.apps.hydroxide
  "https://github.com/emersion/hydroxide setup"
  (:require
   [re-cipes.hardening]
   [re-cipes.development.go :refer (install-go)]
   [re-cipes.access :refer (permissions)]
   [re-cog.resources.git :refer (clone)]
   [re-cog.resources.ufw :refer (add-rule)]
   [re-cog.resources.service :refer (on-boot)]
   [re-cog.resources.file :refer (copy directory chmod line file)]
   [re-cog.resources.systemd :refer (set-service)]
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline {:depends [#'re-cipes.development.go/install-go  #'re-cipes.access/permissions]} setup
  "Setting up hydroxide"
  []
  (let [{:keys [home]} (configuration)
        src "https://github.com/emersion/hydroxide.git"
        dest "/opt/hydroxide"
        go (<< "~{home}/go/bin/go")]
    (letfn [(build []
              (script
               ("cd" ~dest "&&" ~go "build" "./cmd/hydroxide")))]
      (clone src dest {})
      (run build))))

(def-inline hydroxide-smtp-service
  "Setting up hydroxide smtp service, make sure to run:

     hydroxide auth <username>

    before starting this service.
  "
  []
  (let [{:keys [smtp-host tls-cert tls-key]} (configuration :hydroxide)
        cmd (<< "/opt/hydroxide/hydroxide smtp -smtp-host ~{smtp-host} -tls-cert ~{tls-cert} -tls-key ~{tls-key}")
        opts {:wants "basic.target"
              :after "basic.target network.target"
              :restart "always"
              :stop "/bin/kill -HUP $MAINPID"
              :wanted-by "multi-user.target"}]
    (set-service "hydroxide" "Hydroxide Proton mail bridge" cmd opts)))
