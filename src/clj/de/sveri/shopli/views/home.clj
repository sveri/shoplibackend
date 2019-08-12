(ns de.sveri.shopli.views.home
  (:require [de.sveri.shopli.views.base :as v]))

(defn home-page [{:keys [localize] :as req}]
  (v/render
    "Home" req
    [:div
     [:div {:class "jumbotron"}
      [:h1 "Foosdsf!"]
      [:p "Bar"]
      [:p
       [:a {:href "/user/signup", :class "btn btn-primary btn-lg", :role "button"} "Sign up »"]]]]))

(defn contact-page [{:keys [localize] :as req}]
  (v/render
    (localize [:contact/contact]) req
    [:div
     [:div {:class "container"}
      [:h2 (localize [:contact/contact])]
      [:span (localize [:contact/content_belongs_to])]
      [:br]
      [:span (localize [:contact/contact])]]]))

(defn tos-page [req]
  (v/render
    "" req
    [:div
     [:div {:class "container"}
      [:div {:class "row"}
       [:div {:class "span6"}
        [:h1 "TOS"]]]]]))

(defn cookies-page [req]
  (v/render
    "" req
    [:div
     [:div {:class "container"}
      [:div {:class "page-header"} "\t"]
      [:h2 "Cookies"]
      [:p "..."]]]))

(defn reagent-example [req]
  (v/render
    "" (merge req {:plain-js "<script src=\"/js/example/example.js\"></script><script type=\"text/javascript\">de.sveri.shopli.core.init();</script>"})
    [:div
     [:div#app]]))


