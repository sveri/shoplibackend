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

(deftest authentication
  (let [resp (get-api-token-response)]
    (is (= 200 (:status resp)))))

(deftest add-list-forbidden
  (let [resp (cl/post (str test-base-url "mobile/add-list")
                      {:body             (j/write-str {:name "sdlkfj"})
                       :content-type     :json
                       :throw-exceptions false})]
    (is (= 403 (:status resp)))))

(defn add-list [list-name]
  (post-to-url-with-body "mobile/add-list" {:name list-name}))

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
