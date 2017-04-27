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

	var Dom = YAHOO.util.Dom,
		Event = YAHOO.util.Event,
		Selector = YAHOO.util.Selector;

	/**
	 * Multi-Field Picker Control.
	 * It allows for storing item fields in multiple form fields.
	 */
	Citeck.widget.MultiFieldPickerControl = function(htmlid, fieldId, pageHtmlId) {
		Citeck.widget.MultiFieldPickerControl.superclass.constructor.call(this, htmlid, fieldId, pageHtmlId);
	};
	
	YAHOO.extend(Citeck.widget.MultiFieldPickerControl, Citeck.widget.DynamicTreePickerControl, 
	{

		// selectItems is overrided to support retrieving items from multiple form fields,
		// so it CAN NOT be used separately from fields
		selectItems: function(items) {
			var fieldIds = [];
			var fieldMap = this.options.forms.fieldMapping;
			for(var j = 0; j < fieldMap.length; j++) {
				fieldIds.push(this.pageHtmlId + "_" + fieldMap[j].formField);
			}

			this._onAllAvailable(fieldIds, function() {
				var fields = {};
				fields[this.field.name] = items;
				for(var i = 0; i < items.length; i++) {
					var item = {};
					// process field mapping
					for(var j = 0; j < fieldMap.length; j++) {
						var formField = fieldMap[j].formField,
							itemField = fieldMap[j].itemField;
						// if field was not yet read, read it
						if(!fields.hasOwnProperty(formField)) {
							var field = document.getElementById(this.pageHtmlId + "_" + formField);
							if(field) {
								fields[formField] = this._deserialize(field.value);
							}
						}
						item[itemField] = fields[formField][i];
					}
					this.model.addItem(item, "selected-items", false);
				}
			}, this);

		},		

		// helper function:
		// executes fn when all of ids are available
		_onAllAvailable: function(ids, fn, context) {
		
			var intId = setInterval(function() {
				for(var i = 0; i < ids.length; i++) {
					if(Dom.get(ids[i]) == null) {
						return;
					}
				}
				fn.call(context);
				clearInterval(intId);
			}, 30);
			
		},
		
		// override _updateFields to update multiple fields
		_updateFields: function() {
			var selectedItems = this.model.getItem("selected-items")._item_children_;
		
			// calculate current, added, removed
			var current = {},
				added = [], 
				removed = [].concat(this.originalItems);
			
			var fieldMap = this.options.forms.fieldMapping;
			for(var j = 0; j < fieldMap.length; j++) {
				var itemField = fieldMap[j].itemField;
				if(!current.hasOwnProperty(itemField)) {
					current[itemField] = [];
				}
			}
			
			for(var i in selectedItems) {
				if(!selectedItems.hasOwnProperty(i)) continue;
				// current
				for(var j = 0; j < fieldMap.length; j++) {
					var formField = fieldMap[j].formField,
						itemField = fieldMap[j].itemField,
						value = selectedItems[i][itemField];
					current[itemField].push(value);
				}
				// added, removed
				var id = selectedItems[i][fieldMap[0].itemField];
				var index = removed.indexOf(id);
				if(index == -1) {
					added.push(id);
				} else {
					removed.splice(index, 1);
				}
			}

			var form = this.field.form,
				delim = this.options.fieldValueDelim;
			for(var j = 0; j < fieldMap.length; j++) {
				var formField = fieldMap[j].formField,
					itemField = fieldMap[j].itemField,
					field = document.getElementById(this.pageHtmlId + "_" + formField);
				field.value = current[itemField].join(delim);
			}
			this.fieldAdded.value = added.join(delim);
			this.fieldRemoved.value = removed.join(delim);
			
			// notify mandatory constraint checker, if necessary
			if (this.options.mandatory) {
				YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
			}

			// tell the world about update:
			YAHOO.Bubbling.fire("renderCurrentValue", {
				eventGroup: this
			});
		},
		
	});

})();