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
Citeck.namespace('invariants');
(function() {
    
    Citeck.invariants.NodeViewManager = function(key) {
        this.key = key;
        this.options = {
            onsubmit: null,
            oncancel: null
        };
        this.behaviours = {
            back: this.goBack,
            card: this.goToCard
        };
        this.defaultBehaviour = this.behaviours.back;
        
        YAHOO.Bubbling.on("node-view-submit", this.onSubmit, this);
        YAHOO.Bubbling.on("node-view-cancel", this.onCancel, this);
    }
    
    Citeck.invariants.NodeViewManager.prototype = {
        
        setOptions: function(options) {
            _.each(options, function(value, key) {
                if(value != null) {
                    this.options[key] = value;
                }
            }, this);
            return this;
        },
        
        onSubmit: function(layer, args) {
            if(this.key != args[1].key) return;
            var node = args[1].node;
            node.thisclass.save(node, {
                scope: this,
                fn: function(result) {
                    var submitBehaviour = this.behaviours[this.options.onsubmit] || this.defaultBehaviour;
                    submitBehaviour.call(this, result);
                }
            });
        },
        
        onCancel: function(layer, args) {
            if(this.key != args[1].key) return;
            var node = args[1].node;
            var cancelBehaviour = this.behaviours[this.options.oncancel] || this.defaultBehaviour;
            cancelBehaviour.call(this, node);
        },
        
        goBack: function(node) {
            if(history.length > 1) {
                history.go(-1);
            } else if(document.referrer) {
                document.location.href = document.referrer;
            } else {
                document.location.href = Alfresco.constants.URL_CONTEXT;
            }
        },
        
        goToCard: function(node) {
            document.location.href = Alfresco.constants.URL_PAGECONTEXT + "document-details?nodeRef=" + node.nodeRef;
        }
        
    };
    
})()