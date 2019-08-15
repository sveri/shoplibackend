(ns de.sveri.shopli.db.list-entry
  (:require [clojure.java.jdbc :as j]))

;(defn get-all-users [db & [where-email-like]]
;  (j/query db ["select * from users where email like ? order by email asc" (str "%" (or where-email-like "") "%")]))
;
;(defn get-user-by-email [db email]
;  (first (j/query db ["select * from users where email = ? limit 1" email])))
;
;(defn get-user-by-id [db id]
;  (first (j/query db ["select * from users where id = ? limit 1" id])))
;
;(defn username-exists? [db email] (some? (get-user-by-email db email)))

(defn create-list-entry [db name list-id]
  (first (j/insert! db :list_entry {:name name :list_id list-id})))

(defn get-lists [db mobile-clients-id]
  (j/query db ["select * from lists where mobile_clients_id = ?" mobile-clients-id]))

;(defn update-user [db id fields]
;  (j/update! db :users fields ["id = ?" id]))
;
;(defn delete-user [db id] (j/delete! db :users ["id = ?" id]))
;
;(defn change-password [db email pw] (j/update! db :users {:pass pw} ["email = ?" email]))
;
