# Intro

Re-cipes is a collection of recipes that can be remotely executed by [Re-gent](https://github.com/re-ops/re-gent) they use resources and facts from  [Re-cog](https://github.com/re-ops/re-cog).

It is a part of the [Re-ops](https://re-ops.github.io/re-ops/) project that offers a live coding environment for managing and information gathering from remote systems.

[![Build Status](https://travis-ci.org/re-ops/re-cipes.png)](https://travis-ci.org/re-ops/re-cipes)

# Usage

We can either launch them ad-hoc:

```clojure
; mainly useful during development
(run-hosts (hosts ip :hostname) re-cipes.python/python-3.7 [] [5 :minute])
```

Or uses a profile (a collection of recipe namespaces):

```clojure
; Basic profiles
(def ^{:doc "Minimal set of recipes"}
  lean #{'re-cipes.access 're-cipes.shell 're-cipes.tmux 're-cipes.hardening 're-cipes.cleanup})

(def ^{:doc "Base setup common to all plans (shell, hardening, osquery etc.)"}
  base (into #{'re-cipes.osquery 're-cipes.monitoring} lean))

```

And use this profile when creating a Re-core type inside the Repl:

```clojure
; Define a type
(create cog 're-cipes.profiles/base default-src :base "A machine that uses the base profile")
; Create a VM
(create kvm defaults local c1-medium :vuepress "vuepress")
; Deploy Re-gent into the newly create VM
(deploy (hosts (matching (*1)) :ip) "/home/user/code/re-ops/re-gent/target/re-gent")
; Provision the system using the profile
(provision (matching (*1)))
```

# Copyright and license

Copyright [2020] [Ronen Narkis]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
