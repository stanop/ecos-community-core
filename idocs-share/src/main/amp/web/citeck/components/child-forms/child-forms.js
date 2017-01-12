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
Citeck.namespace('widget');
(function() {

    var Dom = YAHOO.util.Dom
        , Ajax = Alfresco.util.Ajax
        ;

    Citeck.widget.ChildForms = function(htmlid) {
        Citeck.widget.ChildForms.superclass.constructor.call(this, "Citeck.widget.ChildForms", htmlid, [ 'button' ]);

        YAHOO.Bubbling.on("itemCreated", this.reload, this);
        YAHOO.Bubbling.on('metadataRefresh', this.refresh, this);
        this.elements = {};
    };

    YAHOO.extend(Citeck.widget.ChildForms, Alfresco.component.Base, {
        
        options: {

            nodeRef: '',
        
            elementsUrl: '',

            elementsPath: null,
            
            elementHeader: '{name}',

            viewFormUrl: '',
            editFormUrl: '',
            createFormUrl: '',

            collapseHeader: false,

            noElementsMsg: ''
        },

        onReady: function() {
            this.rootElement = Dom.get(this.id + "-elements");
            this.refresh();
        },

        destroy: function() {
            YAHOO.Bubbling.unsubscribe("itemCreated", this.reload, this);
            YAHOO.Bubbling.unsubscribe('metadataRefresh', this.refresh, this);
        },

        template: function(template, model) {
            var s = template.replace(/\[\[/g, '{').replace(/\]\]/g, '}');
            return YAHOO.lang.substitute(s, model, function(key, value) {
                if(!key) return "";
                return _.reduce(key.split(/\./), function(model, key) {
                    return model && model[key];
                }, model);
            });
        },

        reload: function() {
            location.reload(true);
        },

        refresh: function() {
            if(this.refreshing) return;
            var url = this.template(this.options.elementsUrl, {
                nodeRef: this.options.nodeRef
            });
            Ajax.jsonGet({
                url: Alfresco.constants.PROXY_URI + url,
                successCallback: {
                    scope: this,
                    fn: this.onDataLoaded
                }
            });
            this.refreshing = true;
        },

        onDataLoaded: function(response) {
            var path = this.options.elementsPath
                , results = _.reduce(path ? path.split(/\./) : [], function(results, key) {
                        return results ? results[key] : [];
                    }, response.json)
                ;
            if(!results || results.length == 0) {
                this.rootElement.innerHTML = this.options.noElementsMsg;
                this.refreshing = false;
                return;
            }
            this.rootElement.innerHTML = "";
            _.each(results, function(result) {
                var block = document.createElement("DIV")
                    , header = this.template(this.options.elementHeader, result)
                    , htmlid = Alfresco.util.generateDomId()
                    , viewFormUrl = this.template(this.options.viewFormUrl, result)
                    , html = "<div id='" + htmlid + "-form-container'></div>"
                    ;
                
                this.elements[htmlid] = result;
                var editHTML = this.options.editFormUrl ? "<span id=\"" + htmlid + "-heading-placeholder\"></span><span id=\"" + htmlid + "-heading-actions\" class=\"alfresco-twister-actions\" style=\"position:relative;float:right;\"></span>" : "";
                if(this.options.collapseHeader) {
                    Dom.get(this.id + "-heading-placeholder").innerHTML = header;
                } else {
                    html = "<h3 id='"+htmlid+"-heading'>" + header + "&nbsp; " + editHTML + "</h3>" + html;
                }
                block.setAttribute('id', htmlid);
                block.innerHTML = html;
                this.rootElement.appendChild(block);
                var bp = new Citeck.widget.ButtonPanel(htmlid + "-heading-actions");
                bp.setOptions({ args : {
                    "buttonsInHeader" : "onPanelButtonEdit",
                    "formId" : this.options.elementFormId,
                    "nodeRef" : result.nodeRef,
                    "header" : this.template(this.options.elementHeader, result)
                } });
                Ajax.jsonGet({
                    url: Alfresco.constants.URL_SERVICECONTEXT + viewFormUrl + "&htmlid=" + htmlid,
                    execScripts: true,
                    successCallback: {
                        scope: this,
                        fn: function(response) {
                            Dom.get(htmlid + "-form-container").innerHTML = response.serverResponse.responseText;
                            Alfresco.util.createTwister(htmlid + "-heading");
                        }
                    }
                });
            }, this);
            this.refreshing = false;
        }

    });

})();
