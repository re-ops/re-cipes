(ns re-cipes.re-ops.core
  "Setting up Re-core"
  (:require
   [re-cog.resources.git :refer (clone)]
   [re-cog.resources.file :refer (template directory edn-set chown chmod)]
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
        repos ["re-core" "re-pack" "re-cipes" "re-gent"]]
    (directory code :present)
    (directory root :present)
    (doseq [repo repos]
      (let [dest (<< "~{root}/~{repo}")]
        (clone (<< "git://github.com/re-ops/~{repo}.git") dest {})
        (chown dest user user {:recursive true})))))

(def-inline {:depends #'re-cipes.re-ops.core/repositories} configure
  "Set basic Re-ops configuraion files"
  []
  (let [{:keys [home user lxd gpg]} (configuration)]
    (copy (<< "~{home}/code/re-ops/re-core/resources/re-ops.edn") (<< "~{home}/.re-ops.edn"))
    (copy (<< "~{home}/code/re-ops/re-core/resources/secrets.edn") "/tmp/secrets.edn")
    (edn-set "/tmp/secrets.edn" [:pgp :pass] (gpg :pass))
    (when (lxd :password)
      (edn-set "/tmp/secrets.edn" [:lxc :pass] (lxd :password)))
    (chown "/tmp/secrets.edn" user user {})
    (chown (<< "~{home}/.re-ops.edn") user user {})))

(def-inline ssh
  "Configuring ssh access for Re-ops instances"
  []
  (let [{:keys [home user lxd]} (configuration)
        dot-ssh (<< "~{home}/.ssh")
        dest (<< "~{dot-ssh}/config")
        private (<< "~{dot-ssh}/id_rsa")
        network (first (re-find (re-pattern "(\\d+\\.\\d+\\.\\d+)") (lxd :ipv4-range)))
        args {:user user :key (<< "~{dot-ssh}/id_rsa") :network (<< "~{network}.*")}]
    (letfn [(generate []
              (script
               ("ssh-keygen" "-t" "rsa" "-f" ~private "-q" "-P" "''")))]
      (directory dot-ssh :present)
      (when-not (exists? private)
        (run generate))
      (template "/tmp/resources/templates/ssh/config.mustache" dest args)
      (chown dot-ssh user user {:recursive true}))))

(def-inline {:depends #'re-cipes.re-ops.core/repositories} keyz
  "Generate gpg keys"
  []
  (let [gpg-bin "/usr/bin/gpg"
        {:keys [home user gpg]} (configuration)
        input "/tmp/resources/gpg-input"
        dest (<< "~{home}/code/re-ops/re-core/keys")
        pass (<< "--passphrase='~(gpg :pass)'")
        public (<< "~{dest}/public.gpg")
        private (<< "~{dest}/secret.gpg")]
    (letfn [(generate []
              (script
               (~gpg-bin "--no-default-keyring" "--keyring" "keys.gpg" "--fingerprint")
               (~gpg-bin "--no-default-keyring" "--keyring" "keys.gpg" "--gen-key" "--batch" ~input)))
            (export []
                    (script
                     (~gpg-bin "--no-default-keyring" "--keyring" "keys.gpg" "--export" ">>" ~public)
                     (~gpg-bin "--no-default-keyring" "--keyring" "keys.gpg" "--export-secret-keys" "--batch" "--yes" ~pass "--pinentry-mode" "loopback" ">>" ~private)))]
      (if-not (and (exists? public) (exists? private))
        (do
          (template "/tmp/resources/templates/gpg/batch.mustache" input gpg)
          (run generate)
          (directory dest :present)
          (run export)
          (chown dest user user {:recursive true}))
        {}))))
