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
/**
 * ConsoleOrgstruct tool component.
 *
 * @namespace Alfresco
 * @class Alfresco.component.ConsoleOrgstruct
 */
define([
    'citeck/components/dynamic-tree/hierarchy-model',
    'citeck/components/dynamic-tree/dynamic-tree',
    'citeck/components/dynamic-tree/dynamic-toolbar',
    'citeck/components/orgstruct/form-dialogs'
], function() {

    Citeck = typeof Citeck != "undefined" ? Citeck : {};

    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event,
        Element = YAHOO.util.Element;
    var $buttonSubscribe = Citeck.widget.HasButtons.subscribe;

    /**
     * ConsoleOrgstruct constructor.
     *
     * @param {String} htmlId The HTML id �of the parent element
     * @return {Alfresco.component.ConsoleOrgstruct} The new ConsoleOrgstruct instance
     * @constructor
     */
    Alfresco.component.ConsoleOrgstruct = function(htmlId)
    {
        this.name = "Alfresco.component.ConsoleOrgstruct";
        Alfresco.component.ConsoleOrgstruct.superclass.constructor.call(this, "Alfresco.component.ConsoleOrgstruct", htmlId, ["button", "container", "resize"]);

        this.state = {};

        this.setOptions({
            buttons: [
                {
                    id: "createGroup",
                    event: "createItem",
                    eventArgs: {
                        itemId: "cm:authorityContainer",
                        formId: "group",
                    },
                },
                {
                    id: "createBranch",
                    event: "createItem",
                    eventArgs: {
                        itemId: "cm:authorityContainer",
                        formId: "branch",
                    },
                },
                {
                    id: "createRole",
                    event: "createItem",
                    eventArgs: {
                        itemId: "cm:authorityContainer",
                        formId: "role",
                    },
                },
                {
                    id: "createBranchType",
                    event: "createItem",
                    eventArgs: {
                        itemId: "org:simpleBranchType",
                    },
                },
                {
                    id: "createRoleType",
                    event: "createItem",
                    eventArgs: {
                        itemId: "org:simpleRoleType",
                    },
                },
                {
                    id: "editItem",
                    event: "editItem",
                    eventArgs: {
                        type: "orgstruct",
                    },
                },
                {
                    id: "convertToBranch",
                    event: "editItem",
                    eventArgs: {
                        type: "branch",
                    },
                },
                {
                    id: "convertToRole",
                    event: "editItem",
                    eventArgs: {
                        type: "role",
                    },
                },
                {
                    id: "editItemInplaced",
                    event: "editItemInplaced",
                    eventArgs: {
                        type: "orgstruct",
                        mode: "edit"
                    },
                },
                {
                    id: "createUser",
                    event: "createUser",
                    eventArgs: {
                        itemId: "cm:person",
                        // type: "person",
                        // formId: "person",
                        mode: "create",
                    },
                }
            ]
        });

    };

    YAHOO.extend(Alfresco.component.ConsoleOrgstruct, Alfresco.component.Base, {

        /**
         * Fired by YUI when parent element is available for scripting.
         * Initial History Manager event registration
         *
         * @method onReady
         */
        onReady: function()
        {
            this.filterMap = this._prepareMap(this.options.filters, "name");

            this.model = new Citeck.util.HierarchyModel(this.name);
            this.widgets.tree = new Citeck.widget.DynamicTree(this.id + "-tree", this.model, this.name);
            this.widgets.list = new Citeck.widget.DynamicTree(this.id + "-list", this.model, this.name);
            this.widgets.treeToolbar = new Citeck.widget.DynamicToolbar(this.id + "-tree-toolbar", this.model, this.name);
            this.widgets.listToolbar = new Citeck.widget.DynamicToolbar(this.id + "-list-toolbar", this.model, this.name);
            this.widgets.resizer = new YAHOO.util.Resize(this.id + "-tree-resizer", {
                handles: ["r"],
            });

            // set widgets configs:
            YAHOO.Bubbling.fire("registerButtons", this.options.buttons);
            YAHOO.Bubbling.on("clearViewJunk", this._clearViewJunk.bind(this));

            // listen to tree events:
            this.widgets.tree.subscribe("clickEvent", this.onTreeItemSelected, this, true);
            this.widgets.list.subscribe("clickEvent", this.onListItemSelected, this, true);
            this.widgets.tree.subscribe("viewItem", this.onViewItem, this, true);

            // initialize dialogs:
            this.widgets.selectUserDialog = new Citeck.widget.SelectUserDialog(this.id + "-peoplepicker");
            this.widgets.selectGroupDialog = new Citeck.widget.SelectGroupDialog(this.id + "-grouppicker");
            this.widgets.deleteItemDialog = new Citeck.widget.DeleteItemDialog(this.id + "-delete-dialog");

            // subscribe on button events:
            var ids = [
                this.widgets.tree.id, this.widgets.listToolbar.id,
                this.widgets.list.id, this.widgets.treeToolbar.id
            ];
            $buttonSubscribe("createItem", this.onCreateItem, this, ids);
            $buttonSubscribe("addGroup", this.onAddGroup, this, ids);
            $buttonSubscribe("addUser", this.onAddUser, this, ids);
            $buttonSubscribe("addDeputy", this.onAddDeputy, this, ids);
            $buttonSubscribe("addAssistant", this.onAddAssistant, this, ids);
            $buttonSubscribe("editItem", this.onEditItem, this, ids);

            $buttonSubscribe("editItemInplaced", this.onEditItemInplaced, this, ids);

            $buttonSubscribe("viewItem", this.onViewItem, this, ids);
            $buttonSubscribe("deleteItem", this.onDeleteItem, this, ids);
            $buttonSubscribe("search", this.onSearch, this, ids);
            $buttonSubscribe("resetSearch", this.onResetSearch, this, ids);
            $buttonSubscribe("convertToGroup", this.onConvertToGroup, this, ids);
            $buttonSubscribe("convertToBranch", this.onEditItem, this, ids);
            $buttonSubscribe("convertToRole", this.onEditItem, this, ids);

            $buttonSubscribe("createUser", this.onCreateUser, this, ids);

            // subscribe on search "Enter"
            this.searchKeyListener = new YAHOO.util.KeyListener(this.id + "-search-input", { keys: 13 }, {
                fn: this.onSearch,
                scope: this,
                correctScope: true,
            }, "keydown");
            this.searchKeyListener.enable();

            // subscribe on dialog events:
            this.widgets.selectUserDialog.subscribe("itemSelected", this.onUserSelectedForAdd, this, true);
            this.widgets.selectGroupDialog.subscribe("itemSelected", this.onGroupSelectedForAdd, this, true);
            this.widgets.deleteItemDialog.subscribe("deleteConfirmed", this.onDeleteItemConfirmed, this, true);

            // initiate work (set default filter):
            this._initFilters();
            this._updateFilter();

            var self = this;
            $(document).bind("DOMNodeRemoved", function(e) {
                if (e.target && e.target.classList && !!~Array.prototype.indexOf.call(e.target.classList, 'user-profile-view-inner-container')) {
                    setTimeout(function () {
                        self._clearViewJunk.call(self);
                    });
                }
            });
        },

        /**
         * Called on destroy.
         * Unsubscribe from button events.
         */
        destroy: function() {
            $buttonUnsubscribe(this);
            Alfresco.component.ConsoleOrgstruct.superclass.destroy.call(this);
        },

        // convert array to map, based on key property
        _prepareMap: function(array, keyProp) {
            var map = {};
            for(var i in array) {
                if(!array.hasOwnProperty(i)) continue;
                map[array[i][keyProp]] = array[i];
            }
            return map;
        },

        // initialize filter selector:
        _initFilters: function() {

            // render menu:
            var menuElId = this.id + "-filter-select",
                menuEl = Dom.get(menuElId);

            for(var i in this.options.filters) {
                if(!this.options.filters.hasOwnProperty(i)) continue;
                var filter = this.options.filters[i],
                    option = document.createElement('OPTION');
                option.text = this.msg("filter." + filter.name + ".label");
                option.value = filter.name;
                menuEl.options.add(option);
            }

            this.widgets.filterButton = new YAHOO.widget.Button(this.id + "-filter", {
                type: "menu",
                menu: this.id + "-filter-select",
                label: this.msg("filter." + this.options.currentFilter + ".label"),
            });
            this.widgets.filterButton.subscribe("selectedMenuItemChange", this.onFilterChange, this, true);

        },

        // save created views ids
        _saveJunkId: function (formId) {
            this.widgets.consts = this.widgets.consts || {};
            this.widgets.consts.junkIdsArray = this.widgets.consts.junkIdsArray || [];
            this.widgets.consts.junkIdsArray.push(formId);
        },

        // clear junk created by onViewItem
        _clearViewJunk: function () {
            var killRuntime = this.readKillRuntimeSign();

            // unsubscribe
            try {YAHOO.Bubbling.unsubscribe("node-view-submit", onSubmit);} catch(e) {
                if (e.message !== 'onSubmit is not defined') {
                    console.log('err on unsubscribe("node-view-submit")', e.message)
                }
            }
            try {YAHOO.Bubbling.unsubscribe("node-view-cancel", onCancel);} catch(e) {
                if (e.message !== 'onCancel is not defined') {
                    console.log('err on unsubscribe("node-view-cancel")', e.message)
                }
            }

            // show toolbar
            $('.orgstruct-console .selected-item-details .toolbar').show();
            $('.orgstruct-console .selected-item-details .dynamic-tree-list>.ygtvitem').show();

            // get the right section - block were we will show the views
            var showArea = $('#' + this.widgets.list.id);

            // terminate runtime
            var ecosForm = showArea[0].querySelector('.ecos-form'),
                ecosFormId = ecosForm ? ecosForm.id : this.widgets.viewItem ? this.widgets.viewItem.containerId ? this.widgets.viewItem.containerId + '-form' : '' : '';
            if (killRuntime && ecosFormId) {
                try {
                    Alfresco.util.ComponentManager.get(ecosFormId).runtime.terminate();
                    if (this.widgets.viewItem) {
                        delete this.widgets.viewItem;
                        this.widgets.viewItem = null;
                    }
                } catch (e) {
                    console.log('consoleOrgstruct._clearViewJunk :: ERROR on runtime.terminate() :', e.message);
                }
            }

            // remove previously created junk
            showArea.children('.user-profile-view-inner-container').each(function () {
                $("[id^='"+ $(this).attr('id')+ "']").remove();
            });

            var idx;
            if (this.widgets.consts && this.widgets.consts.junkIdsArray && this.widgets.consts.junkIdsArray.length) {
                var tempArr = this.widgets.consts.junkIdsArray.slice();
                this.widgets.consts.junkIdsArray.forEach(function (item, i, arr) {
                    if ($("[id^='" + item + "']").remove().length) {
                        idx = tempArr.indexOf(item);
                        tempArr.splice(idx, 1);
                    }
                });
                this.widgets.consts.junkIdsArray = tempArr;
            }

            // remove previous node views
            showArea.children('.user-profile-view-inner-container').remove();
        },

        /**
         * Event handler - item was selected in tree (left-pane).
         * Updates list and list toolbar.
         */
        onTreeItemSelected: function(args) {
            var node = args.node;
            var item = node.data;
            var parent = node.parent.data;

            // clear junk created by onViewItem
            this._clearViewJunk();

            // bind widgets to model
            this.widgets.list.setContext(item, parent);
            this.widgets.listToolbar.setContext(item, parent);

            // update model
            this.model.updateChildren(item);
            return false; // do not toggle
        },

        /**
         * Event handler - item was selected in list (right-pane).
         * Do nothing.
         */
        onListItemSelected: function(args) {
            var node = args.node;
            var item = node.data;
            var parent = node.parent.data;

            if (item.item.authorityType === 'USER') {
                // clear junc created by onViewItem
                this._clearViewJunk();

                // bind widgets to model
                this.widgets.list.setContext(item, parent);
                this.widgets.listToolbar.setContext(item, parent);

                this.onViewItem(args);
            }
            return false; // do not toggle
        },

        /**
         * Event handler - filter was changed by filter button.
         * Updates everyting.
         */
        onFilterChange: function (event) {
            var	oMenuItem = event.newValue;
            if (oMenuItem) {
                this.options.currentFilter = oMenuItem.value;
                this._updateFilter();
            }
        },

        // update filter
        // this function initializes model and all widgets by the new selected configuration (filter)
        _updateFilter: function() {
            this.config = this.filterMap[this.options.currentFilter];
            var configName = this.msg("filter." + this.options.currentFilter + ".label");
            this.widgets.filterButton.set("label", configName);
            // set widgets configuration:
            this.model.setConfig(this.config.model);
            this.model.createSpecialItem({
                title: configName
            }, {
                name: "root",
                keys: ["root"]
            });
            this.model.createSpecialItem({
                title: configName
            }, {
                name: "search",
                keys: ["search"],
            });
            this.widgets.tree.setConfig(this.config.tree);
            this.widgets.list.setConfig(this.config.list);
            this.widgets.treeToolbar.setConfig(this.config.toolbar);
            this.widgets.listToolbar.setConfig(this.config.toolbar);

            this._setRootItem("root");
        },

        /**
         * Event handler - create-item button was clicked.
         * Show create form.
         */
        onCreateItem: function(args) {
            var itemId = args.itemId;
            if (itemId == 'cm:authorityContainer') {
                var self = this,
                    formId = args.formId,
                    parentId = args.item,
                    parent = this.model.getItem(parentId),
                    destination = this.model.getItemProperty(parent, this.config.forms.destination, true),
                    destinationAssoc = 'cm:member',
                    header = this.msg('panel.create.' + (formId || 'default') + '.header'),
                    mode = 'create';

                Alfresco.util.Ajax.jsonGet({
                    url: Alfresco.constants.PROXY_URI + 'citeck/authority/getGroupNodeRef',
                    dataObj: {
                        groupFullName: destination
                    },
                    successCallback: {
                        fn: function(response) {
                            var destinationNodeRef = response.json.nodeRef;
                            if (destinationNodeRef) {
                                self.widgets.createItemDialog = new Citeck.forms.dialog(itemId, formId, function () {
                                    self.onItemCreated(parent);
                                }, {
                                    width: '800px',
                                    mode: mode,
                                    destination: destinationNodeRef,
                                    destinationAssoc: destinationAssoc,
                                    title: header
                                });
                            }
                        },
                        scope: this
                    },
                    failureMessage: 'Group nodeRef receive failed!'
                });
            } else {
                var formId = args.formId,
                    parentId = args.item,
                    parent = this.model.getItem(parentId),
                    htmlid = this.id + "-form-" + Alfresco.util.generateDomId(),
                    destination = this.model.getItemProperty(parent, this.config.forms.destination, true);
                this.widgets.createItemDialog = new Citeck.widget.CreateFormDialog(htmlid, itemId, formId, destination, null, parent, this.name);
                this.widgets.createItemDialog.setOptions(this.config.forms);
                this.widgets.createItemDialog.show();
                this.widgets.createItemDialog.subscribe("itemCreated", this.onItemCreated, this, true);
            }
        },

        /**
         * Event handler - edit-item button was clicked.
         * Show edit form
         */
        onEditItem: function(args) {
            var self = this;
            var mode = args.mode;
            var itemId = args.item;
            var item = this.model.getItem(itemId);
            var type = args.type;
            var formId = (type && type != 'orgstruct' ) ? type : item.groupType;

            // clear junc created by onViewItem
            this._clearViewJunk();

            itemId = this.model.getItemProperty(item, this.config.forms.nodeId, true);
            self.widgets.editItemDialog = new Citeck.forms.dialog(itemId, formId, function () {
                    self.onItemEdited(item);
                }, {
                width: "800px",
                mode: mode
            });
        },

        /**
         * Event handler - tree item was clicked.
         * Show view form
         */
        onViewItem: function(args) {
            var formId, itemId, item;

            if (args.node) {
                formId = 'orgstruct';
                itemId = args.node.data._item_name_;
            } else {
                formId = args.type;
                itemId = args.item;
            }
            item = this.model.getItem(itemId);

            // clear junc created by onViewItem
            this._clearViewJunk();

            itemId = this.model.getItemProperty(item, this.config.forms.nodeId, true);
            this.widgets.viewItem = new Citeck.forms.showViewInplaced(itemId, formId, function () {}, {listId: this.widgets.list.id/*, mode: 'edit'*/});
            this.widgets.viewItem.containerId && this._saveJunkId(this.widgets.viewItem.containerId);
            YAHOO.Bubbling.on("showViewInplacedDone", this.markKillRuntime.bind(this));
        },

        onCreateUser: function (args) {
            var formId, itemId, item;

            if (args.node) {
                formId = 'orgstruct';
                item = args.node.data._item_name_;
            } else {
                formId = args.type;
                item = args.item;
                itemId = args.itemId;
            }
            item = this.model.getItem(item);

            // clear junc created by onViewItem
            this._clearViewJunk();

            var destItemId = this.model.getItemProperty(item, this.config.forms.nodeId, true);
            var self = this,
                parentId = args.item,
                parent = this.model.getItem(parentId);
            this.widgets.viewItem = new Citeck.forms.showViewInplaced(itemId, formId, function () { self.onItemCreated(parent); },
                {listId: this.widgets.list.id, destination: destItemId, mode: 'create'});
            this.widgets.viewItem.containerId && this._saveJunkId(this.widgets.viewItem.containerId);
            YAHOO.Bubbling.on("showViewInplacedDone", this.markKillRuntime.bind(this));
        },

        /**
         * Event handler - tree item was clicked.
         * Show EditItemInplaced form
         */
        onEditItemInplaced: function(args) {
            var mode, formId, itemId, item;

            mode = args.mode;
            if (args.node) {
                formId = 'orgstruct';
                itemId = args.node.data._item_name_;
            } else {
                formId = args.type;
                itemId = args.item;
            }
            item = this.model.getItem(itemId);

            // clear junc created by onViewItem
            this._clearViewJunk();

            itemId = this.model.getItemProperty(item, this.config.forms.nodeId, true);
            this.widgets.viewItem = new Citeck.forms.showViewInplaced(itemId, formId, function () {}, {listId: this.widgets.list.id, mode: mode}); // mode == edit
            this.widgets.viewItem.containerId && this._saveJunkId(this.widgets.viewItem.containerId);
            YAHOO.Bubbling.on("showViewInplacedDone", this.markKillRuntime.bind(this));
        },

        markKillRuntime: function () {
            this.widgets = this.widgets || {};
            this.widgets.consts = this.widgets.consts || {};
            this.widgets.consts.KillRuntime = true;
        },

        readKillRuntimeSign: function () {
            this.widgets = this.widgets || {};
            this.widgets.consts = this.widgets.consts || {};
            var killRuntime = this.widgets.consts.KillRuntime || false;
            this.widgets.consts.KillRuntime = false;
            return killRuntime;
        },

        /**
         * Event handler - convert-to-group button was clicked.
         * Send query to convert to group.
         */
        onConvertToGroup: function(args) {
            var itemId = args.item,
                item = this.model.getItem(itemId);
            Alfresco.util.Ajax.jsonPost({
                url: Alfresco.constants.PROXY_URI + "api/orgstruct/group",
                dataObj: {
                    shortName: item.shortName,
                    fullName: item.fullName,
                    groupType: "group"
                },
                successCallback: {
                    scope: this,
                    fn: function() {
                        this.onItemEdited(item);
                    }
                }
            });
        },

        /**
         * Event handler - add-group button was clicked.
         * Show select-group dialog.
         */
        onAddGroup: function(args) {
            var itemId = args.item,
                item = this.model.getItem(itemId);
            this.state.dialogParentItem = item;
            this.widgets.selectGroupDialog.show();
        },

        /**
         * Event handler - add-user button was clicked.
         * Show select-user dialog.
         */
        onAddUser: function(args) {
            var itemId = args.item,
                item = this.model.getItem(itemId);
            this.state.dialogParentItem = item;
            this.widgets.selectUserDialog.show();
        },

        /**
         * Event handler - add-assistant-user button was clicked.
         * Show select-user dialog.
         */
        onAddAssistant: function (args) {
            var itemId = args.item,
                item = this.model.getItem(itemId);
            item.isAssistant = true;
            this.state.dialogParentItem = item;
            this.widgets.selectUserDialog.show();
        },

        /**
         * Event handler - add-assistant-user button was clicked.
         * Show select-user dialog.
         */
        onAddDeputy: function (args) {
            var itemId = args.item,
                item = this.model.getItem(itemId);
            item.isAssistant = false;
            this.state.dialogParentItem = item;
            this.widgets.selectUserDialog.show();
        },

        /**
         * Event handler - delete-item button was clicked.
         * Show confirm-delete dialog.
         */
        onDeleteItem: function(args) {
            var itemId = args.item,
                parentId = args.from;
            var item = this.state.dialogChildItem = this.model.getItem(itemId);
            var parent = this.state.dialogParentItem = this.model.getItem(parentId);
            var itemText = this.model.getItemTitle(item);
            var parentText = this.model.getItemTitle(parent);
            Dom.get(this.id + "-delete-dialog-message").innerHTML = this.msg("message.delete-confirm", itemText, parentText);
            this.widgets.deleteItemDialog.show();
        },

        onSearch: function(args) {
            // get query string:
            var input = Dom.get(this.id + "-search-input");
            var query = input.value;
            if(query) {
                // update model
                var search = this.model.getItem("search");
                search.query = query;
                this._setRootItem(search);
            } else {
                this.widgets.feedbackMessage = Alfresco.util.PopupManager.displayMessage({
                    text: this.msg("message.search-query-empty"),
                });
                input.focus();
            }
        },

        onResetSearch: function(args) {
            this._setRootItem("root");
        },

        _setRootItem: function(item) {
            // update model
            var root = this.model.getItem(item);
            this.model.updateChildren(root);

            // update widgets
            this.widgets.tree.setContext(root, "none");
            this.widgets.treeToolbar.setContext(root, "none");
            this.widgets.list.setContext("none", "none");
            this.widgets.listToolbar.setContext("none", "none");

        },

        /**
         * Event handler - new child was created in item.
         * Initiates model update.
         */
        onItemCreated: function(parent) {
            this.model.updateChildren(parent);
        },

        /**
         * Event handler - item was edited (updated).
         * Initiates model update.
         */
        onItemEdited: function(item) {
            this.model.updateItem(item);
        },

        /**
         * Event handler - user was selected in select-user dialog.
         * Initiates model update.
         */
        onUserSelectedForAdd: function(args) {
            var parent = this.state.dialogParentItem,
                childName = args.userName,
                child = {
                    authorityType: "USER",
                    shortName: childName,
                    fullName: childName,
                    userName: childName,
                    isAssistant: parent.isAssistant == true,
                };
            this.widgets.selectUserDialog.hide();
            this.model.addItem(child, parent);
        },

        /**
         * Event handler - group was selected in select-group dialog.
         * Initiates model update.
         */
        onGroupSelectedForAdd: function(args) {
            var parent = this.state.dialogParentItem,
                childName = args.itemName,
                child = {
                    authorityType: "GROUP",
                    shortName: childName.replace(/^GROUP_/, ""),
                    fullName: childName,
                };
            this.widgets.selectGroupDialog.hide();
            this.model.addItem(child, parent);
        },

        /**
         * Event handler - delete was confirmed in confirm-delete dialog.
         * Initiates model update.
         */
        onDeleteItemConfirmed: function(args) {
            var parent = this.state.dialogParentItem,
                child  = this.state.dialogChildItem;
            this.model.deleteItem(child, parent);
        },

    });

    return Alfresco.component.ConsoleOrgstruct;
});