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
(function(){

	/**
	 * It is a renderer of actions.
	 * This renderer requests actions for specified node reference and displayed them
	 * in the specified html-element. This renderer automatically requests
	 * action informations, when registering of the information is finished.
	 * 
	 * Using: call {@code registerInfo(nodeRef, htmlElement)}
	 */

	Citeck = typeof Citeck != "undefined" ? Citeck : {};
	Citeck.widget = Citeck.widget || {};
	Citeck.format = Citeck.format || {};
	
	var Dom = YAHOO.util.Dom;
	
	/**
	 * It is a formatter of doclib actions.
	 * @param actionGroup - action group
	 * @param nodeRefField - name of field, which contains node reference in the action response
	 */
	Citeck.format.actions = function(actionGroup, nodeRefField, obj) {
		return function(elCell, oRecord, oColumn, oData) {
			var renderer = new Citeck.widget.ActionRenderer(),
				requester = new Citeck.widget.ActionRequester(),
				nodeRef = oRecord.getData(nodeRefField || "nodeRef");
			if (nodeRef) {
				Dom.addClass(elCell, "actions-cell loading");
				requester.registerInfo(actionGroup, nodeRef, function(item) {
					renderer.renderActions(item, elCell, obj);
				});
			}
		}
	}

	//formatter for injournal's actions
    Citeck.format.journalActions = function(records, nodeRefField, obj) {
        return function(elCell, oRecord) {
            var renderer = new Citeck.widget.ActionRenderer(),
                nodeRef = oRecord.getData(nodeRefField || "nodeRef");
            if (nodeRef && records) {
                Dom.addClass(elCell, "actions-cell loading");
                var node = records.find(function(item){return item.nodeRef && item.nodeRef() == nodeRef});
                if (node && node.doclib && node.doclib()) renderer.renderActions(node.doclib(), elCell, obj);
            }
        }
    }

	Citeck.widget.ActionRenderer = function() {
		if (arguments.callee._instance)
			return arguments.callee._instance;
		arguments.callee._instance = this;

		var htmlid = Alfresco.util.generateDomId();
		var element = document.createElement("DIV");
		YAHOO.util.Dom.addClass(element, "hidden");
		YAHOO.util.Dom.setAttribute(element, "id", htmlid);
		YAHOO.util.Event.onDOMReady(function() {
			document.getElementsByTagName('body')[0].appendChild(element);
		});

		Citeck.widget.ActionRenderer.superclass.constructor.call(this, "Citeck.widget.ActionRenderer", htmlid);

		this.records = {};
		this.modules = {};
		this.modules.actions = new Alfresco.module.DoclibActions();
		// We should catch all registered actions
		YAHOO.Bubbling.on("registerAction", this.onRegisterAction, this);

		// We should get all registered actions, which registered early
		YAHOO.Bubbling.fire("registerDoclibCustom", {
			scope: this,
			eventGroup: this.id
		});
	};

	YAHOO.extend(Citeck.widget.ActionRenderer, Alfresco.component.Base);
	YAHOO.lang.augmentObject(Citeck.widget.ActionRenderer.prototype, Citeck.util.ErrorManager.prototype);
	YAHOO.lang.augmentProto(Citeck.widget.ActionRenderer, Alfresco.doclib.Actions);

	YAHOO.lang.augmentObject(Citeck.widget.ActionRenderer.prototype, {
		options: {
			repositoryUrl: null
		},

		fnActionHandler: function(args, obj) {
			var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "div");
			var actionOwner = YAHOO.Bubbling.getOwnerByClassName(args[1].anchor, "action-set");
			if (owner !== null && actionOwner !== null) {
				var nodeRef = actionOwner.nodeRef;
				if (typeof this[owner.id] === "function" && nodeRef && this.records[nodeRef]) {
					args[1].stop = true;
					var record = this.records[nodeRef];
					record.actionRendererAdditionalObject = obj;
					// -----------------------------------------
					// It is a hack! Delete action requires existing of record.parent.nodeRef , but
					// when node is not container, specified property is null.
					if (record.parent && !record.parent.nodeRef)
						record.parent.nodeRef = '';
					// -----------------------------------------
					this[owner.id].call(this, record, owner);
				}
			}
			return true;
		},

		renderActions: function(item, elCell, obj) {
			var scope = this,
				actionsHtml = "",
				actionsElement = document.createElement("DIV");

			YAHOO.util.Dom.addClass(actionsElement, "action-set");
			actionsElement.nodeRef = item.nodeRef;
			item.actionParams = {};
			if (!item.jsNode)
				item.jsNode = new Alfresco.util.Node(item.node);
			this.records[item.nodeRef] = item;

			for (var j = 0; j < item.actions.length; j++)
				actionsHtml += this.renderAction(item.actions[j], item);
			if (actionsHtml) {
				actionsElement.innerHTML = YAHOO.lang.substitute(actionsHtml, this.getActionUrls(item));
				var nodes = YAHOO.util.Selector.query('.action-link', actionsElement);
				for (var i = 0; i < nodes.length; i++) {
					var node = nodes[i];
					YAHOO.util.Dom.removeClass(node, "action-link");
					YAHOO.util.Dom.addClass(node, "dynamic-table-action-link");
					YAHOO.util.Event.addListener(node, "click", function(e) {
						var args = [e, {'anchor' : e.target}];
						scope.fnActionHandler.call(scope, args, obj);
						YAHOO.util.Event.stopEvent(e);
					});
				}
			}
			elCell.appendChild(actionsElement);
			Dom.removeClass(elCell, "loading");
		},
		
		// this function is called by document-edit-properties action
		_updateDocList: function() {
			YAHOO.Bubbling.fire("metadataRefresh");
		}
		
	});

	Citeck.widget.ActionRequester = function() {
		if (arguments.callee._instance)
			return arguments.callee._instance;
		arguments.callee._instance = this;

		var htmlid = Alfresco.util.generateDomId();
		var element = document.createElement("DIV");
		YAHOO.util.Dom.addClass(element, "hidden");
		YAHOO.util.Dom.setAttribute(element, "id", htmlid);
		YAHOO.util.Event.onDOMReady(function() {
			document.getElementsByTagName('body')[0].appendChild(element);
		});

		Citeck.widget.ActionRenderer.superclass.constructor.call(this, "Citeck.widget.ActionRenderer", htmlid);
		this.MAX_QUEUE = 1000;
		this.lastActionGroup = null;
		this.lastRegisteredNumber = 0;
		this.registeredNumber = 0;
		this.registeredActions = {};
		this.timeout = null;
	};

	YAHOO.extend(Citeck.widget.ActionRequester, Alfresco.component.Base);
	YAHOO.lang.augmentObject(Citeck.widget.ActionRequester.prototype, Citeck.util.ErrorManager.prototype);

	YAHOO.lang.augmentObject(Citeck.widget.ActionRequester.prototype, {
		registerInfo: function(actionGroup, nodeRef, process) {
			if (actionGroup && nodeRef && process) {
				this.registeredNumber++;
				if (!this.registeredActions[actionGroup]) {
					this.registeredActions[actionGroup] = {};
					this.registeredActions[actionGroup][nodeRef] = [];
				}
				else if (!this.registeredActions[actionGroup][nodeRef]) {
					this.registeredActions[actionGroup][nodeRef] = [];
				}
				this.registeredActions[actionGroup][nodeRef].push(process);
				if (!this.timeout) {
					this.lastRegisteredNumber = this.registeredNumber;
					var onRegisterTimeout = function() {
						if (this.lastRegisteredNumber != this.registeredNumber &&
							this.registeredNumber < this.MAX_QUEUE) {
							this.lastRegisteredNumber = this.registeredNumber;
						}
						else {
							this.timeout.cancel();
							var queue = this.registeredActions;
							this.lastRegisteredNumber = 0;
							this.registeredNumber = 0;
							this.registeredActions = {};
							this.timeout = null;
							for (var actionGroup in queue) {
								if (queue.hasOwnProperty(actionGroup))
									this._sendRequest(actionGroup, queue[actionGroup]);
							}
						}
					};
					this.timeout = YAHOO.lang.later(500, this, onRegisterTimeout, null, true);
				}
			}
		},

		_sendRequest: function(actionGroup, queue) {
			var nodeRefsStr = "";
			for (var nodeRef in queue) {
				if (queue.hasOwnProperty(nodeRef))
					nodeRefsStr += (nodeRef + ",");
			}
			Alfresco.util.Ajax.jsonRequest({
				url: Alfresco.constants.URL_SERVICECONTEXT + "citeck/components/documentlibrary/data/explicit/type/node/alfresco/user/home?filter=all" + (actionGroup ? '&view=' + actionGroup : ''),
				method: "POST",
				dataObj: {
					nodeRefs: nodeRefsStr,
				},
				successCallback: {
					fn: function onSuccess(response) {
						if (response && response.json && response.json.items) {
							var scope = this,
								items = response.json.items;
							for (var i = 0; i < items.length; i++) {
								var item = items[i];
								if (item.nodeRef && queue[item.nodeRef]) {
									var processes = queue[item.nodeRef];
									for (var j = 0; j < processes.length; j++) {
										var process = processes[j];
										process(item);
									}
								}
							}
						}
					},
					scope: this
				},
				failureCallback: {
					fn: function onFailure(response) {
						this.onFailure(response);
					},
					scope: this
				}
			});
		}

	});

	new Citeck.widget.ActionRenderer();
	new Citeck.widget.ActionRequester();

})();
