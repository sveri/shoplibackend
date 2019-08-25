(ns de.sveri.shopli.db.share_hash
  (:require [clojure.java.jdbc :as j]))

(defn create-hash [db mobile-clients-id list-id hash]
  (first (j/insert! db :share_hashes {:mobile_clients_id mobile-clients-id :list_id list-id :hash hash})))

(defn get-hash [db hash]
  (first (j/query db ["select * from share_hashes where hash = ?" (str hash)])))

