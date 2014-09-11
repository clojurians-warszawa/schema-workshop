(ns schema-workshop.core
  "Startup: start server with lein ring server-headless.
  End state for application should be ability to seamlessly insert and retrieve JSON data using POST and GET /record respectively.
  Functions tagged with :user-sexp are to be modfied during workshop.
  Timestamp example might seem a littlbe bit complicated but it is example of real-world usage.
  Main documentation is here: 
  Things TODO:
  1. First we want to set new value for db-record atom. db-record has defined validator which uses schema DbRecord to check for conistency.
     We must make sure that value is correct. HINT: DbRecord might be used in little bit better way when using (s/explain DbRecord).
  2. Next move is "
  (:require [compojure.core :refer [defroutes GET POST]]
            [ring.middleware.json :as middleware]
            [schema.core :as s :refer [defschema]]
            [schema.coerce :as coerce]
            [schema.macros :as macros]))

;;; sql timestamp conversion handling
(defn sql-timestamp? [x]
  (isa? (class x) java.sql.Timestamp))

(def Timestamp
  "Any timestamp"
  (s/pred sql-timestamp? 'sql-timestamp?))

(defn convert-long-to-timestamp-object [x]
  (if (integer? x)
    (java.sql.Timestamp. x)
    x))

(defn input-coercion-matcher [schema]
  ({Timestamp convert-long-to-timestamp-object} schema))

(defschema DbRecord
  {(s/optional-key :timestamp) Timestamp
   (s/optional-key :number) s/Int
   (s/optional-key :enum) (s/both s/Str (s/enum "red" "green"))
   :content s/Str})

(def db-record (atom {:content "Pierwotna treść"}
                      ; caveat: if we enclose whole struct in (s/maybe) then validation return nil, which works exactly as it should - except that it
                      ; make validation fail
                       :validator (partial s/validate DbRecord)))

;;; use  to 
(defn ^:user-sexp set-db-record! []
  (reset! db-record {:timestamp (java.sql.Timestamp. (* 1410387702 1000))
                     :number "10"
                     :content "To jest przykładowa treść"
                     :enum "blue"}))

;;; TODO: leave creating
(defschema ^:user-sexp DbOutputRecord
  {})

(def output-integer-cast
  (coerce/safe
   (fn [x]
     x)))

(defn ^:user-sexp get-record [req]
  {:body @db-record})


(defn update-record [{:keys [body] :as req}]
  (let [coerced-body ((coerce/coercer DbRecord (fn [schema]
                                                 (or (coerce/json-coercion-matcher schema)
                                                     (input-coercion-matcher schema)))) body)]
    (reset! db-record coerced-body))
  "ok")

(defroutes routes
  (GET "/record" req get-record)
  (POST "/record" req update-record))

(def handler
  (-> routes
      (middleware/wrap-json-body {:keywords? true})
      middleware/wrap-json-response))
