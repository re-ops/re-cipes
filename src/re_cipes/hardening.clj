(ns re-cipes.hardening
  "Hardedning and security tools"
  (:require
   [re-cipes.access]
   [re-cog.resources.exec :refer [run]]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.file :refer (file line line-set copy template uncomment)]
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cog.resources.service :refer (service on-boot)]
   [re-cog.resources.ufw :refer (set-state add-rule reset)]))

(require-recipe)

(def-inline {:depends #'re-cipes.access/permissions} ssh-haredning
  "SSH server hardening"
  []
  (package "openssh-server" :present)
  (package "rng-tools" :present)
  (set-file-acl "re-ops" "rwx" "/etc/ssh/sshd_config")
  (uncomment "/etc/ssh/sshd_config" "PermitRootLogin" "#")
  (line-set "/etc/ssh/sshd_config" "PermitRootLogin" "no" " ")
  (uncomment "/etc/ssh/sshd_config" "PasswordAuthentication" "#")
  (line-set "/etc/ssh/sshd_config" "PasswordAuthentication" "no" " ")
  (line-set "/etc/ssh/sshd_config" "X11Forwarding" "no" " ")
  (line "/etc/ssh/sshd_config" "\nUseDns no" :present)
  (service "ssh" :restart))

(def-inline {:depends #'re-cipes.access/permissions} systcl-hardening
  "Hardening network, cis using sysctl"
  []
  (letfn [(sysctl-reload []
            (script ("sudo" "/usr/sbin/sysctl" "--system")))]
    (let [target "/etc/sysctl.d/10-network-hardening.conf"
          root "/tmp/resources/templates/hardening"]
      (set-file-acl "re-ops" "rwx" "/etc/sysctl.d")
      (copy (<< "~{root}/network.conf") target)
      (copy (<< "~{root}/cis.conf") target)
      (run sysctl-reload))))

(def-inline disable-bluetooth
  "Disabling bluetooth on desktop machines"
  []
  (if (ubuntu-desktop?)
    (on-boot "bluetooth" :disable)
    {}))

(def-inline firewall
  "Enabling firewall"
  []
  (reset)
  (add-rule 22 :allow {})
  (set-state :enable))

(def-inline cis-packages
  "CIS benchmark related settings"
  []
  (package "aide" :present))

(def-inline {:depends #'re-cipes.access/permissions} disable-fs
  "Disable loading of unused Filesystems cis 1.1.1.1-5"
  []
  (let [source "/tmp/resources/templates/hardening/disable-fs.mustache"
        fs (map (fn [v] {:fs v}) ["freevxfs" "jffs2" "hfs" "hfsplus" "udf"])]
    (template source "/etc/modprobe.d/fs-disable.conf" {:filesystems fs})))

(def-inline auditing
  "Enabling auditd cis: 4.1.2"
  []
  (package "auditd" :present)
  (on-boot "auditd" :enable))

(def-inline {:depends #'re-cipes.access/permissions} security-limits
  "Disabling hard core dumps cis: 1.5.1"
  []
  (let [dest "/etc/security/limits.d/core_dump.conf"]
    (file dest :present)
    (line dest "* hard core 0" :present)))

(def-inline package-purge
  "Clearing unsafe/redundant packages"
  []
  (package "telnet" :absent)
  (package "popularity-contest" :absent)
  (package "apport" :absent)
  (package "whoopsie" :absent)
  (package "whoopsie-preferences" :absent))

(def-inline purge-cups
  "purging cups"
  []
  (package "cups" :absent)
  (package "cups-browsed" :absent)
  (package "cups-common" :absent)
  (package "cups-server-common" :absent)
  (package "cups-daemon" :absent)
  (package "cups-ipp-utils" :absent))
