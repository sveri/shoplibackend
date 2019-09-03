(ns de.sveri.shopli.routes.mobile-test
  (:require [clojure.test :refer :all]
            [de.sveri.shopli.setup :refer [test-base-url setup-db server-setup]]
            [clj-http.client :as cl]
            [clojure.data.json :as j]
            [clojure.string :as str])
  (:import (java.util UUID)))

(use-fixtures :each setup-db)
(use-fixtures :once server-setup)

(defn parse-response-body [resp]
  (j/read-str (:body resp) :key-fn keyword))

(defn get-api-token-response [& [device-id]]
  (cl/post (str test-base-url "mobile/authenticate")
           {:body         (j/write-str {:device-id (or device-id "device-id")
                                        :app-id    "app-id"})
            :content-type :json}))

(defn get-api-token [& [device-icd]]
  (let [resp (get-api-token-response device-icd)
        body (j/read-str (:body resp) :key-fn keyword)]
    (-> body :api-token)))


(defn add-auth-token [m & [device-icd]]
  (assoc m :headers {"Authorization" (str "Token " (get-api-token device-icd))}))

(defn post-to-url-with-body [url body & [device-icd]]
  (cl/post (str test-base-url url)
           (add-auth-token {:body             (j/write-str body)
                            :content-type     :json
                            :throw-exceptions false}
                           device-icd)))

(defn put-to-url-with-body [url body]
  (cl/put (str test-base-url url)
          (add-auth-token {:body             (j/write-str body)
                           :content-type     :json
                           :throw-exceptions false})))

(defn get-with-url [url & [device-id]]
  (cl/get (str test-base-url url)
          (add-auth-token {:accept           :json
                           :throw-exceptions false}
                          device-id)))

(defn add-list [list-name & [device-icd]]
  (post-to-url-with-body "mobile/list" {:name list-name} device-icd))

(defn add-list-entry [list-id entry-name]
  (post-to-url-with-body (str "/mobile/list-entry") {:name entry-name :list-id list-id}))

(deftest authentication
  (let [resp (get-api-token-response)]
    (is (= 200 (:status resp)))))

(deftest add-list-forbidden
  (let [resp (cl/post (str test-base-url "mobile/list")
                      {:body             (j/write-str {:name "sdlkfj"})
                       :content-type     :json
                       :throw-exceptions false})]
    (is (= 403 (:status resp)))))

(deftest add-one-list
  (let [resp (add-list "list-name")
        body (parse-response-body resp)]
    (is (= 200 (:status resp)))
    (is (= "list-name" (-> body :list :name)))))

(deftest add-two-lists
  (let [resp (add-list "list-name")
        body (parse-response-body resp)
        resp1 (add-list "list-name2")
        body1 (parse-response-body resp1)]
    (is (= 200 (:status resp)))
    (is (= "list-name" (-> body :list :name)))
    (is (= 200 (:status resp1)))
    (is (= "list-name2" (-> body1 :list :name)))))

(deftest get-lists
  (let [_ (add-list "list-name")
        _ (add-list "list-name2")
        lists (:lists (parse-response-body (get-with-url "mobile/list")))]
    (is (= 2 (count lists)))
    (is (= "list-name" (-> lists first :name)))
    (is (= "list-name2" (-> lists second :name)))))

(deftest get-lists-should-only-show-lists-per-device
  (let [_ (add-list "list-name")
        _ (add-list "list-name2" "device-id-2")
        lists-for-device-1 (-> (get-with-url "mobile/list") parse-response-body :lists)
        lists-for-device-2 (-> (get-with-url "mobile/list" "device-id-2") parse-response-body :lists)]
    (is (= 1 (count lists-for-device-1)))
    (is (= 1 (count lists-for-device-2)))))

(defn update-list-entry [id entry-name done]
  (put-to-url-with-body (str "/mobile/list-entry/" id) {:name entry-name :done done}))

(deftest add-list-entry-test
  (let [list-id (-> (parse-response-body (add-list "list-name")) :list :id)
        list-entry (:list-entry (parse-response-body (add-list-entry list-id "entry-name")))]
    (is (not (nil? (:id list-entry))))
    (is (= "entry-name" (:name list-entry)))))

(deftest update-list-entry-test
  (let [list-id (-> (parse-response-body (add-list "list-name")) :list :id)
        list-entry (:list-entry (parse-response-body (add-list-entry list-id "entry-name")))
        list-entry (:list-entry (parse-response-body (update-list-entry (:id list-entry) "entry-name1" true)))]
    (is (not (nil? (:id list-entry))))
    (is (= "entry-name1" (:name list-entry)))
    (is (= true (:done list-entry)))))

(defn get-list-entries [list-id]
  (cl/get (str test-base-url "/mobile/list-entry/list/" list-id)
          (add-auth-token {:accept           :json
                           :throw-exceptions false})))

