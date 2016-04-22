/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * CustomiseLayout component.
 *
 * @namespace Alfresco
 * @class Alfresco.CustomiseLayout
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event;

   /**
    * Alfresco.CustomiseLayout constructor.
    *
    * @param {string} htmlId The HTML id of the parent element
    * @return {Alfresco.CustomiseLayout} The new CustomiseLayout instance
    * @constructor
    */
   Alfresco.CustomiseLayout = function(htmlId)
   {
      Alfresco.CustomiseLayout.superclass.constructor.call(this, "Alfresco.CustomiseLayout", htmlId, ["button", "container", "datasource"]);
//      this.name = "Alfresco.CustomiseLayout";
//      this.id = htmlId;
//
//      // Register this component
//      Alfresco.util.ComponentManager.register(this);
//
//      // Load YUI Components
//      Alfresco.util.YUILoaderHelper.require(["button", "container", "datasource"], this.onComponentsLoaded, this);
//
      return this;
   };

//   Alfresco.CustomiseLayout.prototype =
   YAHOO.extend(Alfresco.CustomiseLayout, Alfresco.component.Base,
   {
      widgets: {},

      options: {
         currentLayout: {},
         layouts: {}
      },

      setOptions: function CL_setOptions(obj) {
         this.options = YAHOO.lang.merge(this.options, obj);
         return this;
      },

      setMessages: function CL_setMessages(obj) {
         Alfresco.util.addMessages(obj, this.name);
         return this;
      },

      onReady: function CL_onReady() {
        if (this.id === null) {
          return;
        }

        this.widgets.layoutLiElements = [];
        this.widgets.layoutUlElement = Dom.get(this.id +"-layout-ul");

        for (var layoutId in this.options.layouts) {
          var layoutLi = Dom.get(this.id +"-layout-li-" + layoutId);
          this.widgets.layoutLiElements[layoutId] = layoutLi;

          // Add a listener to the image so we change layout when its clicked
          Event.addListener(this.id + "-select-img-" + layoutId, "click", function (event, obj) {
             obj.thisComponent.onSelectLayoutClick(obj.selectedLayoutId);
          }, {
             selectedLayoutId: layoutId,
             thisComponent: this
          });

          if (this.options.currentLayout.templateId == layoutId) {
            if (layoutLi) { 
              layoutLi.className += " layout-current";
              layoutLi.id = this.id + "-currentLayout-div";

              var layoutLiImage = Dom.getElementsByClassName("layoutIcon", "img", layoutLi)[0];
              layoutLiImage.id = this.id + "-currentLayoutIcon-img"; 

            } else {
              console.log("Element not available!");
            }
          }
        }
      },

      onSelectLayoutClick: function CL_onSelectLayoutClick(selectedLayoutId) {
        var selectedLayoutLi = Dom.get(this.id +"-layout-li-" + selectedLayoutId),
            selectedLayoutLiImg = Dom.getElementsByClassName("layoutIcon", "img", selectedLayoutLi)[0],
            currentLayoutLi = Dom.get(this.id +"-currentLayout-div"),
            currentLayoutLiImg = Dom.getElementsByClassName("layoutIcon", "img", currentLayoutLi)[0];

        if (selectedLayoutLi && currentLayoutLi) {
          currentLayoutLi.classList.remove("layout-current");
          currentLayoutLi.id = this.id + "-layout-li-" + this.options.currentLayout.templateId;
          currentLayoutLiImg.id = this.id + "-select-img-" + selectedLayoutId;

          this.options.currentLayout = this.options.layouts[selectedLayoutId];
          selectedLayoutLi.className += " layout-current";
          selectedLayoutLi.id = this.id + "-currentLayout-div";
          selectedLayoutLiImg.id = this.id + "-currentLayoutIcon-img";

          YAHOO.Bubbling.fire("onDashboardLayoutChanged", {
            dashboardLayout: this.options.layouts[selectedLayoutId]
          });
        }
      }
    });
})();

/* Dummy instance to load optional YUI components early */
new Alfresco.CustomiseLayout(null);
