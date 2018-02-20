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
	Citeck.widget.ButtonPanel = Citeck.widget.ButtonPanel || {};
	Citeck.widget.ButtonPanel.Commands = Citeck.widget.ButtonPanel.Commands || {};

	Citeck.widget.ButtonPanel.Commands.onPanelButtonUpload = function(options) {
		if (options.assocType) {
			var fui = Alfresco.getFileUploadInstance();
			var uploadUrl = "api/citeck/upload?assoctype=" + encodeURIComponent(options.assocType);
			if (options.contentType)
				uploadUrl += '&contenttype=' + encodeURIComponent(options.contentType);
			if (options.propertyName && options.propertyValue) {
                uploadUrl += '&propertyname=' + options.propertyName + '&propertyvalue=' + options.propertyValue;
			}
			if (Alfresco.util.CSRFPolicy && Alfresco.util.CSRFPolicy.getParameter && Alfresco.util.CSRFPolicy.getToken)
				uploadUrl += '&' + Alfresco.util.CSRFPolicy.getParameter() + '=' + encodeURIComponent(Alfresco.util.CSRFPolicy.getToken());
			//todo timefix
			//added for correct param  parsing
			uploadUrl += '&addedParam=param';
			var cfg = {
				destination: options.destination,
				htmlUploadURL: uploadUrl,
				flashUploadURL: uploadUrl,
				onFileUploadComplete: {
					scope: this,
					fn: function(response) {
						var files = response.successful;
						if (typeof files !== 'undefined' && files.length > 0) {
							YAHOO.Bubbling.fire("metadataRefresh");
						}
						else {
							alert(Alfresco.util.message("message.cannotupload"));
						}
					}
				}
			};
			if (options.singleSelectMode)
				cfg.mode = fui.MODE_SINGLE_UPDATE;
			fui.show(cfg);
		}
		else {
			// This situation is related with defining options of this module, so you do not need to specify localization for this message
			alert("Can not upload a file without specified association type");
		}
	};

	Citeck.widget.ButtonPanel.Commands.onPanelButtonEdit = function(options) {
		var formId = options.formId,
			itemId = options.nodeRef,
			htmlid = Alfresco.util.generateDomId(),
			name   = null;
		dialog = new Citeck.widget.EditFormDialog(
				htmlid,
				itemId,
				formId,
				null,
				name);
		dialog.setOptions({ width: '62em' });
		if (options.header) {
			dialog.setOptions({
				doBeforeDialogShow: {
					scope: this,
					fn: Citeck.utils.fnBeforeDialogShow({ headerId: options.header })
				}
			});
		}
		dialog.show();
		dialog.subscribe("itemEdited", function(response) {
			YAHOO.Bubbling.fire("metadataRefresh");
			console.dir(arguments);
		}, this, true);
	};

	Citeck.widget.ButtonPanel.Commands.onPanelButtonCreate = function(options, context) {
    if (options.assocType) {
      var itemId = options.contentType,
	        formId = options.formId || null,
	        dest = _parseConfigParam(options.destination, ""),
					afterCreateCallback = options.afterCreate ? new Function("persistedNode, ButtonPanel", options.afterCreate) : null;

      var dlg = new Citeck.forms.dialog(itemId, formId, {
        scope: this,
        fn: function(response) {
					if (afterCreateCallback) { afterCreateCallback(response, context); }
					YAHOO.Bubbling.fire("metadataRefresh");
				}
      }, {
        title: Alfresco.util.message(options.createTitle || "button.create"),
        destination: dest,
        destinationAssoc: options.assocType
      });
    }
    else {
      // This situation is related with defining options of this module, so you do not need to specify localization for this message
      alert("Can not create a document without specified association type");
    }
	};

	var _addCommandPickers = {};
	var _parseConfigParam = function(param, def) {
		return (typeof param !== 'undefined') ? param.replace(/\[\[/g, "{").replace(/\]\]/g, "}") : def;
	};
	var _parseAddCommandOptions = function(options) {
		var opts = {
			nodeRef: _parseConfigParam(options.nodeRef, ""), // it is a case node reference
			itemType: _parseConfigParam(options.itemType, (options.contentType ? options.contentType : "cm:content")),
			itemKey: _parseConfigParam(options.itemKey, "name"),
			itemTitle: _parseConfigParam(options.itemTitle, "{name}"),
			itemTitleProperties: _parseConfigParam(options.itemTitleProperties, ""),
			itemSearchProperties: _parseConfigParam(options.itemSearchProperties, "cm:name"),
			itemURL: _parseConfigParam(options.itemURL, ""),
			itemURLresults: _parseConfigParam(options.itemURLresults, ""),
			searchURLresults: _parseConfigParam(options.searchURLresults, "nodes"),
			rootURL: _parseConfigParam(options.rootURL, ""),
			rootURLresults: _parseConfigParam(options.rootURLresults, ""),
			caseElementConfigName: _parseConfigParam(options.caseElementConfigName, ""),
			addAssocScript: _parseConfigParam(options.addAssocScript, "")
		};
		if (options.onPanelButtonAdd && typeof options.onPanelButtonAdd === 'function')
			opts.onPanelButtonAdd = options.onPanelButtonAdd;
		var searchURL = _parseConfigParam(options.searchURL,
				"Alfresco.constants.PROXY_URI + \"citeck/search/simple?type=" +
				opts.itemType + "&property=" + opts.itemSearchProperties +
				(opts.itemTitleProperties ? "&properties=" + opts.itemTitleProperties : "") +
				"&value={query}&replaceColon=_\"");
		opts.searchURL = searchURL;
		var objectId = opts.itemType + '_' + opts.itemKey + '_' +
				opts.itemTitle + '_' + opts.itemURL + '_' +
				opts.searchURL + '_' + opts.rootURL;
		opts.objectId = objectId;
		return opts;
	};
	var _onSelectAddToCaseCommandItems = function(opts, items) {
		if (items && items.length > 0) {
			Alfresco.util.Ajax.jsonPut({
				url: Alfresco.constants.PROXY_URI + "citeck/case/elements",
				dataObj: {
					elements: _.map(items, function(item) {
					    return item.nodeRef;
					}),
					caseNode: opts.nodeRef,
					elementType: opts.caseElementConfigName
				},
				successCallback: {
					fn: function (response) {
						YAHOO.Bubbling.fire("metadataRefresh");
					},
					scope: this
				},
				failureMessage: Alfresco.util.message("message.cannot.add.items.to.case"),
				scope: this
			});
		}
	};
	Citeck.widget.ButtonPanel.Commands.onPanelButtonCaseAdd = function(options) {
		var opts = _parseAddCommandOptions(options),
			picker = _addCommandPickers[opts.objectId];
		if (!opts.nodeRef) {
			alert("Mandatory option case node reference - 'nodeRef' is not specified");
			return;
		}
		if (!opts.caseElementConfigName) {
			alert("Mandatory option 'caseElementConfigName' is not specified");
			return;
		}
		if (picker) {
			picker.setSelectedItems([]);
			picker.show();
		}
		else {
			var htmlid = Alfresco.util.generateDomId();
			// load dialog from web-script
			Alfresco.util.Ajax.request({
				url: Alfresco.constants.URL_SERVICECONTEXT + "citeck/components/dynamic-tree-picker",
				dataObj: {
					htmlid: htmlid,
					itemType: opts.itemType,
					itemKey: opts.itemKey,
					itemTitle: opts.itemTitle,
					itemURL: opts.itemURL,
					itemURLresults: opts.itemURLresults,
					searchURL: opts.searchURL,
					searchURLresults: opts.searchURLresults,
					rootURL: opts.rootURL,
					rootURLresults: opts.rootURLresults,

					// additional options
					preloadSearchQuery: options.preloadSearchQuery ? options.preloadSearchQuery : null
				},
				successCallback: {
					fn: function (response) {
						var element = document.createElement("DIV");
						element.innerHTML = response.serverResponse.responseText;
						YAHOO.util.Dom.addClass(element, "inner-panel-button-dialog");
						document.getElementsByTagName('body')[0].appendChild(element);
						YAHOO.util.Event.onContentReady(htmlid + "-picker", function() {
							var picker = Alfresco.util.ComponentManager.get(htmlid + "-picker");
							picker.subscribe("itemsSelected", function(args) {
								picker.hide();
								_onSelectAddToCaseCommandItems(opts, args);
							}, this, true);
							_addCommandPickers[opts.objectId] = picker;
						});
					},
					scope: this
				},
				failureMessage: Alfresco.util.message("message.cannotloaddialog"),
				scope: this,
				execScripts: true
			});
		}
	};

	var _onSelectAddAssocsCommandItems = function(opts, items) {
		if (items && items.length > 0) {
			var nodeRefs = "";
			for (var i = 0; i < items.length; i++) {
				nodeRefs += items[i].nodeRef;
				if (i < items.length - 1)
					nodeRefs += ',';
			}
			var addAssocSript = "citeck/add-assocs";
			if (opts.addAssocScript) {
				addAssocSript = opts.addAssocScript;
			}
			Alfresco.util.Ajax.request({
				//url: Alfresco.constants.PROXY_URI + "citeck/add-assocs",
				url: Alfresco.constants.PROXY_URI + addAssocSript,
				dataObj: {
					sourceRef: opts.nodeRef,
					targetRef: nodeRefs,
					assocTypes: opts.assocType
				},
				method: Alfresco.util.Ajax.POST,
				successCallback: {
					fn: function (response) {
						YAHOO.Bubbling.fire("metadataRefresh");
					},
					scope: this
				},
				failureMessage: Alfresco.util.message("message.cannot.add.assocs.to.node"),
				scope: this
			});
		}
	};

	var _onSelectAddProductOrServiceAssocsCommandItems = function(opts, items) {
		if (items && items.length > 0) {
			var nodeRefs = "";
			for (var i = 0; i < items.length; i++) {
				nodeRefs += items[i].nodeRef;
				if (i < items.length - 1)
					nodeRefs += ',';
			}
			var addAssocSript = "citeck/add-assocs";
			if (opts.addAssocScript) {
				addAssocSript = opts.addAssocScript;
			}
			Alfresco.util.Ajax.jsonPost({
				//url: Alfresco.constants.PROXY_URI + "citeck/add-assocs",
				url: Alfresco.constants.PROXY_URI + addAssocSript,
				dataObj: {
					sourceRef: opts.nodeRef,
					targetRef: nodeRefs,
					assocTypes: opts.assocType
				},
				method: Alfresco.util.Ajax.POST,
				successCallback: {
					fn: function (response) {
						YAHOO.Bubbling.fire("metadataRefresh");
					},
					scope: this
				},
				failureMessage: Alfresco.util.message("message.cannot.add.assocs.to.node"),
				scope: this
			});
		}
	};

	Citeck.widget.ButtonPanel.Commands.onPanelButtonDeleteAllAssoc = function (options) {

		var headerMsg = Alfresco.util.message(options.header);
		
		Alfresco.util.PopupManager.displayPrompt({
			title: Alfresco.util.message("association.assoc_deleteAll.title", this, headerMsg),
			text: Alfresco.util.message("association.assoc_deleteAll.message", this, headerMsg),
			noEscape: true,
			buttons: [
				{
					text: Alfresco.util.message("button.delete"),
					handler: function dlA_onActionDelete_delete() {
						Alfresco.util.PopupManager.displayMessage({
							text: Alfresco.util.message("association.assoc.deleteAll.inProgress"),
							displayTime: 5
						});
						deleteAssocs();
						this.destroy();
					}
				},
				{
					text: Alfresco.util.message("button.cancel"),
					handler: function dlA_onActionDelete_cancel() {
						this.destroy();
					},
					isDefault: true
				}]
		});


		var opts = _parseAddCommandOptions(options);
		var sourceRef = opts.nodeRef,
			assocTypes = options.assocType,
			deleteChildren = options.deleteChildren === "true",
			nodes = "",
			url = Alfresco.constants.PROXY_URI + "citeck/assocs?nodeRef=" + sourceRef + "&assocTypes=" + assocTypes,
			deleteNodesUrl = "/citeck/node?nodeRef=";

		function deleteAssocs() {
			YAHOO.util.Connect.asyncRequest(
				'GET',
				url, {
					success: function (response) {
						if (response.responseText) {
							var data = eval('({' + response.responseText + '})'),
								dataToDelete = deleteChildren ? data.assocs[0].children : data.assocs[0].targets;

							if (dataToDelete.length > 0) {
								var timeDelay = dataToDelete.length * 800;
								for (var i = 0; i < dataToDelete.length; i++) {
                                    nodes = dataToDelete[i]['nodeRef'];
									deleteRequest(nodes);
									sleep(600);
								}
								_.delay(function () {
									YAHOO.Bubbling.fire("metadataRefresh");
								}, timeDelay);
							} else {
								Alfresco.util.PopupManager.displayMessage({
									text: Alfresco.util.message("association.assoc.deleteAll.nothingToDelete", "", headerMsg)
								});
							}
						}
					}
				});
		}

		function sleep(ms) {
			ms += new Date().getTime();
			while (new Date() < ms) {}
		}

		function deleteRequest(nodeRef) {
			Alfresco.util.Ajax.request({
				url: Alfresco.constants.PROXY_URI + deleteNodesUrl + nodeRef,
				method: Alfresco.util.Ajax.DELETE,
				successCallback: {
					fn: function () {

					},
					scope: this
				},
				failureMessage: Alfresco.util.message("message.delete.failure", nodes),
				scope: this
			});
		}
	};

	Citeck.widget.ButtonPanel.Commands.onPanelButtonAssocsAdd = function(options) {
		var opts = _parseAddCommandOptions(options),
			picker = _addCommandPickers[opts.objectId];
		opts.assocType = options.assocType;

		if (!opts.nodeRef) {
			alert("Mandatory option node reference - 'nodeRef' is not specified");
			return;
		}

		if (!opts.assocType) {
			alert("Mandatory option 'assocType' is not specified");
			return;
		}

		if (picker) {
			picker.setSelectedItems([]);
			picker.show();
		} else {
			var htmlid = Alfresco.util.generateDomId();
			// load dialog from web-script
			Alfresco.util.Ajax.request({
				url: Alfresco.constants.URL_SERVICECONTEXT + "citeck/components/dynamic-tree-picker",
				dataObj: {
					htmlid: htmlid,
					itemType: opts.itemType,
					itemKey: opts.itemKey,
					itemTitle: opts.itemTitle,
					itemURL: opts.itemURL,
					itemURLresults: opts.itemURLresults,
					searchURL: opts.searchURL,
					searchURLresults: opts.searchURLresults,
					rootURL: opts.rootURL,
					rootURLresults: opts.rootURLresults,

					// additional options
					preloadSearchQuery: options.preloadSearchQuery ? options.preloadSearchQuery : null,
					preloadSearchQueryEveryTime: options.preloadSearchQueryEveryTime ? options.preloadSearchQueryEveryTime : null
				},
				successCallback: {
					fn: function (response) {
						var element = document.createElement("DIV");
						element.innerHTML = response.serverResponse.responseText;
						YAHOO.util.Dom.addClass(element, "inner-panel-button-dialog");
						document.getElementsByTagName('body')[0].appendChild(element);
						YAHOO.util.Event.onContentReady(htmlid + "-picker", function() {
							var picker = Alfresco.util.ComponentManager.get(htmlid + "-picker");
							picker.subscribe("itemsSelected", function(args) {
								picker.hide();
								_onSelectAddAssocsCommandItems(opts, args);
							}, this, true);
							_addCommandPickers[opts.objectId] = picker;
						});
					},
					scope: this
				},
				failureMessage: Alfresco.util.message("message.cannotloaddialog"),
				scope: this,
				execScripts: true
			});
		}
	};

	var _onSelectAddCommandItems = function(opts, items) {
		if (opts && items && items.length > 0) {
			var nodeRefs = "";
			for (var i = 0; i < items.length; i++) {
				nodeRefs += items[i].nodeRef;
				if (i < items.length - 1)
					nodeRefs += ',';
			}
			alert('parent node ref=' + opts.nodeRef + '; added node refs=' + nodeRefs);
			// TODO(Rulan): Here should be common way to add selected object into caller object.
		}
	};

	Citeck.widget.ButtonPanel.Commands.onPanelButtonAdd = function(options) {
		var opts = _parseAddCommandOptions(options),
			picker = _addCommandPickers[opts.objectId];
		if (picker) {
			picker.setSelectedItems([]);
			picker.show();
		}
		else {
			var htmlid = Alfresco.util.generateDomId();
			// load dialog from web-script
			Alfresco.util.Ajax.request({
				url: Alfresco.constants.URL_SERVICECONTEXT + "citeck/components/dynamic-tree-picker",
				dataObj: {
					htmlid: htmlid,
					itemType: opts.itemType,
					itemKey: opts.itemKey,
					itemTitle: opts.itemTitle,
					itemURL: opts.itemURL,
					itemURLresults: opts.itemURLresults,
					searchURL: opts.searchURL,
					searchURLresults: opts.searchURLresults,
					rootURL: opts.rootURL,
					rootURLresults: opts.rootURLresults
				},
				successCallback: {
					fn: function (response) {
						var element = document.createElement("DIV");
						element.innerHTML = response.serverResponse.responseText;
						YAHOO.util.Dom.addClass(element, "inner-panel-button-dialog");
						document.getElementsByTagName('body')[0].appendChild(element);
						YAHOO.util.Event.onContentReady(htmlid + "-picker", function() {
							var picker = Alfresco.util.ComponentManager.get(htmlid + "-picker");
							picker.subscribe("itemsSelected", function(args) {
								picker.hide();
								if (opts.onPanelButtonAdd && typeof opts.onPanelButtonAdd === 'function')
									opts.onPanelButtonAdd(opts, args);
								else
									_onSelectAddCommandItems(opts, args);
							}, this, true);
							_addCommandPickers[opts.objectId] = picker;
						});
					},
					scope: this
				},
				failureMessage: Alfresco.util.message("message.cannotloaddialog"),
				scope: this,
				execScripts: true
			});
		}
	};

	Citeck.widget.ButtonPanel.Commands.onPanelButtonAddRole = function(options) {
		var opts = _parseAddCommandOptions(options),
			picker = _addCommandPickers[opts.objectId];
		opts.assocType = "icaseRole:referenceRoleAssoc";
		opts.addAssocScript = "/citeck/add-case-roles";


		if (!opts.nodeRef) {
			alert("Mandatory option node reference - 'nodeRef' is not specified");
			return;
		}

		if (!opts.assocType) {
			alert("Mandatory option 'assocType' is not specified");
			return;
		}

		if (picker) {
			picker.setSelectedItems([]);
			picker.show();
		} else {
			var htmlid = Alfresco.util.generateDomId();
			// load dialog from web-script
			Alfresco.util.Ajax.request({
				url: Alfresco.constants.URL_SERVICECONTEXT + "citeck/components/dynamic-tree-picker",
				dataObj: {
					htmlid: htmlid,
					itemType: opts.itemType,
					itemKey: opts.itemKey,
					itemTitle: opts.itemTitle,
					itemURL: opts.itemURL,
					itemURLresults: opts.itemURLresults,
					searchURL: opts.searchURL,
					searchURLresults: opts.searchURLresults,
					rootURL: opts.rootURL,
					rootURLresults: opts.rootURLresults,

					// additional options
					preloadSearchQuery: options.preloadSearchQuery ? options.preloadSearchQuery : null,
					preloadSearchQueryEveryTime: options.preloadSearchQueryEveryTime ? options.preloadSearchQueryEveryTime : null
				},
				successCallback: {
					fn: function (response) {
						var element = document.createElement("DIV");
						element.innerHTML = response.serverResponse.responseText;
						YAHOO.util.Dom.addClass(element, "inner-panel-button-dialog");
						document.getElementsByTagName('body')[0].appendChild(element);
						YAHOO.util.Event.onContentReady(htmlid + "-picker", function() {
							var picker = Alfresco.util.ComponentManager.get(htmlid + "-picker");
							picker.subscribe("itemsSelected", function(args) {
								picker.hide();
								_onSelectAddAssocsCommandItems(opts, args);
							}, this, true);
							_addCommandPickers[opts.objectId] = picker;
						});
					},
					scope: this
				},
				failureMessage: Alfresco.util.message("message.cannotloaddialog"),
				scope: this,
				execScripts: true
			});
		}
	};

	Citeck.widget.ButtonPanel.Commands.onPanelButtonAddProductOrService = function(options) {
		var opts = _parseAddCommandOptions(options),
			picker = _addCommandPickers[opts.objectId];
		opts.assocType = "pas:containsProductsAndServices";
		opts.addAssocScript = "/citeck/add-pas-assocs";

		if (!opts.nodeRef) {
			alert("Mandatory option node reference - 'nodeRef' is not specified");
			return;
		}

		if (!opts.assocType) {
			alert("Mandatory option 'assocType' is not specified");
			return;
		}

		if (picker) {
			picker.setSelectedItems([]);
			picker.show();
		} else {
			var htmlid = Alfresco.util.generateDomId();
			// load dialog from web-script
			Alfresco.util.Ajax.request({
				url: Alfresco.constants.URL_SERVICECONTEXT + "citeck/components/dynamic-tree-picker",
				dataObj: {
					htmlid: htmlid,
					itemType: opts.itemType,
					itemKey: opts.itemKey,
					itemTitle: opts.itemTitle,
					itemURL: opts.itemURL,
					itemURLresults: opts.itemURLresults,
					searchURL: opts.searchURL,
					searchURLresults: opts.searchURLresults,
					rootURL: opts.rootURL,
					rootURLresults: opts.rootURLresults,

					// additional options
					preloadSearchQuery: options.preloadSearchQuery ? options.preloadSearchQuery : null,
					preloadSearchQueryEveryTime: options.preloadSearchQueryEveryTime ? options.preloadSearchQueryEveryTime : null
				},
				successCallback: {
					fn: function (response) {
						var element = document.createElement("DIV");
						element.innerHTML = response.serverResponse.responseText;
						YAHOO.util.Dom.addClass(element, "inner-panel-button-dialog");
						document.getElementsByTagName('body')[0].appendChild(element);
						YAHOO.util.Event.onContentReady(htmlid + "-picker", function() {
							var picker = Alfresco.util.ComponentManager.get(htmlid + "-picker");
							picker.subscribe("itemsSelected", function(args) {
								picker.hide();
								_onSelectAddProductOrServiceAssocsCommandItems(opts, args);
							}, this, true);
							_addCommandPickers[opts.objectId] = picker;
						});
					},
					scope: this
				},
				failureMessage: Alfresco.util.message("message.cannotloaddialog"),
				scope: this,
				execScripts: true
			});
		}
	};

})();
