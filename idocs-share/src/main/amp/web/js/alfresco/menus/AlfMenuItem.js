/**
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @module alfresco/menus/AlfMenuItem
 * @extends dijit/MenuItem
 * @mixes module:alfresco/menus/_AlfMenuItemMixin
 * @mixes module:alfresco/core/Core
 * @author Dave Draper
 */
define(["dojo/_base/declare", "dijit/MenuItem", "alfresco/_Alf.lib", 
        "alfresco/menus/_AlfMenuItemMixin", "alfresco/core/Core", "dojo/dom-class"],
        function(declare, MenuItem, _alflib, _AlfMenuItemMixin, AlfCore, domClass) {

  /**
   * Currently this extends the default Dojo implementation of a MenuItem without making any changes. Despite
   * it not providing any additional value-add yet it should still be used such that changes can be applied
   * without needing to modify page definition files.
   */
  return declare([MenuItem, _AlfMenuItemMixin, AlfCore], {

    clickEvent: null,
    inheriteClickEvent: false,

    movable: null,

    postMixInProperties: function alfresco_menus__AlfMenuItem__postMixInProperties() {
      if (this.clickEvent) this.clickEvent = eval("(" + this.clickEvent + ")")
      this.inherited(arguments);
    },

    onClick: function(event) {
      this.alfLog("log", "AlfMenuBarItem clicked");

      this.emitClosePopupEvent();
      event.stopPropagation();

      if (this.clickEvent) {
        this.clickEvent(event, document.getElementById(this.id), this);
        if (!this.inheriteClickEvent) return false;
      }

      if (this.targetUrl) {
        this.alfPublish("ALF_NAVIGATE_TO_PAGE", { url: this.targetUrl, type: this.targetUrlType, target: this.targetUrlLocation});
      } else if (this.publishTopic) {
        this.alfPublish(this.publishTopic, this.publishPayload ? this.publishPayload : {});
      } else {
        this.alfLog("error", "An AlfMenuItem was clicked but did not define a 'targetUrl' or 'publishTopic' or 'clickEvent' attribute", event);
      }
    },

    /**
     * Ensures that the supplied menu item label is translated.
     * @instance
     */
    postCreate: function alfresco_menus_AlfMenuItem__postCreate() {
      if (this.movable) _alflib.visibilityByWindowSizeEventSubscription(this.id, this.movable, true);

      this.setupIconNode();
      this.inherited(arguments);
    }
  });
});
