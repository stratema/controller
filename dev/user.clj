(ns user
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [com.stuartsierra.component :as component]
            [controller.core :as cc]
            [controller.component-test :as ct]))

(comment
  "Run through component system start and stop"
  (do

    (def state (atom nil))
    (def obj-name (str "controller:name=Controller-" (java.util.UUID/randomUUID)))
    (def ctl (cc/->Controller state))

    (cc/register-controller ctl obj-name)
    ;; (cc/unregister-controller obj-name)

    (cc/init-system ctl #(ct/new-test-system (ct/test-system)))

    (cc/start-system ctl)

    (cc/stop-system ctl)
    (cc/unregister-controller obj-name)

    ))
