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
 * AllSites Module
 * 
 * @namespace Alfresco.module
 * @class .description
*/
(function()
{
	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom,
		Event = YAHOO.util.Event;

	/**
	 * Alfresco Slingshot aliases
	 */
	var $html = Alfresco.util.encodeHTML;

	Alfresco.module.AllSites = function(htmlId)
	{
		return Alfresco.module.AllSites.superclass.constructor.call(this, "Alfresco.module.AllSites", htmlId, ["button", "menu", "container"]);
	};

	YAHOO.extend(Alfresco.module.AllSites, Alfresco.component.Base,
	{
		/**
		 * Object container for initialization options
		 *
		 * @property options
		 * @type object
		 */
		options:
		{
		},
	  
        onReady: function() {
            if(Dom.get(this.id) == null) return;
			Dom.addClass(this.id, 'hidden');
			Alfresco.util.Ajax.request(
			{
				url: Alfresco.constants.URL_SERVICECONTEXT + "modules/header/all-sites",
				dataObj:
				{
					htmlid: this.id
				},
				successCallback:
				{
					fn: this.onSitesLoaded,
					scope: this
				},
				failureMessage: "Could not load user sites",
				scope: this,
				execScripts: true
			});
		},
	  
		onSitesLoaded: function (response)
		{
			// get user sites:
			var html = response.serverResponse.responseText;
			// create container:
			var containerDiv = document.createElement("div");
			containerDiv.innerHTML = html;
			document.body.insertBefore(containerDiv, document.body.firstChild);
			// create button in the container:
			this.widgets.sitesButton = new YAHOO.widget.Button(this.id,
			{
				type: "menu",
				menu: this.id + "-sites-menu",
				lazyloadmenu: false
			});
			var menu = this.widgets.sitesButton.getMenu();
			if(menu.getItems().length > 0) {
				menu.render();
				Dom.removeClass(this.id, 'hidden');
			}
		},
		
	});
})();
/**
 * Dummy instance to load optional YUI components early.
 * Use fake "null" id, which is tested later in onComponentsLoaded()
*/
var moduleSites = new Alfresco.module.AllSites("null");