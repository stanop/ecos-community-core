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

if (typeof Citeck == "undefined" || !Citeck) {
    var Citeck = {};
}

Citeck.forms = Citeck.forms || {};

(function() {

    var Dom = YAHOO.util.Dom;

	Citeck.forms.AutoNumberControl = function(htmlid) {
		Citeck.forms.AutoNumberControl.superclass.constructor.call(this, "Citeck.forms.AutoNumberControl", htmlid, ["button"]);
	};

	YAHOO.extend(Citeck.forms.AutoNumberControl, Alfresco.component.Base, {
	
		options: {
		
			/**
			 * Number template name, that will be used to generate number.
			 */
			template: null,
			
			/**
			 * Item type of this form,
			 */
			itemType: "type",
			
			/**
			 * Item id of this form.
			 */
			itemId: null,
		
			/**
			 * Option to disable "Generate" button, while generating number.
			 * @default true
			 */
			disableButtonOnClick: true,
			
			/**
			 * Option to disable input field, while generating number.
			 * @default true
			 */
			disableInputOnClick: true,
			
			/**
			 * Option to enable "Generate" button on successful number generation.
			 * @default true
			 */
			enableButtonOnSuccess: true,
			
			/**
			 * Option to enable input field on successful number generation.
			 * @default true
			 */
			enableInputOnSuccess: true,
			
			/**
			 * Option to enable "Generate" button on failed number generation.
			 * @default true
			 */
			enableButtonOnFailure: true,
			
			/**
			 * Option to enable input field on failed number generation.
			 * @default true
			 */
			enableInputOnFailure: true,
		},
	
		onReady: function() {
		
			this.widgets.input = Dom.get(this.id);
		
			var container = this.widgets.input.parentElement;
		
			this.widgets.btnGenerate = new YAHOO.widget.Button({
				container: container,
				type: "push",
				label: this.msg("button.generate"),
				onclick: {
					scope: this,
					fn: this.onGenerateBtn
				}
			});
		
		},
		
		onGenerateBtn: function(e) {
			var url = YAHOO.lang.substitute(Alfresco.constants.PROXY_URI + 
				"api/{itemType}/{itemId}/formprocessor", {
					itemType: this.options.itemType + "-number",
					itemId: this.options.itemId.replace(":/","")
				});
			var formUI = Alfresco.util.ComponentManager.get(this.widgets.input.form.id);
			var formsRuntime = formUI.formsRuntime;
			var formData = formsRuntime.getFormData();
			formData.template = this.options.template;
			Alfresco.util.Ajax.jsonPost({
				url: url,
				dataObj: formData,
				successCallback: {
					scope: this,
					fn: this.onGenerateSuccess
				},
				failureCallback: {
					scope: this,
					fn: this.onGenerateFailure
				}
			});
			if(this.options.disableButtonOnClick) {
				this.widgets.btnGenerate.set("disabled", true);
			}
			if(this.options.disableInputOnClick) {
				this.widgets.input.disabled = true;
			}
		},
		
		onGenerateSuccess: function(response) {
			this.widgets.input.value = response.json.persistedObject;
			if(this.options.enableButtonOnSuccess) {
				this.widgets.btnGenerate.set("disabled", false);
			}
			if(this.options.enableInputOnSuccess) {
				this.widgets.input.disabled = false;
			}
		},
		
		onGenerateFailure: function(response) {
			this.onFailure.call(this, response);
			if(this.options.enableButtonOnFailure) {
				this.widgets.btnGenerate.set("disabled", false);
			}
			if(this.options.enableInputOnFailure) {
				this.widgets.input.disabled = false;
			}
		},
	
	});

	// mix-in general configurable onFailure method
	YAHOO.lang.augmentObject(Citeck.forms.AutoNumberControl.prototype, Citeck.util.ErrorManager.prototype);

})();