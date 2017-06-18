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

    Citeck.widget.InactiveWorkflows = function(htmlid) {
        Citeck.widget.InactiveWorkflows.superclass.constructor.call(this, "Citeck.widget.InactiveWorkflows", htmlid, null);

        YAHOO.Bubbling.on("metadataRefresh", this.doRefresh, this);
    };

    YAHOO.extend(Citeck.widget.InactiveWorkflows, Alfresco.component.Base);

    var $siteURL = Alfresco.util.siteURL;

    YAHOO.lang.augmentObject(Citeck.widget.InactiveWorkflows.prototype, {
        options:
        {
            /**
             * Defines what kind of workflows would be shown (active or inactive)
             */
            isActiveWorkflows: "",

            /**
             * Field for sorting
             */
            sortBy: "",

            /**
             * Reference to the current document
             *
             * @property nodeRef
             * @type string
             */
            nodeRef: null,

            /**
             * Current siteId, if any.
             *
             * @property siteId
             * @type string
             */
            siteId: "",

            /**
             * Reference to the parent which will be used when POSTing to the start workflow page.
             *
             * @property destination
             * @type string
             */
            destination: null
        },

        /**
         * Assign Workflow click handler
         *
         * @method onAssignWorkflowClick
         */
        onAssignWorkflowClick: function DocumentWorkflows_onAssignWorkflowClick()
        {
            Alfresco.util.navigateTo($siteURL("start-workflow"), "POST",
                {
                    selectedItems: this.options.nodeRef,
                    destination: this.options.destination
                });
        },

        doRefresh: function DocumentStatus_doRefresh()
        {
            YAHOO.Bubbling.unsubscribe("metadataRefresh", this.doRefresh, this);
            var url = "/components/document-details/inactive-workflows";
            this.refresh(url+"?nodeRef={nodeRef}"
                            + (this.options.siteId ? "&site={siteId}" : "")
                            + (this.options.sortBy ? "&sortBy={sortBy}" : "")
                            + "&active={isActiveWorkflows}");
        }
    }, true);

})();