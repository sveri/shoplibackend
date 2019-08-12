(ns de.sveri.shopli.core
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [de.sveri.shopli.events :as events]
            [de.sveri.shopli.config :as config]
            [de.sveri.shopli.views :as views]
            [de.sveri.shopli.routes :as routes]))




(defn dev-setup []
  (when config/debug?
    (set! *warn-on-infer* true)
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (routes/app-routes)
  (mount-root))
