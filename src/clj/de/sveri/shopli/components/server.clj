(ns de.sveri.shopli.components.server
  (:require [org.httpkit.server :as hkit]
            [com.stuartsierra.component :as component]
            ;[immutant.web :as web]
            [clojure.tools.logging :as log]))

(defrecord WebServer [handler config]
  component/Lifecycle
  (start [component]
    (let [handler (:handler handler)
          port (get-in config [:config :port] 3000)
          ;server (web/run handler :port port)]
          server (hkit/run-server handler
                                  {:port         port
                                   :error-logger (fn [msg ex]
                                                   (log/error msg)
                                                   (log/error (-> ex Throwable->map clojure.main/ex-triage clojure.main/ex-str))
                                                   (log/error ex))})]
      (log/info "started server")
      (assoc component :server server)))
  (stop [component]
    (when-let [server (:server component)]
      (log/info "stopping server")
      (server))
      ;(web/stop))
    (assoc component :server nil)))

(defn new-web-server []
  (map->WebServer {}))
