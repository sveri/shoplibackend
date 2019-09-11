(ns de.sveri.shopli.db.list-entry
  (:require [clojure.java.jdbc :as j]
            [de.sveri.shopli.db.util :as u]))

(defn create-list-entry [db id name list-id]
  (first (j/insert! db :list_entry {:id (u/uuid-str->pg-uuid id) :name name :list_id (u/uuid-str->pg-uuid list-id)})))

(defn update-list-entry [db id name done]
  (first (j/update! db :list_entry {:name name :done done} ["id = ?" id])))

(defn get-list-entries [db list-id]
  (j/query db ["select * from list_entry where list_id = ? order by created_at asc" list-id]))

(defn get-list-by-id [db id]
  (first (j/query db ["select * from list_entry where id = ?" id])))
;
