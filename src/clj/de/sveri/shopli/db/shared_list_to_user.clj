(ns de.sveri.shopli.db.shared-list-to-user
  (:require [clojure.java.jdbc :as j]))

(defn create-entry [db mobile-clients-id {:keys [list_id from_string to_string]}]
  (first (j/insert! db :shared_list_to_user {:mobile_clients_id mobile-clients-id :list_id list_id
                                             :from_string from_string :to_string to_string})))

(defn get-lists-by-client [db mobile-clients-id]
  (j/query db ["select l.*, sltu.from_string, sltu.to_string from shared_list_to_user sltu
               join lists l on sltu.list_id = l.id
               where sltu.mobile_clients_id = ?" mobile-clients-id]))

