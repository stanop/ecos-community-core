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
	Citeck.widget = Citeck.widget || {};

	var Dom = YAHOO.util.Dom;

	/**
	 * Dynamic loading component.
	 */
	Citeck.widget.DynamicComponent = function(htmlid, url) {
	
		Alfresco.util.Ajax.request({
			url: url,
			dataObj: {
				htmlid: htmlid
			},
			successCallback: {
				scope: this,
				fn: function(response) {
					var div = Dom.get(htmlid);
					div.innerHTML = response.serverResponse.responseText;
				},
			},
			failureMessage: "Could not load component",
			execScripts: true
		});
		
	},
	
	Citeck.widget.Dialog = function(htmlid, params) {
		Citeck.widget.Dialog.superclass.constructor.call(this, "Citeck.widget.Dialog", htmlid)
	
		this.widgets.panel = new YAHOO.widget.Dialog(htmlid, YAHOO.lang.merge({
			modal: true,
			constraintoviewport: true,
			draggable: true,
			fixedcenter: YAHOO.env.ua.mobile === null ? "contained" : false,
			close: true,
			visible: false,
			postmethod: "none"
		}, params || {}));
		this.widgets.panel.render(document.body);
	};

	YAHOO.extend(Citeck.widget.Dialog, Alfresco.component.Base);
	YAHOO.lang.augmentObject(Citeck.widget.Dialog.prototype, YAHOO.util.EventProvider.prototype);
	YAHOO.lang.augmentObject(Citeck.widget.Dialog.prototype, {
		show: function() {
			with(this.widgets) {
				panel.center();
				panel.show.apply(panel, arguments);
			}
		},
		
		hide: function() {
			with(this.widgets) {
				panel.hide.apply(panel, arguments);
			}
		},
	});
	
	Citeck.widget.AbstractFinderDialog = function(htmlid, finderId, finderUrl, params) {
		Citeck.widget.AbstractFinderDialog.superclass.constructor.call(this, htmlid, params);
		this.widgets.finder = new Citeck.widget.DynamicComponent(finderId, finderUrl);
	};
	
	YAHOO.extend(Citeck.widget.AbstractFinderDialog, Citeck.widget.Dialog, {
	
		finder: function() {
			return Alfresco.util.ComponentManager.get(this.id + "-finder");
		},
		
		show: function() {
			var finder = this.finder();
			if(!finder) return;
			finder.clearResults();
			Citeck.widget.AbstractFinderDialog.superclass.show.apply(this, arguments);
		},
		
	});
	
	Citeck.widget.SelectUserDialog = function(htmlid, params) {
		Citeck.widget.SelectUserDialog.superclass.constructor.call(this, htmlid, htmlid + "-finder",
			Alfresco.constants.URL_SERVICECONTEXT + "components/people-finder/people-finder", params);
		this.createEvent("itemSelected");
		YAHOO.Bubbling.on("personSelected", function(layer, args) {
			this.fireEvent("itemSelected", args[1]);
			this.hide();
		}, this);
	};
	YAHOO.extend(Citeck.widget.SelectUserDialog, Citeck.widget.AbstractFinderDialog);
	
	Citeck.widget.SelectGroupDialog = function(htmlid, params) {
		Citeck.widget.SelectGroupDialog.superclass.constructor.call(this, htmlid, htmlid + "-finder",
			Alfresco.constants.URL_SERVICECONTEXT + "components/people-finder/group-finder", params);
		this.createEvent("itemSelected");
		YAHOO.Bubbling.on("itemSelected", function(layer, args) {
			this.fireEvent("itemSelected", args[1]);
			this.hide();
		}, this);
	};
	YAHOO.extend(Citeck.widget.SelectGroupDialog, Citeck.widget.AbstractFinderDialog);
	
	Citeck.widget.DeleteItemDialog = function(htmlid, params) {
		Citeck.widget.DeleteItemDialog.superclass.constructor.call(this, htmlid, params);
		
		// create deleteConfirmed event:
		this.createEvent("deleteConfirmed");
		
		// Initialize delete button:
		this.widgets.deleteItemDeleteButton = new YAHOO.widget.Button(htmlid + "-delete-button", {
			onclick: {
				scope: this,
				fn: function() {
					this.fireEvent("deleteConfirmed");
					this.hide();
				},
			}
		});

		// Initialize cancel button:
		this.widgets.deleteItemCancelButton = new YAHOO.widget.Button(htmlid + "-cancel-button", {
			onclick: {
				scope: this,
				fn: this.hide,
			}
		});
		
	};
	YAHOO.extend(Citeck.widget.DeleteItemDialog, Citeck.widget.Dialog);
	
})();