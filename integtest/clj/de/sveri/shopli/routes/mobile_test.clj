(ns de.sveri.shopli.routes.mobile-test
  (:require [clojure.test :refer :all]
            [de.sveri.shopli.setup :refer [test-base-url setup-db server-setup]]
            [clj-http.client :as cl]
            [clojure.data.json :as j]))

(use-fixtures :each setup-db)
(use-fixtures :once server-setup)

(defn parse-response-body [resp]
  (j/read-str (:body resp) :key-fn keyword))

(defn get-api-token-response []
  (cl/post (str test-base-url "mobile/authenticate")
           {:body         (j/write-str {:device-id "device-id"
                                        :app-id    "app-id"})
            :content-type :json}))

(defn get-api-token []
  (let [resp (get-api-token-response)
        body (j/read-str (:body resp) :key-fn keyword)]
    (-> body :api-token)))


(defn add-auth-token [m]
  (assoc m :headers {"Authorization" (str "Token " (get-api-token))}))

(defn post-to-url-with-body [url body]
  (cl/post (str test-base-url url)
           (add-auth-token {:body             (j/write-str body)
                            :content-type     :json
                            :throw-exceptions false})))

(defn get-with-url [url]
  (cl/get (str test-base-url url)
          (add-auth-token {:accept    :json
                           :throw-exceptions false})))

(deftest authentication
  (let [resp (get-api-token-response)]
    (is (= 200 (:status resp)))))

(deftest add-list-forbidden
  (let [resp (cl/post (str test-base-url "mobile/list")
                      {:body             (j/write-str {:name "sdlkfj"})
                       :content-type     :json
                       :throw-exceptions false})]
    (is (= 403 (:status resp)))))

(defn add-list [list-name]
  (post-to-url-with-body "mobile/list" {:name list-name}))

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

(defn add-list-entry [list-id entry-name]
  (post-to-url-with-body (str "/mobile/list-entry") {:name entry-name :list-id list-id}))

(deftest add-list-entry-test
  (let [list-id (-> (parse-response-body (add-list "list-name")) :list :id)
        list-entry (:list-entry (parse-response-body (add-list-entry list-id "entry-name")))]
    (is (not (nil? (:id list-entry))))
    (is (= "entry-name" (:name list-entry)))))

(defn get-list-entries [list-id]
  (cl/get (str test-base-url "/mobile/list-entry/list/" list-id)
          (add-auth-token {:accept    :json
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
        first-list (:1 list-with-entries)
        entries (:list-entries first-list)]
    (is (= "list-name" (:name first-list)))
    (is (= 2 (count entries)))
    (is (= "entry-name2" (-> entries first :name)))
    (is (= "entry-name1" (-> entries second :name)))))



