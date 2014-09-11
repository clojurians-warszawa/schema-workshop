(ns schema-workshop.core
  "Startup: start server with lein ring server-headless.
  End state for application should be ability to seamlessly insert and retrieve JSON data using POST and GET /record respectively.
  Functions tagged with :user-sexp are to be modified during workshop.
  Timestamp example might seem a little bit complicated but it is (mostly) example of real-world usage.
  Main documentation is here: https://github.com/Prismatic/schema 
  Unfortunately on one side it is quite large, on the other - it does not describe some useful constructs (defschema for instance).
  Coercers are additionally explained here: http://blog.getprismatic.com/schema-0-2-0-back-with-clojurescript-data-coercion/ (section coercion)
  Things TODO:
  1. First we want to set new value for db-record atom. db-record has defined validator which uses schema DbRecord to check for consistency.
     We must make sure that value is correct. HINT: DbRecord might be a little bit more readable when using (s/explain DbRecord).
  2. Now we can try using enforcing schema to validate function inputs. Move to schema-workshop.functions and finish task located there.
  3. Next move is ... reading. No, seriously first read about coercers (in /doc directoy), then look at db-record-coercer and it parts.
     It shows very useful feature of coercers which is ability to combine few coercers into one.
  3. Application currently supports setting value of db-record by using POST /record. We want it to support getting exactly the same value by GET /record also.
     For this feature to work one needs to modify db-record-output-coercer accordingly."
  (:require [schema.core :as s :refer [defschema]]
            [schema.coerce :as coerce]
            [schema.macros :as macros]))

;;; sql timestamp conversion handling
(defn sql-timestamp? [x]
  (isa? (class x) java.sql.Timestamp))

(def Timestamp
  "Any timestamp"
  (s/pred sql-timestamp? 'sql-timestamp?))

(defschema DbRecord
  {(s/optional-key :timestamp) Timestamp
   (s/optional-key :number) s/Int
   (s/optional-key :enum) (s/both s/Str (s/enum "red" "green"))
   :content s/Str})

(def db-record (atom {:content "Pierwotna treść"}
                      ; caveat: if we enclose whole struct in (s/maybe) then validation return nil, which works exactly as it should - except that it
                      ; make validation fail
                       :validator (partial s/validate DbRecord)))

;;; use to 
(defn ^:user-sexp set-db-record! []
  (reset! db-record {:timestamp (java.sql.Timestamp. (* 1410387702 1000))
                     :number "10"
                     :content "To jest przykładowa treść"
                     :enum "blue"}))

;;; INPUT COERCER CREATION

(defn convert-long-to-timestamp-object [x]
  (if (integer? x)
    (java.sql.Timestamp. (* x 1000))
    x))

(defn input-coercion-matcher [schema]
  ({Timestamp convert-long-to-timestamp-object} schema))

;;; example of combining
(def db-record-input-coercer (coerce/coercer DbRecord (fn [schema]
                                                       (or (coerce/json-coercion-matcher schema)
                                                           (input-coercion-matcher schema)))))

(defn update-record [{:keys [body] :as req}]
  (let [coerced-body (db-record-input-coercer body)]
    (println coerced-body)
    (reset! db-record coerced-body))
  "ok")


(defschema ^:user-sexp DbOutputRecord
  {})

(def output-integer-cast
  (coerce/safe
   (fn [x]
     x)))

(def ^:user-sexp db-record-output-coercer identity)

(defn get-record [req]
  {:body (db-record-output-coercer @db-record)})
