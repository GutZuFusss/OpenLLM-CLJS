(ns openllm.components.playground.db
  "The branch of the `app-db` that saves data related to the playground view.
   This includes the input box, the response box and the response modal.
   The path to this branch can be expressed as:
   *root -> components -> playground*"
  (:require [cljs.spec.alpha :as s]))

(defn key-seq
  [& more-keys]
  (into [:components-db :playground-db] more-keys))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;                Spec                ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(s/def ::playground-input-value string?)   ;; string the user entered into the input box
(s/def ::playground-last-response string?) ;; the last response from the server
(s/def ::response-modal-open? boolean?)    ;; whether the response modal is open

(s/def ::playground-db (s/keys :req-un [::playground-input-value ;; the spec for the playground-db
                                        ::playground-last-response
                                        ::response-modal-open?]))

(defn initial-db
  []
  {:playground-input-value ""
   :playground-last-response ""
   :response-modal-open? false})


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;           Rich Comments            ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment
  ;; check if initial-db is valid
  (s/valid? ::playground-db (initial-db)))
