(ns de.sveri.shopli.db.shared-list-to-user
  (:require [clojure.java.jdbc :as j]))

(defn create-entry [db mobile-clients-id list-id]
  (first (j/insert! db :shared_list_to_user {:mobile_clients_id mobile-clients-id :list_id list-id})))

(defn get-lists-by-client [db mobile-clients-id]
  (j/query db ["select l.* from shared_list_to_user sltu
               join lists l on sltu.list_id = l.id
               where sltu.mobile_clients_id = ?" mobile-clients-id]))

