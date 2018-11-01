(ns controller.component-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [com.stuartsierra.component :as component]
            [controller.core :as cc]))

;; Define a system with two components, one depending on the other

;; Write a test that shows the system can be started, registered with
;; jmx and then restarted through a jmx connection
(defrecord TestComponent [label]
  component/Lifecycle
  (start [this]
    (println "Starting" label)
    (assoc this :state :started))

  (stop [this]
    (println "Stopping" label)
    (assoc this :state :stopped)))

(defn test-system []
  {:components
   {:component-a (map->TestComponent {:label "Component A"})
    :component-b (map->TestComponent {:label "Component B"})}
   :dependencies
   {:component-a [:component-b]}})

(defn new-test-system [{:keys [components dependencies]}]
  (-> (component/map->SystemMap components)
      (component/system-using dependencies)))


(deftest system-restart-test
  (let [state (atom nil)
        obj-name (str "controller:name=Controller-" (java.util.UUID/randomUUID))
        ctl (cc/->Controller state)]
    (cc/init-system ctl #(new-test-system (test-system)))
    (cc/register-controller ctl obj-name)
    (cc/start-system ctl)
    (cc/stop-system ctl)
    (cc/unregister-controller obj-name)
    (is (= :stopped (-> state deref :component-a :state)))
    (is (= :stopped (-> state deref :component-b :state)))))