(deftest get-list-entries-test
  (let [list-id (-> (parse-response-body (add-list "list-name")) :list :id)
        _ (add-list-entry list-id "entry-name1")
        _ (add-list-entry list-id "entry-name2")
        list-entries (:list-entries (parse-response-body (get-list-entries list-id)))]
    (is (= 2 (count list-entries)))
    (is (= "entry-name1" (-> list-entries first :name)))
    (is (= "entry-name2" (-> list-entries second :name)))))


(deftest get-initial-data
  (let [list-id (-> (parse-response-body (add-list "list-name")) :list :id)
        list-id2 (-> (parse-response-body (add-list "list-name2")) :list :id)
        _ (add-list-entry list-id "entry-name1")
        _ (add-list-entry list-id "entry-name2")
        _ (add-list-entry list-id2 "entry-name21")
        list-with-entries (-> (parse-response-body (get-with-url "mobile/initial-data")) :lists)
        first-list (first list-with-entries)
        entries (:list-entries first-list)]
    (is (= "list-name" (:name first-list)))
    (is (= 2 (count entries)))
    (is (= "entry-name1" (-> entries first :name)))
    (is (= "entry-name2" (-> entries second :name)))))


(deftest get-initial-data-only-lists
  (let [_ (-> (parse-response-body (add-list "list-name")) :list :id)
        _ (-> (parse-response-body (add-list "list-name2")) :list :id)
        list-with-entries (-> (parse-response-body (get-with-url "mobile/initial-data")) :lists)]
    (is (= 2 (count list-with-entries)))
    (is (= 0 (count (:list-entries (first list-with-entries)))))))


(deftest share-list
  (let [shared-list-id (-> (parse-response-body (add-list "list-name")) :list :id)
        share-hash (-> (post-to-url-with-body "mobile/list/share" {:list-id shared-list-id})
                       parse-response-body
                       :hash)]
    (is (UUID/fromString share-hash))
    (is (not (str/blank? share-hash)))))


(deftest share-list-with-from-and-to
  (let [shared-list-id (-> (parse-response-body (add-list "list-name")) :list :id)
        share-hash (-> (post-to-url-with-body "mobile/list/share" {:list-id shared-list-id
                                                                   :from "from"
                                                                   :to "to"})
                       parse-response-body)]
    (is (UUID/fromString (:hash share-hash)))
    (is (= "from" (:from share-hash)))
    (is (= "to" (:to share-hash)))))


(deftest share-list-workflow
  (let [shared-list-id (-> (parse-response-body (add-list "list-name")) :list :id)
        share-hash (-> (post-to-url-with-body "mobile/list/share" {:list-id shared-list-id})
                       parse-response-body
                       :hash)
        _ (post-to-url-with-body "mobile/list/accept-share"
                                 {:share-hash share-hash}
                                 "device-id-2")
        lists-for-device-2 (-> (get-with-url "mobile/list" "device-id-2") parse-response-body :lists)
        initial-data-for-device-1 (-> (parse-response-body (get-with-url "mobile/initial-data" "device-id-1")) :lists)
        initial-data-for-device-2 (-> (parse-response-body (get-with-url "mobile/initial-data" "device-id-2")) :lists)]
    (is (= 1 (count lists-for-device-2)))
    (is (= shared-list-id (-> lists-for-device-2 first :id)))
    (is (= 1 (count initial-data-for-device-2)))
    (is (= shared-list-id (-> initial-data-for-device-2 first :id)))
    (is (not (-> initial-data-for-device-1 first :shared)))
    (is (-> initial-data-for-device-2 first :shared))))

(deftest share-list-workflow-with-from-and-to
  (let [shared-list-id (-> (parse-response-body (add-list "list-name")) :list :id)
        share-hash (-> (post-to-url-with-body "mobile/list/share" {:list-id shared-list-id
                                                                   :from "from"
                                                                   :to "to"})
                       parse-response-body
                       :hash)
        _ (post-to-url-with-body "mobile/list/accept-share"
                                 {:share-hash share-hash}
                                 "device-id-2")
        lists-for-device-2 (-> (get-with-url "mobile/list" "device-id-2") parse-response-body :lists)
        initial-data-for-device-2 (-> (parse-response-body (get-with-url "mobile/initial-data" "device-id-2")) :lists)]
    (is (= 1 (count lists-for-device-2)))
    (is (= shared-list-id (-> lists-for-device-2 first :id)))
    (is (= "from" (-> lists-for-device-2 first :from_string)))
    (is (= "to" (-> lists-for-device-2 first :to_string)))
    (is (= 1 (count initial-data-for-device-2)))
    (is (= shared-list-id (-> initial-data-for-device-2 first :id)))
    (is (= "from" (-> initial-data-for-device-2 first :from_string)))
    (is (= "to" (-> initial-data-for-device-2 first :to_string)))))



