(ns re-cipes.k8s
  "k8s setup"
  (:require
   [re-cipes.access :refer (permissions)]
   [re-cog.common.recipe :refer (require-recipe)]
   [re-cog.resources.download :refer (download)]
   [re-cog.resources.file :refer (rename symlink chmod)]))

(require-recipe)

(def-inline {:depends #'re-cipes.access/permissions} minikube
  "Setting minikube"
  []
  (let [version "0.9.1"
        release (<< "restic_~{version}_linux_amd64")
        expected "81d77d1babe63be393e0a3204aac7825eb35e0fdf58ffefd9f66508a43864866"
        url "https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64"]
    (download url "/usr/bin/minikube" expected)
    (chmod "/usr/bin/minikube" "0755" {})))

(def-inline kubectl
  "Setting minikube"
  []
  (let [version "v1.17.0"
        url (<< "https://storage.googleapis.com/kubernetes-release/release/~{version}/bin/linux/amd64/kubectl")
        expected "6e0aaaffe5507a44ec6b1b8a0fb585285813b78cc045f8804e70a6aac9d1cb4c"]
    (download url "/usr/bin/kubectl" expected)
    (chmod "/usr/bin/kubectl" "0755" {})))
