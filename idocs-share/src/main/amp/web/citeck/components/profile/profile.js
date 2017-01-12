/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
(function() {

    Citeck = typeof Citeck != "undefined" ? Citeck : {};
    Citeck.component = Citeck.component || {};

    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event,
        Selector = YAHOO.util.selector;

    var $html = Alfresco.util.encodeHTML,
        $siteURL = Alfresco.util.siteURL;

    Citeck.component.UserProfile = function UserProfile_constructor(htmlId) {
    	Citeck.component.UserProfile.superclass.constructor.call(this, "Citeck.component.UserProfile", htmlId, []);

	    return this;
    };

    YAHOO.extend(Citeck.component.UserProfile, Alfresco.component.Base);

    YAHOO.lang.augmentObject(Citeck.component.UserProfile.prototype, {
	
        options: {
            itemKind: "",
            itemId: "",
            mode: "",
            formId: "",
            formUI: "",
            submitType: "",
            showCaption: "",
            showCancelButton: "",
            writeMode: false
        },

        widgets: {
            editButton: null
        },


        onReady: function UserProfile_onReady() {

            this.widgets.editButton = new YAHOO.widget.Button(this.id + "-profileEditButton", {
                type: "button",
                label: this.msg("button.editProfile"),
                onclick: {
                    scope: this,
                    fn: this.onEditButtonClick
                }
            });
            this.widgets.editButton.setStyle("visibility", "hidden");

            YAHOO.Bubbling.on("beforeFormRuntimeInit", this.onBeforeFormRuntimeInit, this);

            this.renderForm();

        },

        onFormContentReady: function UserProfile_onFormContentReady(layer, args) {

            var submitButton = args[1].buttons.submit;
            submitButton.set("label", this.msg("button.save"));

            // add a handler to the cancel button
            var cancelButton = args[1].buttons.cancel;
            cancelButton.addListener("click", this.onCancelButtonClick, null, this);

            YAHOO.Bubbling.unsubscribe("formContentReady", this.onFormContentReady, this);
        },

        onBeforeFormRuntimeInit: function CreateContentMgr_onBeforeFormRuntimeInit(layer, args) {
            args[1].runtime.setAJAXSubmit(true,
                {
                    successCallback:
                    {
                        fn: this.onCreateContentSuccess,
                        scope: this
                    },
                    failureCallback:
                    {
                        fn: this.onCreateContentFailure,
                        scope: this
                    }
                });
        },

        onCreateContentSuccess: function CreateContentMgr_onCreateContentSuccess(response)
        {
            this.options.mode = "view";
            this.renderForm();
        },

        onCreateContentFailure: function CreateContentMgr_onCreateContentFailure(response)
        {
            var errorMsg = this.msg("create-content-mgr.create.failed");
            if (response.json && response.json.message)
            {
                errorMsg = errorMsg + ": " + response.json.message;
            }

            Alfresco.util.PopupManager.displayPrompt(
                {
                    title: this.msg("message.failure"),
                    text: errorMsg
                });
        },

        onCancelButtonClick: function UserProfile_onCancelButtonClick(type, args) {
            this.options.mode = "view";
            this.renderForm();
        },

        renderForm: function UserProfile_renderForm() {
            var formUrl = YAHOO.lang.substitute(
                Alfresco.constants.URL_SERVICECONTEXT + "components/form?itemKind={itemKind}&itemId={itemId}&formId={formId}&mode={mode}&showCancelButton={showCancelButton}&submitType={submitType}", {
                    itemId: this.options.itemId,
                    itemKind: this.options.itemKind,
                    formId: this.options.formId,
                    mode: this.options.mode,
                    showCancelButton: this.options.showCancelButton,
                    submitType: this.options.submitType
                }
            );

            var formData = {
                htmlid: this.id + "-profileForm-" + Alfresco.util.generateDomId()
            };


            Alfresco.util.Ajax.request({
                url: formUrl,
                dataObj: formData,
                successCallback: {
                    fn: function (response) {
                        Dom.get(this.id + "-profile-form").innerHTML = response.serverResponse.responseText;
                        this.currentForm = Dom.get(this.id + "-profileForm-form");
                        if(this.options.mode == "view" && this.options.writeMode) {
                            this.widgets.editButton._setDisabled(false);
                            this.widgets.editButton.setStyle("visibility", "visible");
                        } else if (this.options.mode == "edit") {
                            this.widgets.editButton.setStyle("visibility", "hidden");
                        }
                    },
                    scope: this
                },
                failureMessage: "Could not load form component '" + formUrl + "'.",
                scope: this,
                execScripts: true
            });

        },

        onEditButtonClick: function UserProfile_onEditButtonClick() {
            this.widgets.editButton._setDisabled(true);
            YAHOO.Bubbling.on("formContentReady", this.onFormContentReady, this);
            this.options.mode = "edit";
            this.renderForm();
        }
    });
})();
