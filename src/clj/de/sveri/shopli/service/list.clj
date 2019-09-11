(ns de.sveri.shopli.service.list
  (:require [de.sveri.shopli.db.lists :as db-l]
            [de.sveri.shopli.db.mobile-clients-list :as db-mcl]))

(defn create-list [db id name mobile-clients-id]
  (let [list (db-l/create-list db id name mobile-clients-id)]
    (db-mcl/create-entry db mobile-clients-id (:id list))
    list))

(defn accept-share-list [db request-mobile-clients-id {:keys [list_id shared_by shared_with mobile_clients_id]}]
  (db-mcl/create-accept-entry db request-mobile-clients-id list_id shared_by)
  (db-mcl/update-entry db mobile_clients_id list_id shared_with))

(defn is-shared? [db list-id]
  (< 1 (-> (db-mcl/get-list-occurrence-number db list-id)
           first
           :count)))
