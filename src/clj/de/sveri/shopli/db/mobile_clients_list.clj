(ns de.sveri.shopli.db.mobile-clients-list
  (:require [clojure.java.jdbc :as j]))

(defn create-entry
  ([db mobile-clients-id list-id] (create-entry db mobile-clients-id list-id "" ""))
  ([db mobile-clients-id list-id shared-by shared-with]
   (first (j/insert! db :mobile_clients_list {:mobile_clients_id mobile-clients-id :list_id list-id
                                              :shared_by         shared-by :shared_with shared-with}))))

(defn create-accept-entry [db mobile-clients-id list-id shared-by]
  (create-entry db mobile-clients-id list-id shared-by ""))

(defn update-entry [db mobile-clients-id list-id shared-with]
  (j/update! db :mobile_clients_list {:shared_with shared-with}
             ["mobile_clients_id = ? and list_id = ?" mobile-clients-id list-id]))

(defn get-list-occurrence-number [db list-id]
  (j/query db ["select count(*) from mobile_clients_list where list_id = ? " list-id]))

;(defn create-entry [db mobile-clients-id {:keys [list_id from_string to_string]}]
;  (first (j/insert! db :shared_list_to_user {:mobile_clients_id mobile-clients-id :list_id list_id
;                                             :from_string from_string :to_string to_string})))

;(defn get-lists-by-client [db mobile-clients-id]
;  (j/query db ["select l.*, sltu.from_string, sltu.to_string from shared_list_to_user sltu
;               join lists l on sltu.list_id = l.id
;               where sltu.mobile_clients_id = ?" mobile-clients-id]))

