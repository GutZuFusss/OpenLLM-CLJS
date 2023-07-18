(ns openllm.components.chat.views
  (:require [re-frame.core :as rf]
            [openllm.components.chat.events :as events]
            [openllm.components.chat.subs :as subs]
            [openllm.components.side-bar.subs :as side-bar-subs]
            [openllm.subs :as root-subs]
            [openllm.components.chat.views :as views]
            [openllm.api.components :as api-components]
            [reagent-mui.material.tooltip :refer [tooltip]]
            [reagent-mui.material.icon-button :refer [icon-button]]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.material.modal :refer [modal]]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.paper :refer [paper]]
            [reagent-mui.material.typography :refer [typography]]
            [reagent-mui.icons.design-services :as ds-icon]
            [reagent-mui.icons.send :as send-icon]
            [reagent.core :as r]))

(defn chat-input-field
  "The chat input field."
  [on-submit]
  (let [chat-input-sub (rf/subscribe [::subs/chat-input-value])
        on-change #(rf/dispatch [::events/set-chat-input-value (.. % -target -value)])]
    (fn []
      [:textarea {:class "py-1 h-20 w-[calc(100%_-_80px)] block"
                  :style {:resize "none"}
                  :placeholder "Type your message..."
                  :value @chat-input-sub
                  :on-change on-change
                  :on-key-press (fn [e]
                                  (when (and (= (.-charCode e) 13) (not (.-shiftKey e)))
                                    (on-submit)))
                  :id "chat-input"}])))

(defn chat-controls
  "Aggregates the chat input field and the send button as well as the
   prompt layout button."
  []
  (let [llm-config (rf/subscribe [::root-subs/model-config])
        submit-prompt (rf/subscribe [::subs/prompt])
        on-submit-event [::events/on-send-button-click @submit-prompt @llm-config]]
    (fn chat-controls []
      [:form {:class "flex justify-end"}
       [chat-input-field #(rf/dispatch on-submit-event)]
       [:div {:class "grid grid-rows-2 ml-1.5"}
        [:div {:class "items-start"}
         [tooltip {:title "Edit prompt layout"}
          [icon-button {:on-click #(rf/dispatch [::events/toggle-modal])
                        :color "primary"}
           [ds-icon/design-services]]]]
        [button {:on-click #(rf/dispatch on-submit-event)
                 :variant "outlined"
                 :end-icon (r/as-element [send-icon/send])
                 :color "primary"} "Send"]]])))

(defn user->extra-bubble-style
  "Produces additional style attributes for a chatbubble contingent upon
   the provided user."
  [user]
  (if (= user :model)
     "bg-gray-50 mr-10 rounded-bl-none border-gray-200"
     "bg-gray-300 ml-10 rounded-br-none border-gray-400"))

(defn chat-history
  "The chat history."
  []
  (let [history (rf/subscribe [::subs/chat-history])]
    (fn chat-history []
      (into [:div {:class "px-8 flex flex-col items-center"}]
            (map (fn [{:keys [user text]}]
                   (let [display-user (if (= user :model) "System" "You")
                         alignment (if (= user :model) "flex-row" "flex-row-reverse")]
                     [:div {:class (str "flex " alignment " items-end my-2 w-full")}
                      [:h3 {:class "font-bold text-lg mx-2"} display-user]
                      [:div {:class (str "p-2 rounded-xl border " (user->extra-bubble-style user))}
                       [:p {:class (if (= user :model) "text-gray-700" "text-gray-950")} text]]]))
                 @history)))))

(defn prompt-layout-modal
  "The modal for editing the prompt layout."
  []
  (let [modal-open? (rf/subscribe [::subs/modal-open?])
        prompt-layout-value (rf/subscribe [::subs/prompt-layout])
        on-change #(rf/dispatch [::events/set-prompt-layout (.. % -target -value)])]
    (fn []
      [modal {:open @modal-open?
              :on-close #(rf/dispatch [::events/toggle-modal])}
       [box {:style {:position "absolute"
                     :width 800,
                     :top "50%"
                     :left "50%"
                     :transform "translate(-50%, -50%)"}}
        [paper {:elevation 24
                :style {:padding "20px 30px"}}
         [typography {:variant "h5"} "Prompt Layout"]
         [:textarea {:class "pt-3 mt-1 w-full h-64 block border bg-gray-200"
                     :value @prompt-layout-value
                     :on-change on-change}]
         [:div {:class "grid grid-cols-2"}
          [:div {:class ""}
           [api-components/file-upload
            {:callback-event ::events/set-prompt-layout
             :class "w-7/12 mt-3 py-2 px-4 rounded cursor-pointer bg-gray-600 text-white hover:bg-gray-700 file:bg-gray-900 file:hidden"}]]
          [:div {:class "mt-4 flex justify-end space-x-2"}
           [button {:type "button"
                    :variant "outlined"
                    :on-click #(rf/dispatch [::events/toggle-modal])} "Save"]]]]]])))

(defn chat-tab-contents
  "The component rendered if the chat tab is active."
  []
  (let [side-bar-open? (rf/subscribe [::side-bar-subs/side-bar-open?])]
    (fn []
      [:<>
       [prompt-layout-modal]
       [:div {:id "chat-history-container"
              :class "overflow-y-scroll mt-6 h-[calc(100%_-_170px)] w-full no-scrollbar"
              :style {:scrollBehavior "smooth"}}
        [:div
         [chat-history]]]
       [:div {:class (str "bottom-1 fixed w-[calc(100%_-_200px)] mb-2"
                          (if @side-bar-open?
                            " w-[calc(100%_-_350px)]"
                            " w-[calc(100%_-_30px)]"))}
        [chat-controls]]])))
