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
Citeck.util = Citeck.util || {};

var thisClass = Citeck.util.CriteriaModel = function(name) {
	thisClass.superclass.constructor.call(this, name || "Citeck.util.CriteriaModel");
	
	// create events
	this.createEvent("childrenUpdated");
	this.createEvent("childDeleted");
};

YAHOO.extend(thisClass, Citeck.util.AbstractModel, {

	updateChildren: function(item, criteria) {
		
		item = this.getItem(item);
		if(!item) return false;
		
		var config = this.getItemProperty(item, this.config.children);
		if(!config) return false;
		
		var format = this.config.formats[config.format];
		if(!format) return false;
		
		var url = config.get;
		if(!url) return false;
		
		var method = config.getMethod || "POST";
		
		Alfresco.util.Ajax.request({
			method: method,
			url: url,
			dataObj: criteria,
			requestContentType: Alfresco.util.Ajax.JSON,
			responseContentType: Alfresco.util.Ajax.JSON,
			successCallback: {
				scope: this,
				fn: function(response) {
					// get results
					var items = this._getResults(response, config.resultsList);
					// get metadata
					var metadata = this._getResults(response, config.metadataPath);
					
					// fill model
					this._fillModel(items, format);
					item._item_children_ = items;
					item.criteria = criteria;
					
					// fire event
					this.fireEvent("childrenUpdated", { 
						items: items, 
						from: item,
						metadata: metadata
					});
				}
			},
			failureCallback: {
				scope: this,
				fn: this.onFailure
			}
		});
	},
	
	deleteItem: function(child, parent) {
		child = this.getItem(child);
		parent = this.getItem(parent);
		if(!child || !parent) return false;
		
		var config = this.getItemProperty(parent, this.config.children);
		if(!config) return false;
		
		var process = function() {
			// search for item in children
			var index = this._findChild(child, parent);
			if(index == -1) return;
			parent._item_children_.splice(index, 1);
			// fire "deleted" event
			this.fireEvent("childDeleted", { item: child, from: parent });
		}
		
		// first set item properties, then parent properties:
		var url = config["delete"];
		url = this.renderTemplate(child, url.replace(/\{item\./g, '{'));
		url = this.renderTemplate(parent, url.replace(/\{parent\./g, '{'));
		url = encodeURI(url);

		// send request:
		Alfresco.util.Ajax.request({
			method: Alfresco.util.Ajax.DELETE,
			url: url,
			successCallback: {
				scope: this,
				fn: process,
			},
			failureCallback: {
				scope: this,
				fn: this.onFailure
			},
		});
	},

});

})();