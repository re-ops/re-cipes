(ns re-cipes.hardening
  "Hardedning and security tools"
  (:require
   [re-cog.resources.exec :refer [run]]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.facts.config :refer (configuration)]
   [re-cog.resources.file :refer (line line-set uncomment copy)]
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cog.resources.service :refer (service on-boot)]))

(require-recipe)

(def-inline ssh-haredning
  "SSH server hardening"
  []
  (package "openssh-server" :present)
  (package "rng-tools" :present)
  (set-file-acl "re-ops" "rwX" "/etc/ssh/sshd_config")
  (uncomment "/etc/ssh/sshd_config" "PermitRootLogin" "#")
  (line-set "/etc/ssh/sshd_config" "PermitRootLogin" "no" " ")
  (uncomment "/etc/ssh/sshd_config" "PasswordAuthentication" "#")
  (line-set "/etc/ssh/sshd_config" "PasswordAuthentication" "no" " ")
  (line-set "/etc/ssh/sshd_config" "X11Forwarding" "no" " ")
  (line "/etc/ssh/sshd_config" "\nUseDns no" :present)
  (service "ssh" :restart))

(def-inline networking
  "Hardening network"
  []
  (letfn [(sysctl-reload [target]
            (fn []
              (script ("/sbin/sysctl" "-e" "-p" ~target))))]
    (let [target "/etc/sysctl.d/10-network-hardening.conf"]
      (set-file-acl "re-ops" "rwX" "/etc/sysctl.d")
      (copy "/tmp/resources/networking/harden.conf" target)
      (run (sysctl-reload target)))))

(def-inline disable-bluetooth
  "Disabling bluetooth on desktop machines"
  []
  (if (ubuntu-desktop?)
    (on-boot "bluetooth" :disable)
    {}))
