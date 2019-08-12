(ns de.sveri.shopli.components.components
  (:require
    [com.stuartsierra.component :as component]
    [de.sveri.shopli.components.server :refer [new-web-server]]
    [de.sveri.shopli.components.handler :refer [new-handler]]
    [de.sveri.shopli.components.config :as c]
    [de.sveri.shopli.components.db :refer [new-db]]))


(defn dev-system []
  (component/system-map
    :config (c/new-config (c/prod-conf-or-dev))
    :db (component/using (new-db) [:config])
    :handler (component/using (new-handler) [:config :db])
    :web (component/using (new-web-server) [:handler :config])))


(defn prod-system []
  (component/system-map
    :config (c/new-config (c/prod-conf-or-dev))
    :db (component/using (new-db) [:config])
    :handler (component/using (new-handler) [:config :db])
    :web (component/using (new-web-server) [:handler :config])))
