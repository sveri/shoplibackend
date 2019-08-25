(ns de.sveri.shopli.db.list-entry
  (:require [clojure.java.jdbc :as j]))

(defn uuid-str->pg-uuid  [uuid]
  (doto (org.postgresql.util.PGobject.)
    (.setType "uuid")
    (.setValue uuid)))

(defn create-list-entry [db id name list-id]
  (first (j/insert! db :list_entry {:id (uuid-str->pg-uuid id) :name name :list_id (uuid-str->pg-uuid list-id)})))

(defn update-list-entry [db id name done]
  (first (j/update! db :list_entry {:name name :done done} ["id = ?" id])))

(defn get-list-entries [db list-id]
  (j/query db ["select * from list_entry where list_id = ? order by created_at asc" list-id]))

(defn get-list-by-id [db id]
  (first (j/query db ["select * from list_entry where id = ?" id])))
;
