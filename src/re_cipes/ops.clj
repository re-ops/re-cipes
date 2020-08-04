(ns re-cipes.ops
  "Setting up Re-ops"
  (:require
   [re-cog.resources.git :refer (clone)]
   [re-cog.resources.file :refer (template directory)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.file :refer (directory copy)]))

(require-recipe)

(def-inline repositories
  "Setting up Re-ops repositories"
  []
  (let [{:keys [home user]} (configuration)
        code (<< "~{home}/code")
        root (<< "~{home}/code/re-ops")
        repos ["re-core" "re-pack" "re-dock" "re-cipes" "re-gent"]]
    (directory code :present)
    (directory root :present)
    (doseq [repo repos]
      (let [dest (<< "~{root}/~{repo}")]
        (clone (<< "git://github.com/re-ops/~{repo}.git") dest)))))

(def-inline {:depends #'re-cipes.ops/repositories} configure
  "Set basic Re-ops configuraion files"
  []
  (let [{:keys [home]} (configuration)]
    (copy (<< "~{home}/code/re-ops/re-core/resources/re-ops.edn") (<< "~{home}/.re-ops.edn"))
    (copy (<< "~{home}/code/re-ops/re-core/resources/secrets.edn") "/tmp/secrets.edn")))

(def-inline ssh
  "Configuring ssh access for Re-ops instances"
  []
  (let [{:keys [home user lxd]} (configuration)
        dot-ssh (<< "~{home}/.ssh")
        dest (<< "~{dot-ssh}/config")
        network (first (re-find (re-pattern "(\\d+\\.\\d+\\.\\d+)") (lxd :ipv4-range)))
        args {:user user :key (<< "~{dot-ssh}/id_rsa") :network (<< "~{network}.*")}]
    (directory dot-ssh :present)
    (template "/tmp/resources/templates/ssh/config.mustache" dest args)))

(def-inline {:depends #'re-cipes.ops/repositories} keyz
  "Generate gpg keys"
  []
  (let [gpg-bin "/usr/bin/gpg"]
    (letfn [(generate [input]
              (fn []
                (script
                 (~gpg-bin "--no-default-keyring" "--keyring" "trustedkeys.gpg" "--fingerprint")
                 (~gpg-bin "--no-default-keyring" "--keyring" "trustedkeys.gpg" "--gen-key" "--batch" ~input))))
            (export [passphrase public secret]
                    (fn []
                      (script
                       (~gpg-bin "--no-default-keyring" "--keyring" "trustedkeys.gpg" "--export" ">>" ~public)
                       (~gpg-bin "--no-default-keyring" "--keyring" "trustedkeys.gpg" "--export-secret-keys" "--batch" "--yes" ~passphrase "--pinentry-mode" "loopback" ">>" ~secret))))]
      (let [{:keys [home user gpg]} (configuration)
            input "/tmp/resources/gpg-input"
            dest (<< "~{home}/code/re-ops/re-core/keys")
            passphrase (<< "--passphrase='~(gpg :pass)'")]
        (template "/tmp/resources/templates/gpg/batch.mustache" input gpg)
        (run (generate input))
        (directory dest :present)
        (run (export passphrase (<< "~{dest}/public.gpg") (<< "~{dest}/secret.gpg")))))))
