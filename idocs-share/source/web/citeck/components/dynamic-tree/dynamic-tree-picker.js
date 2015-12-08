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
	var $buttonSubscribe = Citeck.widget.HasButtons.subscribe;
	var $buttonUnsubscribe = Citeck.widget.HasButtons.unsubscribe;

	/**
	 * Item Picker Dialog based on DynamicTree widgets.
	 * Event itemsSelected - returns selected items, when dialog ends.
	 */
	Citeck.widget.DynamicTreePicker = function(htmlid, name) {
	
		Citeck.widget.DynamicTreePicker.superclass.constructor.call(this, name || "Citeck.form.DynamicTreePicker", htmlid, ["button", "resize"]);

		// create model:
		this.model = new Citeck.util.HierarchyModel(this.name);

		this.createEvent("itemsSelected");
	
	};
	
	YAHOO.extend(Citeck.widget.DynamicTreePicker, Alfresco.component.Base, {
	
		// default values for options
		options: {
		
			// model configuration
			model: {},
			
			// tree (left-pane) configuration
			tree: {},
			
			// list (right-pane) configuration
			list: {},
			
			// true if multiple items can be selected
			multipleSelectMode: true,
			
			// label of search text input
			searchLabel: null,
			
			// roots configuration
			roots: null,
			
			// current root
			currentRoot: "root",
			
			// show immediately after onReady method
			autoShow: false
		},

		/**
		 * Called on destroy.
		 * Unsubscribe from button events.
		 */
		destroy: function() {
			$buttonUnsubscribe(this);
			if (this.model) {
				this.model.unsubscribeAll(this);
				delete this.model;
			}
			if (this.searchKeyListener) {
				this.searchKeyListener.disable();
				delete this.searchKeyListener;
			}
			if (this.widgets) {
				if (this.widgets.tree)
					this.widgets.tree.unsubscribeAll(this);
				if (this.widgets.list)
					this.widgets.list.unsubscribeAll(this);
			}
			Citeck.widget.DynamicTreePicker.superclass.destroy.call(this);
		},
	
	});
	YAHOO.lang.augmentObject(Citeck.widget.DynamicTreePicker.prototype, YAHOO.util.EventProvider.prototype);
	YAHOO.lang.augmentObject(Citeck.widget.DynamicTreePicker.prototype, {
	
		onReady: function() {

			// create widgets:
			this.widgets.tree = new Citeck.widget.DynamicTree(this.id + "-results", this.model, this.name);
			this.widgets.list = new Citeck.widget.DynamicTree(this.id + "-selectedItems", this.model, this.name);
			this.widgets.dialog = Alfresco.util.createYUIPanel(this.id,
			{
				width: "62em"
			});
			
			this.widgets.buttonOK = Alfresco.util.createYUIButton(this, "ok", this.onButtonOKClick, {}, this.id.replace(/picker$/,"ok"));
			this.widgets.buttonCancel = Alfresco.util.createYUIButton(this, "cancel", this.onButtonCancelClick, {}, this.id.replace(/picker$/,"cancel"));
			this.widgets.resizer = new YAHOO.util.Resize(this.id + "-left", {
				handles: ["r"],
			});
			this.widgets.buttonSearch = Alfresco.util.createYUIButton(this, "searchButton", this.onSearch);
			
			// subscribe on model updates:
			this.model.subscribe("childAdded", this.onChildAdded, this, true);
			this.model.subscribe("childDeleted", this.onChildDeleted, this, true);
			this.model.subscribe("childrenUpdated", this.setSelectStyle, this, true);
			
			// subscribe on tree events:
			this.widgets.list.subscribe("clickEvent", this.onListItemSelected, this, true);
			
			// subscribe on button actions:
			var ids = [
				this.widgets.tree.id,
				this.widgets.list.id,
			];
			$buttonSubscribe("itemSelect", this.onItemSelect, this, ids);
			$buttonSubscribe("itemUnselect", this.onItemUnselect, this, ids);
			
			// subscribe on search "Enter"
			this.searchKeyListener = new YAHOO.util.KeyListener(this.id + "-searchText", { keys: 13 }, {
				fn: this.onSearch,
				scope: this,
				correctScope: true,
			}, "keydown");
			this.searchKeyListener.enable();

			// set configuration:
			this.model.setConfig(this.options.model);
			this.widgets.tree.setConfig(this.options.tree);
			this.widgets.list.setConfig(this.options.list);

			// create root items
			var roots = this.options.roots;
			if(roots && roots.length > 0) {
				// create menu for selecting root:
				var rootSelector = 
				this.widgets.rootSelector = new YAHOO.widget.ButtonGroup({
					id: this.id + "-root-selector",
					container: YAHOO.util.Selector.query(".picker-header", this.id, true)
				});

				// iterate roots
				for(var i = 0; i < roots.length; i++) {
					this.model.createSpecialItem({}, {
						name: roots[i].name,
						keys: roots[i].keys,
						title: roots[i].title
					});
					rootSelector.addButton({
						label: roots[i].title,
						value: roots[i].name,
						checked: roots[i].name == this.options.currentRoot
					});
				}
				
				rootSelector.subscribe("valueChange", this.onNewRootSelected, this, true);
			} else {
				// only one root item by default
				this.model.createSpecialItem({}, {
					name: "root",
					keys: ["root"]
				});
			}
			
			this.model.createSpecialItem({}, {
				name: "selected-items",
				keys: ["selected-items"]
			});
			
			// if there is no search configuration
			// do not create search and hide search bar
			this.searchEnabled = this.options.model.children.search && this.options.model.children.search.get;
			if(this.searchEnabled) {
				this.model.createSpecialItem({}, {
					name: "search",
					keys: ["search"]
				});
				
				// set search label:
				if(this.options.searchLabel) {
					var label = document.createElement("label");
					label.innerHTML = this.options.searchLabel;
					label.setAttribute("for", this.id + "-searchText");
					var container = Dom.get(this.id + "-searchContainer");
					container.appendChild(label);
					container.insertBefore(label, Dom.get(this.id + "-searchText"));
				}
			} else {
				Dom.addClass(Selector.query(".picker-header .search", this.id), "hidden");
			}

			// set root items:
			this.widgets.tree.setContext(this.options.currentRoot, "none");
			this.widgets.list.setContext("selected-items", "none");
			
			// start work - update model root children:
			this.model.updateChildren(this.options.currentRoot);
			
			// set styles: 
			Dom.addClass(this.id, "dynamic-tree-picker");
			Dom.addClass(this.widgets.tree.id, "dynamic-tree-list dynamic-tree color-hover");
			Dom.addClass(this.widgets.list.id, "dynamic-tree-list dynamic-list color-hover");

			// if there is nothing in header, hide it:
			this._hideHeaderIfEmpty();

			if (this.options.autoShow)
				this.show();
		},
		
		onNewRootSelected: function(e) {
			var newRoot = e.newValue;
			this.options.currentRoot = newRoot;
			this.widgets.tree.setContext(newRoot, "none");
			this.model.updateChildren(newRoot);
		},
		
		_hideHeaderIfEmpty: function() {
			var headerItems = Selector.query(".picker-header > *", this.id);
			for(var i = 0; i < headerItems.length; i++) {
				if(Dom.getStyle(headerItems[i], "display") != "none") {
					return;
				}
			}
			Dom.addClass(Selector.query(".picker-header", this.id), "hidden");
		},
		
		// onListItemSelected is called, when list item is selected
		onListItemSelected: function(args) {
			return false; // do not toggle
		},

		// convert array to map, based on key property
		_prepareMap: function(array, keyProp) {
			var map = {};
			for(var i = 0; i < array.length; i++) {
				map[array[i][keyProp]] = array[i];
			}
			return map;
		},
		
		/**
		 * Set currently selected items.
		 * Items should be taken from model, probably not the same as this.model, but configured appropriately.
		 */
		setSelectedItems: function(items) {
			var root = this.model.getItem("selected-items");
			
			var oldItems = this._prepareMap(root._item_children_, "_item_name_");
			var newItems = this._prepareMap(items, "_item_name_");

			// process removed items:
			for(var name in oldItems) {
				if(!oldItems.hasOwnProperty(name)) continue;
				if(newItems.hasOwnProperty(name)) continue;
				// unset selected and update item
				this.model.deleteItem(name, root, true);
			}
			
			// process added items:
			for(var name in newItems) {
				if(!newItems.hasOwnProperty(name)) continue;
				if(oldItems.hasOwnProperty(name)) continue;
				var item = this.model.getItem(name);
				var exist = item != null;
				if(!exist) {
					item = newItems[name];
				}
				item.selected = "yes";
				this.model.addItem(item, root, exist);
			}
		},

		/**
		 * Show dynamic-tree-picker dialog.
		 */
		show: function() {
			if(this.options.multipleSelectMode) {
				Dom.removeClass(this.id, "single-selector");
			} else {
				Dom.addClass(this.id, "single-selector");
			} 
			this.widgets.dialog.show();
			if(this.searchEnabled) {
				Dom.get(this.id + "-searchText").focus();
			}
		},
		
		/**
		 * Hide dynamic-tree-picker dialog.
		 */
		hide: function() {
			this.widgets.dialog.hide();
		},
		
		/**
		 * Event handler - button OK was clicked.
		 * Fires event itemsSelected and hides dialog.
		 */
		onButtonOKClick: function(e) {
			var selectedItems = this.model.getItem("selected-items")._item_children_;
			this.fireEvent("itemsSelected", [].concat(selectedItems));
			this.hide();
		},

		/**
		 * Event handler - button Cancel was clicked.
		 * Silently hides dialog,
		 */
		onButtonCancelClick: function(e) {
			this.hide();
		},
		
		/**
		 * Event handler - button Search was clicked.
		 * Initiates search.
		 */
		onSearch: function(e) {
			var input = Dom.get(this.id + "-searchText");
			var query = input.value, context = this.options.currentRoot;
			if(query) {
				var search = this.model.getItem("search");
				search.query = query+"*";
				context = "search";
			}
			this.widgets.tree.setContext(context, "none");
			this.model.updateChildren(context);
		},

		/**
		 * Event handler - button itemSelect was clicked.
		 * Adds item to 'selected-items' root and sets 'selected' field to "yes".
		 */
		onItemSelect: function(args) {
			var item = this.model.getItem(args.item);
			this.model.addItem(item, "selected-items", true);
		},
	
		/**
		 * Event handler - button itemUnselect was clicked.
		 * Removes item from 'selected-items' root and sets 'selected' field to "no".
		 */
		onItemUnselect: function(args) {
			var item = this.model.getItem(args.item);
			this.model.deleteItem(item, "selected-items", true);
		},
		
		/**
		 * Event handler - child was added.
		 * If item was added to "selected-items", set "selected" field.
		 */
		onChildAdded: function(args) {
			if(this.model.getItemName(args.from) == "selected-items") {
				var item = this.model.getItem(args.item);
				item.selected = "yes";
				this.model.updateItem(item, true);
			}
			this.setSelectStyle();
		},
		
		/**
		 * Event handler - child was deleted.
		 * If item was deleted from "selected-items", unset "selected" field.
		 */
		onChildDeleted: function(args) {
			if(this.model.getItemName(args.from) == "selected-items") {
				var item = this.model.getItem(args.item);
				item.selected = "no";
				this.model.updateItem(item, true);
			}
			this.setSelectStyle();
		},
		
		/**
		 * Event handler - item was updated.
		 * Changes picker style class: "selected" if something was selected, or nothing otherwise.
		 */
		setSelectStyle: function() {
			var children = this.model.getItem("selected-items")._item_children_;
			if(!children || children.length == 0) {
				Dom.removeClass(this.id, "selected");
			} else {
				Dom.addClass(this.id, "selected");
			}
		},
	
	});
	
	
	/**
	 * Dynamic Tree Picker Control.
	 * It is a form control, that is based on Dynamic Tree Picker Dialog.
	 * It consists of current-value item list, Select button and Picker Dialog. 
	 */
	Citeck.widget.DynamicTreePickerControl = function(htmlid, fieldId, pageHtmlId) {
		Citeck.widget.DynamicTreePickerControl.superclass.constructor.call(this, "Citeck.widget.DynamicTreePickerControl", htmlid, ["button"]);
		this.fieldId = fieldId;
		this.pageHtmlId = pageHtmlId;
		
		this.eventGroup = htmlid;
		
		// create dialog:
		this.widgets.dialog = new Citeck.widget.DynamicTreePicker(this.id + "-picker");
		
		// listen to its event:
		this.widgets.dialog.subscribe("itemsSelected", this.onItemsSelected, this, true);
		
		// listen to form destroy event:
		YAHOO.Bubbling.on("formContainerDestroyed", this.onFormContainerDestroyed, this);
		
	};
	
	YAHOO.extend(Citeck.widget.DynamicTreePickerControl, Alfresco.component.Base, {
	
		// default values for options
		options: {
		
			// true if field is mandatory
			mandatory: false,
			
			// true if multiple items can be selected
			multipleSelectMode: true,
			
			// model configuration
			model: {},
			
			// list (current-value) configuration
			list: {},
			
			// forms configuration
			forms: {},
			
			// field value delimiter (in multipleSelectMode)
			fieldValueDelim: ",",
			
			// allow to select new objects:
			allowSelectAction: true,
			
			// allow to remove old objects:
			allowRemoveAction: true,
			
			// allow to remove all objects:
			allowRemoveAllAction: true,
			
			// custom select button label:
			selectButtonLabel: null,
			
			// auto destroy with form container
			destroyWithFormContainer: false

		},
	
		// transmit options to dialog
		setOptions: function(options) {
			Citeck.widget.DynamicTreePickerControl.superclass.setOptions.call(this, options);
			this.widgets.dialog.setOptions(options);
			return this;
		},
	
		// transmit messages to dialog
		setMessages: function(messages) {
			Citeck.widget.DynamicTreePickerControl.superclass.setMessages.call(this, messages);
			this.widgets.dialog.setMessages(messages);
			return this;
		},
	
		onReady: function() {
		
			this._initModel();
		
			this._initCurrentValuesList();

			// set allow/deny classes
			var controlEl = Dom.get(this.id);
			Dom.addClass(controlEl, this.options.allowSelectAction ? "allow-select" : "deny-select");
			Dom.addClass(controlEl, this.options.allowRemoveAction ? "allow-remove" : "deny-remove");
			Dom.addClass(controlEl, this.options.allowRemoveAllAction ? "allow-remove-all" : "deny-remove-all");

			this._initButtons();

			// data fields:
			this.field = Dom.get(this.fieldId);
			this.fieldAdded = Dom.get(this.id + "-added");
			this.fieldRemoved = Dom.get(this.id + "-removed");

			// fill model with original selected items:
			this.originalItems = this._deserialize(this.field.value);
			var itemsToAdd = [],
				itemsToRemove = [];
			if (this.fieldAdded && this.fieldRemoved) {
				itemsToAdd = this._deserialize(this.fieldAdded.value);
				itemsToRemove = this._deserialize(this.fieldRemoved.value);
			}
			var items = _.difference(_.uniq(_.union(this.originalItems, itemsToAdd)), itemsToRemove);
			this.selectItems(items);

			// tell the world, that we are ready:
			// object-finder compatible message
			this.initialized = true;
			YAHOO.Bubbling.fire("objectFinderReady", {
				eventGroup: this
			});
			
		},
		
		_initModel: function() {
			// take model from dialog
			var modelConfig = this.options.model;
			this.model = new Citeck.util.HierarchyModel(this.name);
			this.model.setConfig(modelConfig);
			
			// create "selected-items" item
			this.model.createSpecialItem({}, {
				name: "selected-items",
				keys: ["selected-items"]
			});

			// subscribe on model events
			this.model.subscribe("childrenUpdated", this.onChildrenUpdated, this, true);
			this.model.subscribe("childAdded", this.onChildAdded, this, true);
			this.model.subscribe("childDeleted", this.onChildDeleted, this, true);
		},

		_initCurrentValuesList: function() {
			this.widgets.list = new Citeck.widget.DynamicTree(this.id + "-currentValueDisplay", this.model, this.name);
			this.widgets.list.setConfig(this.options.list);
			
			this.widgets.list.subscribe("render", this.onRender, this, true);

			// set context
			this.widgets.list.setContext("selected-items", "none");

			// subscribe on button events:
			$buttonSubscribe("itemUnselect", this.onItemUnselect, this, [this.widgets.list.id]);

			// set styles: 
			Dom.addClass(this.widgets.list.id, "dynamic-tree-list dynamic-list hide-buttons");
		},

		_initButtons: function() {
			// initialize widgets
			if(this.options.allowSelectAction) {
				this.widgets.buttonSelect = new YAHOO.widget.Button({
					type: "button",
					container: this.id + "-itemGroupActions",
					label: this.options.selectButtonLabel || this.msg("button.select"),
					onclick: {
						scope: this,
						fn: this.onButtonSelectClick,
					},
				});
			}
		},
		
		/**
		 * Event handler - tree was rendered.
		 * Tell the world about update.
		 */
		onRender: function() {
			YAHOO.Bubbling.fire("renderCurrentValue", {
				eventGroup: this
			});
		},

		/**
		 * Event handler - current form is being destroyed.
		 * Destroy the component too.
		 */
		onFormContainerDestroyed: function(layer, args) {
			if(this.options.destroyWithFormContainer) {
				this.destroy();
			}
		},

		/**
		 * Called on destroy.
		 * Unsubscribe from button events.
		 */
		destroy: function() {
			YAHOO.Bubbling.unsubscribe("formContainerDestroyed", this.onFormContainerDestroyed, this);
			if (this.model) {
				this.model.unsubscribeAll();
				delete this.model;
			}
			if (this.widgets) {
				if (this.widgets.dialog)
					this.widgets.dialog.unsubscribeAll(this);
				if (this.widgets.list)
					this.widgets.list.unsubscribeAll(this);
			}
			$buttonUnsubscribe(this);
			Citeck.widget.DynamicTreePickerControl.superclass.destroy.call(this);
		},

		// deserialize comma-separated string
		_deserialize: function(value) {
			return value ? value.split(this.options.fieldValueDelim) : [];
		},

		/**
		 * Set items to be selected.
		 * Items are specified as identifiers, other information is actively retrieved from repository.
		 * @param items - array of item identifiers or string of identifiers (serialized, as it is stored in field value)
		 */
		selectItems: function(items) {
		
			if(typeof items != "object") {
				items = this._deserialize(items);
			}
			
			// update items:
			var selected = this.model.getItem("selected-items");
			var children = [];
			for(var i = 0; i < items.length; i++) {
				children[i] = {};
				children[i]["_item_name_"] = items[i];
				children[i][this.options.forms.nodeId] = items[i];
				children[i]["selected-index"] = i;
			}
			
			selected._item_children_ = children;
			this.model.updateChildren("selected-items", false);
			
			if(this.initialized) {
				this._updateFields();
			}
		},
        
		/**
		 * Get currently selected items.
		 * @return items - array of item identifiers
		 */
        getSelectedItems: function() {
            var result = [];
			var selected = this.model.getItem("selected-items")._item_children_;
            for(var i in selected) {
                result.push(selected[i][this.options.forms.nodeId]);
            }
            return result;
        },
		
		/**
		 * Event handler - Select button was clicked.
		 * Sets dialog selected items to currently selected items and shows dialog.
		 */
		onButtonSelectClick: function() {
			var items = this.model.getItem("selected-items")._item_children_;
			this.widgets.dialog.setSelectedItems(items);
			this.widgets.dialog.show();
		},
		
		/**
		 * Event handler - Unselect button was clicked.
		 * Updates model's 'selected-items' and form-fields.
		 */
		onItemUnselect: function(args) {
			// update model:
			this.model.deleteItem(args.item, args.from, true);
		},

		/**
		 * Event handler - Picker dialog reports, that items were selected.
		 * Updates model's 'selected-items' and form-fields.
		 */
		onItemsSelected: function(selectedItems) {
			// update model:
			this.model.getItem("selected-items")._item_children_ = selectedItems;
			this.model.updateChildren("selected-items", true);
		},
		
		/**
		 * Event handler - child was added.
		 * If item was added to "selected-items", set "selected" field.
		 */
		onChildAdded: function(args) {
			if(this.model.getItemName(args.from) == "selected-items") {
				var item = this.model.getItem(args.item);
				item.selected = "yes";
				// fire itemUpdated to renew all controls
				this.model.updateItem(item, true);
				// update fields:
				this._updateFields();
			}
		},
		
		/**
		 * Event handler - child was deleted.
		 * If item was added to "selected-items", unset "selected" field.
		 */
		onChildDeleted: function(args) {
			if(this.model.getItemName(args.from) == "selected-items") {
				var item = this.model.getItem(args.item);
				item.selected = "no";
				// fire itemUpdated to renew all controls
				this.model.updateItem(item, true);
				// update fields:
				this._updateFields();
			}
		},
		
		onChildrenUpdated: function(args) {
			if(this.model.getItemName(args.from) == "selected-items") {
				// update fields:
				this._updateFields();
			}
		},
		
		/**
		 * Updates fields of control.
		 * Maintains main field (with comma-separated value) and added/removed fields.
		 * Fires mandatoryControlValueUpdated event, if the field is mandatory.
		 */
		_updateFields: function() {
			var selectedItems = this.model.getItem("selected-items")._item_children_;
		
			// calculate current, added, removed
			var current = [], 
				added = [], 
				removed = [].concat(this.originalItems);
				
			
			for(var i in selectedItems) {
				if(!selectedItems.hasOwnProperty(i)) continue;
				var id = selectedItems[i][this.options.forms.nodeId];
				current.push(id);
				var index = removed.indexOf(id);
				if(index == -1) {
					added.push(id);
				} else {
					removed.splice(index, 1);
				}
			}
			
			var delim = this.options.fieldValueDelim;
			this.field.value = current.join(delim);
			if (this.fieldAdded)
				this.fieldAdded.value = added.join(delim);
			if (this.fieldRemoved)
				this.fieldRemoved.value = removed.join(delim);
			
			// notify mandatory constraint checker, if necessary
			if (this.options.mandatory) {
				YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
			}

		},

	});

})();
