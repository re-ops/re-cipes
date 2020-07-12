# Intro

Re-cipes a collection of recipes for provisioing Linux systems, supporting remote execution by [Re-gent](https://github.com/re-ops/re-gent), they use resources and facts from  [Re-cog](https://github.com/re-ops/re-cog).

It is a part of the [Re-ops](https://re-ops.github.io/re-ops/) project that offers a live coding environment for managing and information gathering from remote systems.

[![Build Status](https://travis-ci.org/re-ops/re-cipes.png)](https://travis-ci.org/re-ops/re-cipes)

# Usage

One of the key benefits of Re-ops is the ability to run and test your provisioning logic directly from the REPL, we can test a single function:

```clojure
; mainly useful during development
(run-hosts (hosts ip :hostname) re-cipes.python/python-3.7 [] [5 :minute])
```

Or provision a system using a [profile](https://github.com/re-ops/re-cipes/blob/master/src/re_cipes/profiles.clj) (collection of recipe namespaces):

```clojure
; Basic profiles
(def ^{:doc "Minimal set of recipes"}
  lean #{'re-cipes.access 're-cipes.shell 're-cipes.tmux 're-cipes.hardening 're-cipes.cleanup})

(def ^{:doc "Base setup common to all plans (shell, hardening, osquery etc.)"}
  base (into #{'re-cipes.osquery 're-cipes.monitoring} lean))
```

We now use this profile to creating a Re-core type inside the REPL:

```clojure
; Define a type
(create cog 're-cipes.profiles/base default-src :base "A machine that uses the base profile")
; Create a new VM
(create kvm defaults local c1-medium :base "An example machine that uses the base profile")
; Deploy Re-gent into the newly create VM
(deploy (hosts (matching (*1)) :ip) "/home/user/code/re-ops/re-gent/target/re-gent")
; Provision the system using the profile
(provision (matching (*1)))
```

Lastly we can also provision a profile using the standalone binary:

```bash
$ wget -q https://github.com/re-ops/re-cipes/releases/download/0.1.26/re-cipes -P /tmp
$ sudo /tmp/re-cipes prov -p 're-cipes.profiles/elasticsearch
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
