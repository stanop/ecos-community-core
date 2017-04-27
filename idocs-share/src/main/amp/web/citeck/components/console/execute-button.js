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

(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
       Event = YAHOO.util.Event,
       Element = YAHOO.util.Element,
       KeyListener = YAHOO.util.KeyListener;
    
    /**
     * Alfresco Slingshot aliases
     */
    var $html = Alfresco.util.encodeHTML,
        $combine = Alfresco.util.combinePaths;


   /**
    * ExecuteButton constructor.
    * 
    * @param {String} htmlId The HTML id of the parent element
    * @return {Alfresco.ExecuteButton} The new ExecuteButton instance
    * @constructor
    */
   Alfresco.ExecuteButton = function(htmlId)
   {
      Alfresco.ExecuteButton.superclass.constructor.call(this, "Alfresco.ExecuteButton", htmlId, ["button", "json"]);
      
      return this;
   };

   YAHOO.extend(Alfresco.ExecuteButton, Alfresco.component.Base,
   {
		options: {
		
			/**
			 * url
			 */
			url: null,
			
			/**
			 * button title
			 */
			title: null,
			/**
			 * success Message
			 */
			successMessage: null,
			/**
			 * failure Message
			 */
			failureMessage: null
		},

      /**
       * Fired by YUI when parent element is available for scripting.
       *
       * @method onReady
       */
      onReady: function ExecuteButton_onReady()
      {
         // Reference to self - used in inline functions
         var me = this;
         
		this.widgets.button = new YAHOO.widget.Button({
			type: "button",
			container: me.id,
			label: me.options.title,
			onclick: {
				fn: me.onButtonClick,
				scope: me
			},
		});
      },

	  onButtonClick: function ExecuteButton_onButtonClick() {
		var me = this;
		var button = me.widgets.button;
		button.set("disabled", true);
		Alfresco.util.Ajax.jsonPost(
         {
            url: Alfresco.constants.PROXY_URI +me.options.url,
            successCallback:
            {
               fn: function(res)
               {
					var json = Alfresco.util.parseJSON(res.serverResponse.responseText);
					Alfresco.util.PopupManager.displayPrompt(
					{
						title: Alfresco.util.message("message.success"),
						text: me.options.successMessage
					});
					button.set("disabled", false);
               },
               scope: me
            },
            failureCallback:
            {
               fn: function(res)
               {
					var json = Alfresco.util.parseJSON(res.serverResponse.responseText);
					Alfresco.util.PopupManager.displayPrompt(
					{
						title: Alfresco.util.message("message.failure"),
						text: me.options.failureMessage
					});
					button.set("disabled", false);
               },
               scope: me
            }
         });
	  }
  })

})();