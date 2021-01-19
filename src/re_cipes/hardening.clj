(ns re-cipes.hardening
  "Hardedning and security tools"
  (:require
   [re-cog.resources.exec :refer [run]]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.file :refer (line line-set copy template)]
   [re-cog.resources.permissions :refer (set-file-acl)]
   [re-cog.resources.service :refer (service on-boot)]
   [re-cog.resources.ufw :refer (set-state add-rule reset)]))

(require-recipe)

(def-inline ssh-haredning
  "SSH server hardening"
  []
  (package "openssh-server" :present)
  (package "rng-tools" :present)
  (set-file-acl "re-ops" "rwx" "/etc/ssh/sshd_config")
  (line "/etc/ssh/sshd_config" "PermitRootLogin" :uncomment :with "#")
  (line-set "/etc/ssh/sshd_config" "PermitRootLogin" "no" " ")
  (line "/etc/ssh/sshd_config" "PasswordAuthentication" :uncomment :with "#")
  (line-set "/etc/ssh/sshd_config" "PasswordAuthentication" "no" " ")
  (line-set "/etc/ssh/sshd_config" "X11Forwarding" "no" " ")
  (line "/etc/ssh/sshd_config" "\nUseDns no" :present)
  (service "ssh" :restart))

(def-inline systcl-hardening
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

(def-inline disable-fs
  "Disable loading of unused Filesystems cis 1.1.1.1-5"
  []
  (let [source "/tmp/resources/templates/hardening/disable-fs.mustache"
        fs (map (fn [v] {:fs v}) ["freevxfs" "jffs2" "hfs" "hfsplus" "udf"])]
    (template  source "/etc/modprobe.d/fs-disable.conf" {:filesystems fs})))

(def-inline auditing
  "Enabling auditd cis: 4.1.2"
  []
  (package "auditd" :present)
  (on-boot "auditd" :enable))
