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
 * DocumentVersionsComparison document-details component.
 *
 * @class Citeck.widget.DocumentVersionsComparison
 */
if (typeof Citeck == "undefined" || !Citeck) {
    var Citeck = {};
}
if (typeof Citeck.widget == "undefined" || !Citeck.widget) {
    Citeck.widget = {};
}

(function() {

    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event,
        Element = YAHOO.util.Element;

    /**
     * DocumentVersionsComparison constructor
     *
     * @param htmlId - identifier of component root DOM element.
     */
    Citeck.widget.DocumentVersionsComparison = function(htmlId) {

        Citeck.widget.DocumentVersionsComparison.superclass.constructor.call(this, "Citeck.widget.DocumentVersionsComparison", htmlId, ["button", "menu"]);

    };

    YAHOO.extend(Citeck.widget.DocumentVersionsComparison, Alfresco.component.Base, {

        options: {

            /**
             * NodeRef of target object
             *
             * @property nodeRef
             * @type string
             */
            nodeRef: null

        },

        versRefButtonButtonMenu : null,
        nodeRefButtonButtonMenu : null,

        /**
         * Event handler called when "onReady"
         *
         * @method: onReady
         */
        onReady: function() {
            var me = this;

            var compareButton = Dom.get(this.id + '-compare-button');
            var compareVersionButton = new YAHOO.widget.Button(compareButton, {
                type: "button",
                label: this.msg("button.compare")
            });
            compareVersionButton.on("click", this.redirectVersionsCompare, compareVersionButton, this);

            YAHOO.Bubbling.on("metadataRefresh", this.refreshVersionsCompare, this);

            this.refreshVersionsCompare();
        },

        refreshVersionsCompare: function QB_refreshVersionsCompare() {
            var me = this;
            var versRefMenu = [];
            var nodeRefMenu = [];

            this.versRefButtonMenu = new YAHOO.widget.Button(this.id + "-versRef", {
                type: "menu",
                label: this.msg("label.prevVersRef"),
                name: "versRef",
                menu: versRefMenu
            });

            this.nodeRefButtonMenu = new YAHOO.widget.Button(this.id + "-nodeRef", {
                type: "menu",
                label: this.msg("label.currentVnodeRef"),
                name: "nodeRef",
                menu: nodeRefMenu
            });

            var onVersRefMenuItemClick = function (p_sType, p_aArgs, p_oItem) {
                var sText = p_oItem.cfg.getProperty("text");
                me.versRefButtonMenu.set("label", sText);
            };

            var onNodeRefMenuItemClick = function (p_sType, p_aArgs, p_oItem) {
                var sText = p_oItem.cfg.getProperty("text");
                me.nodeRefButtonMenu.set("label", sText);
            };
            var versionUrl = Alfresco.constants.PROXY_URI + 'api/version?nodeRef=' + this.options.nodeRef;
            YAHOO.util.Connect.asyncRequest(
                'GET',
                versionUrl, {
                    success: function (response) {
                        if (response.responseText) {
                            var result = eval('(' + response.responseText + ')');
                            var versionsJson =[];
                            var versionsLabel =[];
                            for(var i=0; i<result.length; i++) {
                                versionsJson[result[i].label] = result[i].nodeRef;
                                versionsLabel[i] = result[i].label;
                            }
                            versionsLabel.sort(me.sortVersionsIncrease);
                            for(var i=0; i<versionsLabel.length; i++) {
                                versRefMenu.push({
                                    'text': versionsLabel[i],
                                    'value': versionsJson[versionsLabel[i]],
                                    'onclick': { fn: onVersRefMenuItemClick }
                                });
                                nodeRefMenu.push({
                                    'text': versionsLabel[i],
                                    'value': versionsJson[versionsLabel[i]],
                                    'onclick': { fn: onNodeRefMenuItemClick }
                                });
                            }
                            var lastVersion = versionsLabel[versionsLabel.length-1];
                            me.nodeRefButtonMenu.set("label", lastVersion);
                            me.nodeRefButtonMenu.set("selectedMenuItem", { value: versionsJson[lastVersion]});

                            if(versionsLabel.length > 1) {
                                var penultVersion = versionsLabel[versionsLabel.length-2];
                                me.versRefButtonMenu.set("label", penultVersion);
                                me.versRefButtonMenu.set("selectedMenuItem", { value: versionsJson[penultVersion]});
                            } else {
                                me.versRefButtonMenu.set("label", lastVersion);
                                me.versRefButtonMenu.set("selectedMenuItem", { value: versionsJson[lastVersion]});
                            }
                        }
                    },
                    failure: function() {
                        Alfresco.logger.error("association request error");
                    },
                    scope: this
                }
            );

        },

        sortVersionsIncrease: function QB_sortVersionsIncrease(a, b) {
            var aArray = a.split(".");
            var bArray = b.split(".");
            if (aArray[0]*1 > bArray[0]*1) {
                return 1
            }
            if (aArray[0]*1 < bArray[0]*1) {
                return -1;
            }
            if (aArray[1]*1 > bArray[1]*1) {
                return 1
            }
            if (aArray[1]*1 < bArray[1]*1) {
                return -1;
            }

            return 0;
        },

        redirectVersionsCompare: function QB_redirectVersionsCompare() {

            document.location.href = '/share/page/versions-difference?versRef='+ this.versRefButtonMenu.get("selectedMenuItem").value
                + '&nodeRef=' + this.nodeRefButtonMenu.get("selectedMenuItem").value;
        }
    });

})();
