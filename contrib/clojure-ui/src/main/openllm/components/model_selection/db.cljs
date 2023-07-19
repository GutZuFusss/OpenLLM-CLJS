(ns openllm.components.model-selection.db
  "The branch of the `app-db` that saves data related to the model-selection view.
   This includes the current model selection, as well as the data for all available
   models.
   The path to this branch can be expressed as:
   *root -> components -> model-selection*"
  (:require [re-frame.core :as rf]
            [clojure.spec.alpha :as s]))

(defn key-seq
  [& more-keys]
  (into [:components-db :model-selection-db] more-keys))

(def loading-text "Loading...")

(declare get-all-models)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;                Spec                ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(s/def ::vec-of-runtimes? (s/coll-of
                           (s/and string?
                                  #(or (= % "pt")
                                       (= % "flax")  ;; all available runtimes
                                       (= % "tf")))
                           :kind vector?))

(s/def ::model_id (s/coll-of string? :kind vector?))                   ;; model_id is a vector of all models for a given model_type
(s/def ::url string?)                                                  ;; url to the model's page
(s/def ::requires_gpu boolean?)                                        ;; whether the model requires a gpu
(s/def ::runtime_impl ::vec-of-runtimes?)                              ;; supported runtimes
(s/def ::installation string?)                                         ;; installation instructions (pip command)
(s/def ::model-spec (s/keys :req-un [::model_id ::url ::requires_gpu   ;; the spec for a single model (aggregates all the above)
                                     ::runtime_impl ::installation]))
(s/def ::all-models #(or loading-text                                  ;; -- this is the case when the file with the model data has not been loaded yet by the ::set-model-data effect
                         (s/map-of keyword? ::model-spec)))            ;; map of all models

(s/def ::selected-model (s/keys :req-un
                                [::model-type #(or (keyword? %)        ;; currently selected model-id and model-type
                                                   (= % loading-text)) ;; -- same as above
                                 ::model-id string?]))

(s/def ::model-selection-db (s/keys :req-un [::all-models
                                             ::selected-model]))       ;; the spec of the model-selection-db

(defn initial-db
  []
  (rf/dispatch [:slurp-model-data-json])
  {:all-models loading-text ;; will be overwritten by the event dispatched above
   :selected-model {:model-type :chatglm
                    :model-id "thudm/chatglm"}})


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;           Rich Comments            ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment
  ;; check if initial-db is valid
  (s/valid? ::model-selection-db (initial-db)))
