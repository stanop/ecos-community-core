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
Citeck.util = Citeck.util || {};
Citeck.widget = Citeck.widget || {};

var Dom = YAHOO.util.Dom;


/**
 * Drag-n-drop object class
 */

var DDList = function(id) {
	DDList.superclass.constructor.call(this, id);
	var src = Dom.get(id); // element, that was clicked
	var el = this.getDragEl(); // element to be dragged instead of src
	// styles of drag-element - as in src element + transparency
	el.setAttribute("class", src.getAttribute("class"));
	Dom.setStyle(el, "opacity", 0.67);
	Dom.addClass(el, "proxy");
	Dom.setStyle(el, "border", "none");
	// private properties
	this.y = 0;
	this.goingUp = false;
	
	this.createEvent("endDrag");
}

YAHOO.extend(DDList, YAHOO.util.DDProxy, {

	// drag begins - hide src-el, show drag-el
	startDrag: function(x, y) {
		var dragEl = this.getDragEl();
		var clickEl = this.getEl();
		Dom.setStyle(clickEl, "visibility", "hidden");
		Dom.setStyle(dragEl, "visibility", "visible");
		dragEl.innerHTML = clickEl.innerHTML;
	},

	// drag ends - show src-el, hide drag-el, fire event
	endDrag: function (e) {
		var dragEl = this.getDragEl();
		var clickEl = this.getEl();
		Dom.setStyle(dragEl, "visibility", "hidden");
		Dom.setStyle(clickEl, "visibility", "inherit");
		this.fireEvent("endDrag");
	},

	// while dragging - manage "y" and "goingUp" properties
	onDrag: function(e) {
		var y = YAHOO.util.Event.getPageY(e);
		this.goingUp = (y < this.y);
		this.y = y;
	},

	// when dragged over other draggable object
	onDragOver: function (e, id) {
		var clickEl = this.getEl();
		var destEl = Dom.get(id);
		if(destEl == clickEl) return;

		var p = clickEl.parentNode;
		// peer object
		if(p.parentNode == destEl.parentNode.parentNode) {
			if(this.goingUp) {
				Dom.insertBefore(clickEl, destEl);
			} else {
				Dom.insertAfter(clickEl, destEl);
			}
		// parent object
		} else if(p != destEl && p.parentNode == destEl.parentNode) {
			if(this.goingUp) {
				Dom.insertAfter(clickEl, destEl.lastChild);
			} else {
				// after, because firstChild is reserved for caption
				Dom.insertAfter(clickEl, destEl.firstChild);
			}
		}	
	},

});

/**
 * GroupModel - data representation of items and item groups
 */
 
Citeck.util.GroupModel = function(options) {

	// array of groups
	// each group is itself array of items
	this.groups = [];
	
	// map item to its group index
	this.itemGroups = {};
	
	this.options = _.defaults(options, {
	    addDefaultGroup: true
	});

	// register events:
	this.createEvent("itemAdded");    // item added to group
	this.createEvent("itemDeleted");  // item deleted from group
	this.createEvent("groupAdded");   // new group added
	this.createEvent("groupDeleted"); // group deleted
	this.createEvent("modelUpdated"); // the whole model updated

};

YAHOO.lang.augmentObject(Citeck.util.GroupModel.prototype, YAHOO.util.EventProvider.prototype);
YAHOO.lang.augmentObject(Citeck.util.GroupModel.prototype, {

	/**
	 * Update model items (groups were not changed).
	 * It is called, when a wrapped control changes selected items.
	 * - deleted items are deleted, 
	 * - existing items are left in there groups 
	 * - new items are added to new group
	 * @param items - new set of items
	 */
	updateItems: function(items) {
	
		// process deleted items:
		for(var i = 0; i < this.groups.length; i++) {
			var groupItems = this.groups[i];
			for(var j = groupItems.length; j--; ) {
				var item = groupItems[j];
				if(items.indexOf(item) == -1) {
					this.groups[i].splice(j, 1);
					delete this.itemGroups[item];
				}
			}
		}
		
		// process empty groups at the end:
		for(var i = this.groups.length; i--; ) {
			if(this.groups[i].length > 0) {
				break;
			}
			this.groups.splice(i, 1);
		}
		
		// add default group:
		if(this.groups.length == 0 || this.groups[this.groups.length-1].length > 0) {
			var defaultGroupId = this.groups.length;
			this.groups[defaultGroupId] = [];
		} else {
			var defaultGroupId = this.groups.length-1;
		}
		var defaultGroup = this.groups[defaultGroupId];
		
		// process added items:
		for(var i = 0; i < items.length; i++) {
			var item = items[i];
			if(this.itemGroups.hasOwnProperty(item)) {
				continue;
			}
			defaultGroup.push(item);
			this.itemGroups[item] = defaultGroupId;
		}
		
		// add default group:
		if(this.options.addDefaultGroup) {
		    if(defaultGroup.length > 0) {
		        this.groups.push([]);
		    }
		} else {
		    if(defaultGroup.length == 0) {
		        this.groups.pop();
		    }
		}
		
		// now fire events:
		for(var i = 0; i < this.groups.length; i++) {
			this.fireEvent("groupAdded", { group: i });
			for(var j = 0; j < this.groups[i].length; j++) {
				var item = this.groups[i][j];
				this.fireEvent("itemAdded", { item: item, group: i });
			}
		}
		
		this.fireEvent("modelUpdated", this.groups);
	},

	/**
	 * Update model groups (items were not changed).
	 * It is called when drag-n-drop control changes dom tree and it is necessary to sync model with view (this was much simpler).
	 * Also it is called on init to set model groups.
	 * @param groups - new groups data in format [ [ item11, item12, ... ], [ item21, item22, ... ], ... ]
	 */
	updateGroups: function(groups) {
	
		this.groups = groups;
		
		// fill item groups:
		for(var i = 0; i < this.groups.length; i++) {
			for(var j = 0; j < this.groups[i].length; j++) {
				this.itemGroups[this.groups[i][j]] = i;
			}
		}
		
		// remove all empty groups in the end:
		if(this.groups.length == 0 || this.groups[this.groups.length-1].length > 0) {
			this.groups.push([]);
			this.fireEvent("groupAdded", { group: this.groups.length-1 });
		} else {
			for(var i = this.groups.length-1; i--; ) {
				if(this.groups[i].length > 0) break;
				this.groups.splice(i, 1);
				this.fireEvent("groupDeleted", { group: i });
			}
		}

		this.fireEvent("modelUpdated", this.groups);
	},
	
});

