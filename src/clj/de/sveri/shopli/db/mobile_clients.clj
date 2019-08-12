(ns de.sveri.shopli.db.mobile-clients
  (:require [clojure.java.jdbc :as j]))


(defn create [db device-id app-id]
  (first (j/insert! db :mobile_clients {:device_id device-id :app_id app-id})))

(defn get-or-create-client-by-app-device-id [db device-id app-id]
  (let [res (j/query db ["select * from mobile_clients where device_id = ? and app_id = ?" device-id app-id])]
    (if (empty? res)
      (create db device-id app-id)
      (first res))))


