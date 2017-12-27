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

    Citeck.widget.ButtonClickHandler = function(eventName, handler, scope, ids) {
        this.eventName = eventName;
        this.handler = handler;
        this.scope = scope;
        this.ids = ids;

        this.subscribe();
    };

    Citeck.widget.ButtonClickHandler.prototype = {

        subscribe: function() {
            YAHOO.Bubbling.on(this.eventName, this.onEvent, this);
        },

        unsubscribe: function() {
            try {
                YAHOO.Bubbling.unsubscribe(this.eventName, this.onEvent, this);
            } catch(e) {
                // ignore
            }
        },

        onEvent: function(layer, args) {
            if(this.ids.indexOf(args[1].id) != -1) {
                this.handler.call(this.scope, args[1]);
            }
        },

    };

    /**
     * Some control, that has buttons
     */
    Citeck.widget.HasButtons = function() {
        YAHOO.Bubbling.on("registerButtons", this.onRegisterButtons, this);
    };

    var buttonHandlers = {};

    /**
     * Subscribe on button click.
     * @param eventName - button eventName
     * @param handler - handler function
     * @param scope - scope
     * @param ids - array of args.id values, that are accepted
     */
    Citeck.widget.HasButtons.subscribe = function(eventName, handler, scope, ids) {

        var handler = new Citeck.widget.ButtonClickHandler(eventName, handler, scope, ids);

        if(!buttonHandlers[scope.id]) {
            buttonHandlers[scope.id] = [];
        }
        buttonHandlers[scope.id].push(handler);

    };

    /**
     * Unsubscribe from button clicks - called on destroy.
     * @param scope - scope object, which was subscribed.
     */
    Citeck.widget.HasButtons.unsubscribe = function(scope) {
        var handlers = buttonHandlers[scope.id];
        if(!handlers) return;
        for(var i in handlers) {
            if(!handlers.hasOwnProperty(i)) continue;
            handlers[i].unsubscribe();
        }
        buttonHandlers[scope.id] = [];
    };

    YAHOO.extend(Citeck.widget.HasButtons, Alfresco.component.Base, {

        /**
         * Event handler - button types are registered.
         */
        onRegisterButtons: function(layer, args) {
            this.buttons = this._prepareMap(this.buttons || {}, args[1], "id");
        },

        // convert array to map, based on key property
        _prepareMap: function(map, array, keyProp) {
            for(var i = 0; i < array.length; i++) {
                map[array[i][keyProp]] = array[i];
            }
            return map;
        },

        _getButtonIds: function(item) {
            item = this.model.getItem(item);
            return this.model.getItemProperty(item, this.config.buttons) || [];
        },

        /**
         * Render buttons for given item and its parent.
         */
        _renderButtons: function(item, parent) {
            item = this.model.getItem(item);
            parent = this.model.getItem(parent);

            var buttonIds = this._getButtonIds(item);

            var html = "";
            // generate html:
            for(var i in buttonIds) {
                if(!buttonIds.hasOwnProperty(i)) continue;
                var button = this.buttons && this.buttons[buttonIds[i]];
                if(!button) {
                    button = {
                        id: buttonIds[i],
                        event: buttonIds[i],
                    };
                }
                var eventArgs = [
                    "id:'" + this.id + "'",
                    "item:'" + item._item_name_ + "'",
                    "from:'" + (parent && parent._item_name_ || "root") + "'"
                ];
                for(var j in button.eventArgs) {
                    if(!button.eventArgs.hasOwnProperty(j)) continue;
                    eventArgs.push("'" + j + "':'" + button.eventArgs[j] + "'");
                }
                html += "<button class='" + button.id + "' title='" + this._getButtonMessage(button, "title") +
                    "' onclick=\"javascript:YAHOO.Bubbling.fire('" + button.event + "', {" + eventArgs.join(",") + "}); " +
                    "var event = arguments[0] || window.event; event.cancelBubble = true; event.stopPropagation && event.stopPropagation();\">" +
                    "<label>" + this._getButtonMessage(button, "label") + "</label></button>";
                /* open in view mode (for debug purposes) */
                /* if (button.id == 'editItem') {
                    eventArgs.push("mode:'view'");
                    html += "<button class='" + button.id + "' title='" + "Просмотр" +
                        "' onclick=\"javascript:YAHOO.Bubbling.fire('" + button.event + "', {" + eventArgs.join(",") + "}); " +
                        "var event = arguments[0] || window.event; event.cancelBubble = true; event.stopPropagation && event.stopPropagation();\">" +
                        "<label>" + this._getButtonMessage(button, "label") + "</label></button>";
                } */
            }
            return html;
        },

        // get button message of specified type
        _getButtonMessage: function(button, msgType) {
            var msg = button[msgType];
            if(msg) return msg;
            var msgId = "button." + msgType + "." + button.id;
            msg = this.msg(msgId);
            return (msg != msgId) ? msg : "";
        },

    });

})();