/**
 * Group View - component to wrap selected items, render them into groups and allow drag-n-drop.
 */
Citeck.widget.GroupView = function(id, model) {

	this.id = id;
	this.model = model;
	
	this.options = {
		dragItems: true, // allow dragging of items
		dragGroups: true, // allow dragging of groups
		groupTitleSuffix: "",
		itemSelector: "> *", // css selector to select items from container
	};	

	// subscribe on model updates:
	model.subscribe("itemAdded", this.onItemAdded, this, true);
	model.subscribe("itemDeleted", this.onItemDeleted, this, true);
	model.subscribe("groupAdded", this.onGroupAdded, this, true);
	model.subscribe("groupDeleted", this.onGroupDeleted, this, true);
	
};

YAHOO.lang.augmentObject(Citeck.widget.GroupView.prototype, YAHOO.util.EventProvider.prototype);
YAHOO.lang.augmentObject(Citeck.widget.GroupView.prototype, {

	// inherit some Base methods implementation
	setOptions: Alfresco.component.Base.prototype.setOptions,
	bind: Alfresco.component.Base.prototype.bind,

	/**
	 * Fetch just-rendered html elements,
	 * that should correspond to passed items one-to-one.
	 */
	init: function(items) {
		var elements = Selector.query(this.options.itemSelector, this.id);
		
		// if something went wrong - log error message and quit
		if(elements.length != items.length) {
			Alfresco.logger.error("number of elements differs from number of items, possibly, wrong item selector is used")
			return;
		}

		this.itemElems = {};
		
		// for each element:
		for(var i = 0; i < elements.length; i++) {
			var el = elements[i].parentNode.removeChild(elements[i]);
			el.itemId = items[i];
			if(this.options.dragItems) {
				this._createDD(el);
				Dom.addClass(el, "draggable");
			}
			this.itemElems[items[i]] = el;
		}
		// finally clear container:
		Dom.get(this.id).innerHTML = "";
	},
	
	// utility funciton to create drag-n-drop object for specified element
	_createDD: function(el) {
		var dd = new DDList(el);
		dd.subscribe("endDrag", this.onEndDrag, this, true);
	},
	
	// utility function to create new group element
	_createGroupEl: function() {
		var groupEl = document.createElement("div");
		Dom.addClass(groupEl, "item-group");
		var caption = document.createElement("p");
		Dom.addClass(caption, "item-group-caption");
		Dom.setAttribute(caption, "titlesuffix", this.options.groupTitleSuffix);
		groupEl.appendChild(caption);
		if(this.options.dragGroups) {
			this._createDD(groupEl);
			Dom.addClass(caption, "draggable");
		}
		return groupEl;
	},
	
	/**
	 * Event handler - item was added to group.
	 * Add item element to group element (create if not existed).
	 */
	onItemAdded: function(args) {
	
		var itemId = args.item,
			container = Dom.get(this.id),
			itemEl = this.itemElems[itemId],
			groupEl = container.childNodes[args.group];
		
		// if no group element found, create it:
		if(!groupEl) {
			groupEl = this._createGroupEl();
			container.appendChild(groupEl);
		}

		// add element to it:
		groupEl.appendChild(itemEl);
	
	},

	/**
	 * Event handler - item was deleted from group.
	 * Delete item element from group element (if such).
	 */
	onItemDeleted: function(args) {
		var itemId = args.item,
			container = Dom.get(this.id),
			itemEl = this.itemElems[itemId],
			groupEl = container.childNodes[args.group];

		if(groupEl && itemEl) {
			groupEl.removeChild(itemEl);
		}
	},

	/**
	 * Event handler - group added.
	 * Create new group element.
	 */
	onGroupAdded: function(args) {
		var groupId = args.group,
			container = Dom.get(this.id);
		if(container.childNodes[groupId] == null) {
			var groupEl = this._createGroupEl(groupId);
			container.appendChild(groupEl);
		}
	},

	/**
	 * Event handler - group deleted.
	 * Delete corresponding group element.
	 */
	onGroupDeleted: function(args) {
		var container = Dom.get(this.id),
			groupEl = container.childNodes[args.group];
		container.removeChild(groupEl);
	},

	/**
	 * Event handler - item dropped (drag-n-drop finished).
	 * Read new groups model from dom tree and update model groups.
	 */
	onEndDrag: function() {
	
		// get new model from dom:
		var container = Dom.get(this.id);
		var groups = [];
		
		for(var groupElem = container.firstChild; groupElem != null; groupElem = groupElem.nextSibling) {
			var group = [];
			groups.push(group);
			for(var itemElem = groupElem.firstChild; itemElem != null; itemElem = itemElem.nextSibling) {
				if(itemElem.itemId) {
					group.push(itemElem.itemId);
				}
			}
		}
		
		// update model
		this.model.updateGroups(groups);
		
	},
	
});



