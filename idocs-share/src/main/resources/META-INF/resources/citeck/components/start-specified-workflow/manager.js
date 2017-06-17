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
	if(typeof Citeck == "undefined") Citeck = {};
	Citeck.component = Citeck.component || {};

	var Dom = YAHOO.util.Dom;
	
	Citeck.component.StartWorkflowManager = function(htmlid) {
		Citeck.component.StartWorkflowManager.superclass.constructor.call(this, htmlid);
		YAHOO.Bubbling.on("objectFinderReady", this.onObjectFinderReady, this);
	};
	
	YAHOO.extend(Citeck.component.StartWorkflowManager, Alfresco.component.ShareFormManager, {

		onObjectFinderReady: function(layer, args) {
			var finder = args[1].eventGroup;
			var key = finder.options.field.replace(/^assoc_/, '');
			// if the field was set for finder - set it
			if(this.options.args.hasOwnProperty(key)) {
				finder.selectItems(this.options.args[key]);
			}
		},
		
		onFormSubmitSuccess: function(response) {
			// get submitUrl from parsed response
			if(this.options.returnPage == "workflow-details") {
				this.options.submitUrl = Alfresco.util.siteURL(response.json.persistedObject.replace(/^WorkflowInstance\[id=([^,]*),.*$/, "workflow-details?workflowId=$1"));
			}
			Citeck.component.StartWorkflowManager.superclass.onFormSubmitSuccess.apply(this, arguments);
		},
	
	});

})();