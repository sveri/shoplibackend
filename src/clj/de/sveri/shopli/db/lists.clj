(ns de.sveri.shopli.db.lists
  (:require [clojure.java.jdbc :as j]))

(defn create-list [db name owner-id]
  (first (j/insert! db :lists {:name name :owner owner-id})))

(defn get-lists [db mobile-clients-id]
  (j/query db ["select l.*, mcl.shared_by, mcl.shared_with from mobile_clients_list mcl
               join lists l on mcl.list_id = l.id
               where mcl.mobile_clients_id = ?" mobile-clients-id]))

