(ns schema-workshop.main
  "Nothing to change here."
  (:require [compojure.core :refer [defroutes GET POST]]
            [ring.middleware.json :as middleware]
            [schema-workshop.core :as core]))

(defroutes routes
  (GET "/record" req core/get-record)
  (POST "/record" req core/update-record))

(def handler
  (-> routes
      (middleware/wrap-json-body {:keywords? true})
      middleware/wrap-json-response))