/**
 * Form control, that groups items of container.
 * Wraps some other control, that maintains selected items and allows to group these items into groups.
 */
Citeck.widget.Groupable = function(id) {
	Citeck.widget.Groupable.superclass.constructor.call(this, "Citeck.widget.Groupable", id);
	
	this.options.itemSep = '|';
	this.options.groupSep = ',';
	this.options.itemSelector = "> *";
	this.options.groupTitleSuffix = "";
	
}

YAHOO.extend(Citeck.widget.Groupable, Alfresco.component.Base, {
    
    options: {
        
        disabled: false
        
    },

	onReady: function() {

		// create model and view:
		this.model = new Citeck.util.GroupModel({
		    addDefaultGroup: !this.options.disabled
		});
		this.view = new Citeck.widget.GroupView(this.options.itemsContainerId, this.model);
		this.view.setOptions({
			dragItems: this.options.dragItems,
			dragGroups: this.options.dragGroups,
			itemSelector: this.options.itemSelector,
			groupTitleSuffix: this.options.groupTitleSuffix,
		});

		// subscribe on model updates (maintain field value)
		this.model.subscribe("modelUpdated", this.onModelUpdated, this, true);
		
		// subscribe on wrapped control updates (update model)
		YAHOO.Bubbling.on("renderCurrentValue", this.onValueChanged, this);
	
		// initialize model from field:
		// but only when container is also ready:
		YAHOO.util.Event.onAvailable(this.options.itemsContainerId, function() {
			this.model.updateGroups(this._deserialize(Dom.get(this.id).value));
		}, this, true);

	},
	
	/**
	 * Update model groups
	 */
	updateGroups: function(precedence) {
		Dom.get(this.id).value = precedence;
		this.model.updateGroups(this._deserialize(Dom.get(this.id).value));
	},
	
	/**
	 * Called on destroy
	 */
	destroy: function() {
	
		YAHOO.Bubbling.unregister("renderCurrentValue", this.onValueChanged, this);
		
	},
	
	/**
	 * Get currently selected items from wrapped field.
	 */
	getCurrentItems: function() {
	
		// get value:
		var value = Dom.get(this.options.itemsFieldId).value;
		
		// get items:
		return value ? value.split(',') : [];
		
	},

	/**
	 * Event handler - model updated.
	 * Update field contents.
	 */
	onModelUpdated: function(groups) {
		Dom.get(this.id).value = this._serialize(groups);
	},

	/**
	 * Event handler - wrapped field value changed.
	 * Read new selected items from field, update view and model.
	 */
	onValueChanged: function(layer, args) {
	
		// only items field is interesting:
		if(args[1].eventGroup.id.substring(0, this.options.itemsFieldId.length) != this.options.itemsFieldId) {
			return;
		}
		
		// do it later, to ensure, that object-picker rendered its elements
		YAHOO.lang.later(0, this, function() {
		
			var items = this.getCurrentItems();

			// view should fetch rendered html elements:
			this.view.init(items);
			
			// update model:
			this.model.updateItems(items);
            YAHOO.Bubbling.fire("controlValueUpdated", this);
		});
	},

	// serialize groups structure into string
	_serialize: function(groups) {
		var strs = [];
		for(var i = 0; i < groups.length; i++) {
			strs[i] = groups[i].join(this.options.itemSep);
		}
		return strs.join(this.options.groupSep);
	},
	
	// deserialize string into groups structure
	_deserialize: function(str) {
		var strs = str ? str.split(this.options.groupSep) : [];
		for(var i = 0; i < strs.length; i++) {
			strs[i] = strs[i].split(this.options.itemSep);
		}
		return strs;
	},
	
});

})();

