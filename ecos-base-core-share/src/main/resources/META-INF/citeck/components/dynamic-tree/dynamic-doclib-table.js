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
	
	var RESCONTEXT = Alfresco.constants.URL_RESCONTEXT;
	
	Citeck.widget.DynamicDoclibTable = function(htmlid, model, name) {
		
		Citeck.widget.DynamicDoclibTable.superclass.constructor.call(this, name || "Citeck.widget.DynamicDoclibTable", htmlid, [ "slider" ]);
	
		this.model = model;
		this.state = {};
		
		var doclist = this.widgets.doclist = new Citeck.widget.HandyDocumentList(this.id + "-doclist");
		doclist.setOptions({
			rootNode: "alfresco://company/home",
			oldSchoolActions: true
		});
		
		YAHOO.Bubbling.on("handyDoclistReady", function(layer, args) {
			if(args[1].eventGroup != this.id + "-doclist") return;
			YAHOO.Bubbling.fire("dynamicDoclibTableLoaded", {
				scope: this,
				eventGroup: this.id
			});
		}, this);
		
		model.subscribe("itemUpdated", this.onItemUpdated, this, true);
		model.subscribe("childrenUpdated", this.onChildrenUpdated, this, true);
		model.subscribe("childAdded", this.onChildAdded, this, true);
		model.subscribe("childDeleted", this.onChildDeleted, this, true);
	};

	YAHOO.extend(Citeck.widget.DynamicDoclibTable, Alfresco.component.Base);
	YAHOO.lang.augmentObject(Citeck.widget.DynamicDoclibTable.prototype, YAHOO.util.EventProvider.prototype);
	YAHOO.lang.augmentObject(Citeck.widget.DynamicDoclibTable.prototype, {

		// load modules - dependencies of dynamic-doclib-table
		loadModules: function(modules) {
//			this.loadModulesLoaderImpl(modules);
			this.loadModulesGetImpl(modules);
		},
		
		// loadModules implementation via YUI Loader Utility
		// CAUTION: this implementation is unsafe - see https://tools.citeck.ru/issues/2329
		loadModulesLoaderImpl: function(modules) {
			var names = [];
			for(var i in modules) {
                if(!modules.hasOwnProperty(i)) continue;
				var module = modules[i];
				this.yuiLoader.addModule(module);
				names.push(module.name);
			}
			this.yuiLoader.require(names);
			this.yuiLoader.insert();
		},
	
		// loadModules implementation via YUI Get Utility
		loadModulesGetImpl: function(modules) {
			var js = [],
				css = [];
			for(var i in modules) {
                if(!modules.hasOwnProperty(i)) continue;
				var path = modules[i].fullpath || RESCONTEXT + modules[i].path;
				if(modules[i].type == "js") {
					js.push(path);
				} else if(modules[i].type == "css") {
					css.push(path)
				}
			}
			YAHOO.util.Get.css(css);
			YAHOO.util.Get.script(js, {
				onSuccess: this.bind(this.onModuleLoaded)
			});
		},
		
		onModuleLoaded: function() {
		
			if(!this.doclistLoaded) {
				this.loadModules(this.options.customModules);

				this.doclistLoaded = true;
				
			} else if(!this.modulesLoaded) {
				
				// if there are some items to load - do it
				if(this.itemsToLoad) {
					this.widgets.doclist.loadData(this.itemsToLoad);
				}
				
				this.modulesLoaded = true;
			}
			
		},
		
		/**
		 * Set root element of control
		 */
		setContext: function(item, parent) {
			this.state.item = this.model.getItem(item);
			this.state.parent = this.model.getItem(parent);
		},

		onItemUpdated: function(args) {
			var item = this.model.getItem(args.item);
			var found = false;
			var items = this.state.item._item_children_;
			for(var i = 0; i < items.length; i++) {
				if(items[i]._item_name_ == item._item_name_) {
					items.splice(i, 1, item);
					found = true;
				}
			}
			if(found) {
				this.widgets.doclist.loadData(items);
			}
		},
		
		/**
		 * Event handler - children of some element were updated.
		 * This adds child to documentlist.
		 */
		onChildrenUpdated: function(args) {
		
			var parent = this.model.getItem(args.from);
			if(parent != this.state.item) {
				return;
			}
			
			var items = parent._item_children_;
			
			this.widgets.doclist.loadData(items);
			
		},
		
		/**
		 * Event handler - child was added to model.
		 * This adds child to documentlist.
		 */
		onChildAdded: function() {
			// use simple implementation - update the whole list
			this.onChildrenUpdated.apply(this, arguments);
		},

		/**
		 * Event handler - child was deleted from model.
		 * This deletes child from documentlist.
		 */
		onChildDeleted: function() {
			// use simple implementation - update the whole list
			this.onChildrenUpdated.apply(this, arguments);
		},

	});

})();