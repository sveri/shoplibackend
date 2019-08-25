(ns de.sveri.shopli.db.lists
  (:require [clojure.java.jdbc :as j]))

(defn create-list [db name mobile-clients-id]
  (first (j/insert! db :lists {:name name :mobile_clients_id mobile-clients-id})))

(defn get-lists [db mobile-clients-id]
  (j/query db ["select * from lists where mobile_clients_id = ? order by name asc" mobile-clients-id]))

