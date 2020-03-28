(ns re-cipes.k8s
  "k8s setup"
  (:require
   [re-cog.resources.exec :refer [run]]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.file :refer (rename symlink chmod)]
   [re-cog.resources.archive :refer (untar bzip2)]))

(require-recipe)

(def-inline minikube
  "Setting minikube"
  []
  (let [version "0.9.1"
        release (<< "restic_~{version}_linux_amd64")
        expected "eabd027438953d29a4b0f7b810c801919cc13bef3ebe7aff08c9534ac2b091ab"
        url "https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64"]
    (download url "/usr/bin/minikube" expected)
    (chmod "/usr/bin/minikube" "0755")))

(def-inline kubectl
  "Setting minikube"
  []
  (let [version "v1.17.0"
        url (<< "https://storage.googleapis.com/kubernetes-release/release/~{version}/bin/linux/amd64/kubectl")
        expected "6e0aaaffe5507a44ec6b1b8a0fb585285813b78cc045f8804e70a6aac9d1cb4c"]
    (download url "/usr/bin/kubectl" expected)
    (chmod "/usr/bin/kubectl" "0755")))
