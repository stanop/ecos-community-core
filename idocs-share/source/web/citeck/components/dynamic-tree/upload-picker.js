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

	var thisClass = Citeck.widget.UploadPickerControl = function(htmlid, fieldId, pageHtmlId) {
		thisClass.superclass.constructor.call(this, htmlid, fieldId, pageHtmlId);
		return this;
	};

	YAHOO.extend(Citeck.widget.UploadPickerControl, Citeck.widget.DynamicTreePickerControl, {
	
		options: YAHOO.lang.merge(Citeck.widget.DynamicTreePickerControl.prototype.options, {
			// custom upload button label:
			uploadButtonLabel: null,
			destinationNodeRef: null,
			assocType: null,
			contentType: null,
			propertyName: null,
			propertyValue: null,
			showSelectButton: true,
		}),

		_initButtons: function() {
			// initialize upload button
			var itemGroupActionsContainerId = this.id + "-itemGroupActions";
			if(this.options.allowSelectAction) {
				// create upload button
				this.widgets.uploadButton = new YAHOO.widget.Button({
					container: itemGroupActionsContainerId,
					label: this.options.uploadButtonLabel || this.msg("button.upload"),
					type: "button",
					disabled: true,
					onclick: {
						scope: this,
						fn: this.onFileUpload
					}
				});
				// get upload destination - destinationNodeRef or user home folder
				if (this.options.destinationNodeRef) {
					this.uploadDestination = this.options.destinationNodeRef;
					this.widgets.uploadButton.set("disabled", false);
				}
				else {
					Alfresco.util.Ajax.jsonGet({
						url: (Alfresco.constants.PROXY_URI + "api/people/" + Alfresco.constants.USERNAME + "/homefolder"),
						successCallback: {
							scope: this,
							fn: function(response) {
								this.uploadDestination = response.json.nodeRef;
								this.widgets.uploadButton.set("disabled", false);
							}
						}
					});
				}
			}
			if (this.options.showSelectButton)
				Citeck.widget.DynamicTreePickerControl.prototype._initButtons.call(this);
		},

		onFileUpload: function() {
			if(this.uploader == null) {
				this.uploader = Alfresco.getFileUploadInstance();
			}
			var conf = {
				destination: this.uploadDestination,
				mode: this.uploader.MODE_MULTI_UPLOAD,
				suppressRefreshEvent: true,
				onFileUploadComplete: {
					scope: this,
					fn: this.onFileUploadComplete
				}
			};
			if (this.options.assocType) {
				var uploadUrl = "api/citeck/upload?assoctype=" + encodeURIComponent(this.options.assocType);
				if (this.options.contentType)
					uploadUrl += '&contenttype=' + encodeURIComponent(this.options.contentType);
				if (this.options.propertyName && this.options.propertyValue)
					uploadUrl += '&propertyname=' + encodeURIComponent(this.options.propertyName) + '&propertyvalue=' + encodeURIComponent(this.options.propertyValue);
				if (Alfresco.util.CSRFPolicy && Alfresco.util.CSRFPolicy.getParameter && Alfresco.util.CSRFPolicy.getToken)
					uploadUrl += '&' + Alfresco.util.CSRFPolicy.getParameter() + '=' + encodeURIComponent(Alfresco.util.CSRFPolicy.getToken());
				uploadUrl += '&addedParam=param';
				conf.htmlUploadURL = uploadUrl;
				conf.flashUploadURL = uploadUrl;
			}
			this.uploader.show(conf);
		},

		onFileUploadComplete: function(response) {
			var files = response.successful;
			for(var i in files) {
                if(!files.hasOwnProperty(i)) continue;
				this.model.addItem({ 
					nodeRef: files[i].nodeRef
				}, "selected-items");
			}
		},

	});

})();
