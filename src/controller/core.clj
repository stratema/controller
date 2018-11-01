(ns controller.core
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component])
  (:import [java.lang.management
            ManagementFactory]
           [javax.management
            Attribute
            AttributeList
            DynamicMBean
            MBeanAttributeInfo
            MBeanInfo
            MBeanOperationInfo
            MBeanParameterInfo
            MBeanServer
            MBeanServerConnection
            ObjectName
            RuntimeMBeanException]))

;; has an atom containing the latest system-map instance
;; exposes start-system, stop-system, restart-system

;; what about component level functions?

;; SystemControls
;; ComponentControls

;; component - cmp
;; integrant - itg
;; mount     - mnt

(defprotocol SystemCommands
  (init-system [this init-fn])
  (start-system [this])
  (stop-system [this]))

(deftype Controller [system-ref]
  SystemCommands
  (init-system [this init-fn]
    (reset! system-ref (init-fn))
    nil)

  (start-system [this]
    (swap! system-ref component/start)
    nil)

  (stop-system [this]
    (swap! system-ref component/stop)
    nil)

  ;; (into-array MBeanAttributeInfo
  ;;   [(MBeanAttributeInfo. "Status"
  ;;                         (.getName (class String))
  ;;                         "Status"
  ;;                         true
  ;;                         true
  ;;                         false)])
  DynamicMBean
  (getMBeanInfo [this]
    (MBeanInfo. (.. this getClass getName)
                "Provides controls for externally restarting a System"
                nil                     ; Attributes
                nil                     ; Constructors
                (into-array MBeanOperationInfo
                            (->> SystemCommands :sigs vals
                                 (map (fn [{:keys [name doc]}]
                                        (MBeanOperationInfo.
                                         (str name)
                                         (str (or doc name))
                                         nil nil
                                         MBeanOperationInfo/ACTION)))))
                nil))

  (invoke [this action-name params signature]
    (condp = action-name
      "start-system" (start-system this)
      "stop-system" (stop-system this)
      (log/errorf "action-name '%s' not recognised" action-name))))

(defn register-controller
  ([controller]
   (register-controller controller "controller:name=Controller"))
  ([controller object-name]
   (let [mbs (ManagementFactory/getPlatformMBeanServer)]
     (.registerMBean mbs controller (ObjectName. object-name)))))

(defn unregister-controller [object-name]
  (let [mbs (ManagementFactory/getPlatformMBeanServer)]
    (.unregisterMBean mbs (ObjectName. object-name))))
