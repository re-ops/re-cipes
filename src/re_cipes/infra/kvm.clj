(ns re-cipes.infra.kvm
  "Setting up hypervisors"
  (:require
   [re-cog.common.recipe :refer (require-recipe)]))

(require-recipe)

(def-inline kvm
  "Installing KVM"
  []
  (package "libvirt-daemon-system" :present)
  (package "libvirt-clients" :present)
  (package "qemu-kvm" :present)
  (package "virtinst" :present)
  (package "bridge-utils" :present)
  (if (ubuntu-desktop?)
    (package "virt-manager" :present)))
