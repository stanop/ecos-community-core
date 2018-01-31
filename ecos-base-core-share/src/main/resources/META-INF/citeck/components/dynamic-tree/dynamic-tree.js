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
     * Dynamic Tree control
     */
    Citeck.widget.DynamicTree = function(htmlid, model, name) {

        Citeck.widget.DynamicTree.superclass.constructor.call(this, name || "Citeck.widget.DynamicTree", htmlid);

        this.state = {};

        var tree = this.widgets.tree = new YAHOO.widget.TreeView(htmlid);
        this.model = model;

        // subscribe on config updates:
        YAHOO.Bubbling.on("registerButtons", this.onRegisterButtons, this);

        // subscribe on model updates:
        model.subscribe("itemUpdated", this.onItemUpdated, this, true);
        model.subscribe("childrenUpdated", this.onChildrenUpdated, this, true);
        model.subscribe("childAdded", this.onChildAdded, this, true);
        model.subscribe("childDeleted", this.onChildDeleted, this, true);

        // subscribe on tree events:
        var scope = this;
        tree.setDynamicLoad(function() {
            scope.onLoadNodeChildren.apply(scope, arguments);
        });
        tree.subscribe("clickEvent", this.onItemSelected, this, true);
        tree.singleNodeHighlight = true;

        // forward all tree events
        var events = tree.__yui_events;
        for(var event in events) {
            if(!events.hasOwnProperty(event)) continue;
            (function(event) { // create scope for event variable
                this.createEvent(event);
                tree.subscribe(event, function(args) {
                    return this.fireEvent(event, args);
                }, this, true);
            }).call(this, event);
        }

        // introduce "render" event
        var scope = this;
        this.createEvent("render");
        tree.render = function() {
            YAHOO.widget.TreeView.prototype.render.apply(this, arguments);
            scope.fireEvent("render");
        };

        // dynamic callback function - used to call specified function
        this.state.onLoadNodeChildrenCompleteCallback = null;
    };

    YAHOO.extend(Citeck.widget.DynamicTree, Alfresco.component.Base);
    YAHOO.lang.augmentObject(Citeck.widget.DynamicTree.prototype, YAHOO.util.EventProvider.prototype);
    YAHOO.lang.augmentObject(Citeck.widget.DynamicTree.prototype, Citeck.widget.HasButtons.prototype);
    YAHOO.lang.augmentObject(Citeck.widget.DynamicTree.prototype, {

        /**
         * Set tree configuration.
         * configuration can contain:
         * buttons: {
         *    key1: [ "buttonId1", "buttonId2", ... ],
         *    key2: [ "buttonId1", "butonId2", .... ],
         *    ...
         * }
         */
        setConfig: function(config) {
            this.config = config;
        },

        /**
         * Set tree context.
         * Tree context is a data item, that corresponds to tree root.
         * Note: If tree receives message, that its root item is removed from its parent,
         *       it clears data and sets context to "none", "none".
         * @param item - item that corresponds to tree root.
         * @param parent - root's parent
         */
        setContext: function(item, parent) {
            this.state.rootItem = this.model.getItem(item);
            this.state.rootParent = this.model.getItem(parent);
            with(this.widgets) {
                tree.getRoot().data.item = this.state.rootItem;
                tree.removeChildren(tree.getRoot());
                tree.render();
            }
            Dom.addClass(this.id, "loading");
        },

        /**
         * Node is highlighted on select by default.
         * @param args.node - selected node
         */
        onItemSelected: function(args) {
            var node = args.node;
            node.highlight();

            if (node.isLeaf) {
                this.createEvent("viewItem");
                this.fireEvent("viewItem", args);
            };

            return this.fireEvent("clickEvent", args); // prevent default action
        },

        /**
         * Dynamic load handler - loads data on item expand.
         * @param node - node, which children are to be loaded
         * @param onCompleteCalback - callback to be called, when data is loaded
         */
        onLoadNodeChildren: function(node, onCompleteCallback) {
            var parent = node.data.item;
            this.state.onLoadNodeChildrenCompleteCallback = this.bind(function() {
                this.state.onLoadNodeChildrenCompleteCallback = null;
                onCompleteCallback();
            });
            // update the model - it will notify us on complete.
            this.model.updateChildren(node.data.item._item_name_);
        },

        /**
         * Model update handler - item updated.
         * Re-renders all views of changed item in tree.
         * @param args.item - updated item
         */
        onItemUpdated: function(args) {
            var item = this.model.getItem(args.item);
            var nodes = this._getItemNodes(item);
            for(var i in nodes) {
                if(!nodes.hasOwnProperty(i)) continue;
                this._updateNode(item, nodes[i]);
            }
            this.widgets.tree.render();
        },

        /**
         * Model update handler - children updated.
         * Finds changes to item children - renders new children and removes old.
         * @param args.from - parent of updated items
         * @param args.items - updated items
         */
        onChildrenUpdated: function(args) {
            var tree = this.widgets.tree;
            var parent = this.model.getItem(args.from);
            var items = args.items;

            // get all parents like this:
            var parentNodes = this._getItemNodes(parent, true);

            var update = function(items, name, value) {
                if(items[name]) items[name] += value;
                else items[name] = value;
            }

            // for each parent:
            for(var i in parentNodes) {
                if(!parentNodes.hasOwnProperty(i)) continue;

                var allItems = {};

                // get rendered items:
                var childNodes = parentNodes[i].children;
                for(var j in childNodes) {
                    if(!childNodes.hasOwnProperty(j)) continue;
                    update(allItems, childNodes[j].data.item._item_name_, 1);
                }

                // get existing items:
                var itemsMap = {};
                for(var j in items) {
                    if(!items.hasOwnProperty(j)) continue;
                    var name = items[j]._item_name_;
                    itemsMap[name] = items[j];
                    update(allItems, name, 2);
                }

                // items to add:
                for(var j in items) {
                    if(!items.hasOwnProperty(j)) continue;
                    // existing, but not rendered
                    if(allItems[items[j]._item_name_] == 2) {
                        this._addChildNode(items[j], parentNodes[i]);
                    }
                }

                // items to update:
                for(var j in childNodes) {
                    if(!childNodes.hasOwnProperty(j)) continue;
                    // rendered and existing
                    var name = childNodes[j].data.item._item_name_;
                    if(allItems[name] == 3) {
                        this._updateNode(itemsMap[name], childNodes[j]);
                    }
                }

                // items to delete:
                for(var j in childNodes) {
                    if(!childNodes.hasOwnProperty(j)) continue;
                    // rendered but not existing
                    if(allItems[childNodes[j].data.item._item_name_] == 1) {
                        tree.removeNode(childNodes[j]);
                    }
                }

                this._sortNodeChildren(parentNodes[i]);
            }

            // DOG-NAIL: here we checks the tree because of there is some bug
            if (tree)
                tree.render();

            // if it was called as the result of onLoadNodeChildren
            // call its callback.
            if(this.state.onLoadNodeChildrenCompleteCallback) {
                this.state.onLoadNodeChildrenCompleteCallback.call();
            }

            // if context item was loaded, then remove "loading" class:
            if(this.state.rootItem._item_name_ == parent._item_name_) {
                Dom.removeClass(this.id, "loading");
            }
        },

        /**
         * Model update handler - child added.
         * Renders new child node in all views of its parent.
         * @param args.item - added item
         * @param args.from - item, to which it was added
         */
        onChildAdded: function(args) {
            var parent = this.model.getItem(args.from);
            var item = this.model.getItem(args.item);
            var tree = this.widgets.tree;

            // find all nodes of parent:
            var parentNodes = this._getItemNodes(parent, true);
            for(var i in parentNodes) {
                if(!parentNodes.hasOwnProperty(i)) continue;
                // add item:
                this._addChildNode(item, parentNodes[i]);
                // sort node children:
                this._sortNodeChildren(parentNodes[i]);
            }

            tree.render();
        },

        /**
         * Model update handler - child removed.
         * Removes child nodes of removed item from nodes of parent item.
         * Note: If tree receives message, that its root item is removed from its parent,
         *       it clears data and sets context to "none", "none".
         * @param args.item - deleted item
         * @param args.from - item, from which it was deleted
         */
        onChildDeleted: function(args) {
            var parent = this.model.getItem(args.from);
            var item = this.model.getItem(args.item);
            var tree = this.widgets.tree;

            // handle special case:
            // if root was deleted:
            if(this.state.rootItem._item_name_ == item._item_name_
            && this.state.rootParent._item_name_ == parent._item_name_)
            {
                // nothing is selected now
                this.setContext("none", "none");
                return;
            }

            // find all nodes of item:
            var nodes = this._getItemNodes(item);
            for(var i in nodes) {
                if(!nodes.hasOwnProperty(i)) continue;
                if(nodes[i].parent.data.item._item_name_ == parent._item_name_) {
                    tree.removeNode(nodes[i]);
                }
            }

            tree.render();
        },

        /**
         * Called on destroy.
         * Unsubscribe from button events.
         */
        destroy: function() {
            YAHOO.Bubbling.unsubscribe("registerButtons", this.onRegisterButtons, this);
            if (this.model)
                this.model.unsubscribeAll(this);
            if (this.widgets && this.widgets.tree)
                this.widgets.tree.unsubscribeAll(this);
            Citeck.widget.DynamicTree.superclass.destroy.call(this);
        },

        /**
         * Generate node config from data item.
         * @param item - item to generate config
         * @param parent - its parent item
         */
        _generateNodeConfig: function(item, parent) {
            var node = {};
            // link item into node config:
            node.item = item;
            // allow search by name:
            node._item_name_ = item._item_name_;
            var link = this.model.getItemProperty(item, this.config.link);
            var label = this.model.getItemTitle(item);
            node.html = "<span>" + this._renderLabel(item, link) + "</span>" + "<span class='item-buttons'>" + this._renderButtons(item, parent) + "</span>";
            node.label = label;
            node.labelStyle = node.contentStyle = item._item_keys_.join(" ");
            // if no children config is defined, then set leaf mode
            node.isLeaf = !this.model.hasItemChildren(item._item_name_);
            return node;
        },

        _sortNodeChildren: function(node) {
            var model = this.model,
                config = model.getItemProperty(node.data.item, this.config.sorting);
            if(!config || config.length < 1 || node.children.length < 2) return;
            node.children.sort(function(a, b) {
                for(var i = 0; i < config.length; i++) {
                    var av = model.renderTemplate(a.data.item, config[i].by);
                    var bv = model.renderTemplate(b.data.item, config[i].by);
                    if(av == bv) continue;
                    if(config[i].descend) {
                        var x = av;
                        av = bv;
                        bv = x;
                    }
                    if(av < bv) return -1;
                    if(av > bv) return 1;
                }
                return 0;
            });
        },

        /**
         * Add child item to given parent node.
         * @param item - item to add
         * @param parentNode - parent node to which new node should be added.
         */
        _addChildNode: function(item, parentNode) {
            var parent = parentNode.data.item;
            var nodeConfig = this._generateNodeConfig(item, parent);
            return new YAHOO.widget.HTMLNode(nodeConfig, parentNode);
        },

        /**
         * Update node with the given item info
         * @param item - new item info
         * @param node - tree node to be updated
         */
        _updateNode: function(item, node) {
            var nodeConfig = this._generateNodeConfig(item, node.parent.data.item);
            node.initContent(nodeConfig, true);
            // hack to update node styles
            node.labelStyle = nodeConfig.labelStyle;
            node.contentStyle = nodeConfig.contentStyle;
        },

        /**
         * Search nodes, that correspond to given item.
         * @param item - item to search
         * @param includeRoot - include rot in search or not
         */
        _getItemNodes: function(item, includeRoot) {
            var nodes = [],
                tree = this.widgets.tree;
            // DOG-NAIL: here we checks the tree because of there is some bug
            if (tree) {
                var nodes2 = tree.getNodesByProperty("_item_name_", item._item_name_);
                if(nodes2 && nodes2.length) {
                    nodes = nodes2;
                }
                if(includeRoot && tree.getRoot().data.item._item_name_ == item._item_name_) {
                    nodes.push(tree.getRoot());
                }
            }
            return nodes;
        },

        _renderLabel: function(item, link) {
            var label = this.model.getItemTitle(item);
            var result = label;
            if (link && link.url) {
                var url = encodeURI(YAHOO.lang.substitute(link.url, item));
                result = '<a href="' + url + '"' + (link.target ? ' target="' + link.target + '"' : '') + '>' + label + '</a>';
            }
            return result;
        }
    });

})();