(ns schema-workshop.functions
  "Schema functions examples are demonstrated here."
  (:require [schema.core :as s :refer [defschema]]
            [schema.macros :as sm]))

;;; TODO: extend with optional-nip parameter
(defschema PersonalData
  {:zip-code s/Str
   :first-name s/Str
   :phone-number (s/both s/Str (s/pred (partial every? #(Character/isDigit ^Character %)) 'ever-character-is-digit?))})

;;; TODO: modify this function to make use of automatic validation of input data
(defn format-data [{:keys [zip-code first-name phone-number optional-nip] :as new-data}]
  (format "We recived new data:\nZip code: %s\nFirst name: %s\nPhone number: %s\nNIP: %s" zip-code first-name phone-number optional-nip))

(comment
  (= (format-data {:zip-code "93-472"
                   :first-name "Grzesiu"
                   :phone-number "426088822"})
     "We recived new data:\nZip code: 93-472\nFirst name: Grzesiu\nPhone number: 426088822\nNIP: null")
  (= (format-data {:first-name "Grzesiu"
                   :phone-number "426088822"})
     Exception)
  (= (format-data {:zip-code "93-472"
                   :first-name "Grzesiu"
                   :phone-number 426088822})
     Exception)
  (= (format-data {:zip-code "93-472"
                   :first-name "Grzesiu"
                   :phone-number 426088822})
     Exception)
  (= (format-data {:zip-code "93-472"
                   :first-name "Grzesiu"
                   :phone-number "426088822"
                   :optional-nip "7281479015"})
     "We recived new data:\nZip code: 93-472\nFirst name: Grzesiu\nPhone number: 426088822\nNIP: 7281479015"))
