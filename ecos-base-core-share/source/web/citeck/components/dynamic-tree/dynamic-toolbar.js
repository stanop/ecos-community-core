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
	 * Dynamic Toolbar widget
	 */

	Citeck.widget.DynamicToolbar = function(htmlid, model, name) {
		Citeck.widget.DynamicToolbar.superclass.constructor.call(this, name || "Citeck.widget.DynamicToolbar", htmlid);
	
		this.model = model;
		this.state = {};

		// catch registering buttons to know, what to render
		YAHOO.Bubbling.on("registerButtons", this.onRegisterButtons, this);
		
		// subscribe on child deleted, to clear toolbar, if its item is deleted
		model.subscribe("childDeleted", this.onChildDeleted, this, true);
	}
	
	YAHOO.extend(Citeck.widget.DynamicToolbar, Alfresco.component.Base);
	YAHOO.lang.augmentObject(Citeck.widget.DynamicToolbar.prototype, Citeck.widget.HasButtons.prototype);	
	YAHOO.lang.augmentObject(Citeck.widget.DynamicToolbar.prototype, {
	
		/**
		 * Set toolbar configuration.
		 * @param config {
		 *   buttons: [
		 *     "key1": [ "buttonId1", "buttonId2", ... ],
		 *     "key2": [ "buttonId1", "buttonId2", ... ],
		 *     ...
		 *   ],
		 * }
		 */
		setConfig: function(config) {
			this.config = config;
		},
		
		/**
		 * Set toolbar context.
		 * Context means data item, that is represented by toolbar and its parent.
		 * If toolbar knows, that the item is removed from the parent, 
		 *  it cleares itself.
		 * @param item - item represented by toolbar
		 * @param parent - parent of item represented by toolbar
		 */
		setContext: function(item, parent) {
			this.state.item = this.model.getItem(item);
			this.state.parent = this.model.getItem(parent);
			Dom.get(this.id + "-buttons").innerHTML = this._renderButtons(item, parent);
			// also set style of toolbar:
			var buttonStyles = Dom.get(this.id + "-button-styles");
			if(buttonStyles) {
				buttonStyles.className = this._getButtonIds(item).join(" ");
			}
		},
		
		/**
		 * childDeleted event handler.
		 * @param args.item - item that is deleted
		 * @param args.from - item, from which item is deleted
		 */
		onChildDeleted: function(args) {
			var parent = this.model.getItem(args.from);
			var item = this.model.getItem(args.item);
			
			// handle special case:
			// if selected item was deleted:
			if(this.state.item._item_name_ == item._item_name_
			&& this.state.parent._item_name_ == parent._item_name_)
			{
				// nothing is selected now
				this.setContext("none", "none");
			}
		},
	
	});
	
})();