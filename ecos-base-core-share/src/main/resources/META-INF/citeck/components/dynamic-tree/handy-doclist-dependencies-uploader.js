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

	Citeck.widget.HandyDocumentListUploader = function(htmlid, items) {

		Citeck.widget.HandyDocumentListUploader.superclass.constructor.call(this, "Citeck.widget.HandyDocumentListUploader", htmlid);

		this.itemsToLoad = items;

		this.yuiLoader = new YAHOO.util.YUILoader({
			base: RESCONTEXT,
			onSuccess: this.onModuleLoaded,
			onFailure: this.onModuleLoadFailed,
			scope: this
		});

		this.options.modules = [
			{
				name: "slider.js",
				type: "js",
				path: "yui/slider/slider.js"
			},
			{
				name: "actions.css",
				type: "css",
				path: "components/documentlibrary/actions.css",
			},
			{
				name: "actions.css",
				type: "css",
				path: "components/documentlibrary/actions.css",
			},
			{
				name: "actions.js",
				type: "js",
				path: "components/documentlibrary/actions.js",
			},
			{
				name: "doclib-actions.js",
				type: "js",
				path: "modules/documentlibrary/doclib-actions.js",
			},
			{
				name: "documentlist.css",
				type: "css",
				path: "components/documentlibrary/documentlist.css",
			},
			{
				name: "documentlist.css",
				type: "css",
				path: "components/documentlibrary/documentlist.css",
			},
			{
				name: "documentlist-messages.js",
				type: "js",
				fullpath: Alfresco.constants.URL_SERVICECONTEXT + "components/org/alfresco/components/documentlibrary/documentlist.get/messages?format=js&scope=Alfresco.DocumentList",
			},
			{
				name: "documentlist.js",
				type: "js",
				path: "components/documentlibrary/documentlist.js",
				requires: [ "actions.js", "doclib-actions.js", "documentlist-messages.js" ]
			},
			{
				name: "documentlist-view-detailed.js",
				type: "js",
				path: "components/documentlibrary/documentlist-view-detailed.js",
				requires: [ "documentlist.js" ]
			},
			{
				name: "documentlist-view-simple.js",
				type: "js",
				path: "components/documentlibrary/documentlist-view-simple.js",
				requires: [ "documentlist-view-detailed.js" ]
			},
			{
				name: "documentlist-view-gallery.js",
				type: "js",
				path: "components/documentlibrary/documentlist-view-gallery.js",
				requires: [ "documentlist-view-detailed.js" ]
			},
			{
				name: "handy-doclist.js",
				type: "js",
				path: "citeck/components/dynamic-tree/handy-doclist.js",
				requires: [ "documentlist.js", "actions.css", "documentlist.css" ]
			}
		];

		this.options.customModules = [
			{
				name: "DocLibCustom.css",
				type: "css",
				fullpath: Alfresco.constants.URL_SERVICECONTEXT + "components/dependencies/DocLibCustom.css",
			},
			{
				name: "DocLibCustom.js",
				type: "js",
				fullpath: Alfresco.constants.URL_SERVICECONTEXT + "components/dependencies/DocLibCustom.js",
			}
		];

	};

	YAHOO.extend(Citeck.widget.HandyDocumentListUploader, Alfresco.component.Base);
	YAHOO.lang.augmentObject(Citeck.widget.HandyDocumentListUploader.prototype, YAHOO.util.EventProvider.prototype);
	YAHOO.lang.augmentObject(Citeck.widget.HandyDocumentListUploader.prototype, {

		// load modules - dependencies of dynamic-doclib-table
		loadModules: function(modules) {
			this.loadModulesGetImpl(modules);
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

		onReady: function() {
			this.loadModules(this.options.modules);
		},

		onModuleLoaded: function() {

			if(!this.doclistLoaded) {
				var doclist = this.widgets.doclist = new Citeck.widget.HandyDocumentList(this.id + "-doclist");
				doclist.setOptions({
					rootNode: "alfresco://company/home"
				});

				this.widgets.doclist.onReady();

				this.loadModules(this.options.customModules);

				this.doclistLoaded = true;

			} else if(!this.modulesLoaded) {

				// if there are some items to load - do it
				if(this.itemsToLoad) {
					this.widgets.doclist.loadData(this.itemsToLoad);
				}

				YAHOO.Bubbling.fire("dynamicSupplDoclibTableLoaded", {
					scope: this,
					eventGroup: this.id
				});

				this.modulesLoaded = true;
			}

		},

	});

})();