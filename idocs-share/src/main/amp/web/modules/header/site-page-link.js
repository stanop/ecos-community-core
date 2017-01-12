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
 * Document library link Module
 * Adds to header a link to repository (if there is no {site} set or to 
 * 
 * @namespace Alfresco.module
 * @class .description
*/
(function()
{
	/**
	 * YUI Library aliases
	 */
	var Selector = YAHOO.util.Selector;

	/**
	 * Alfresco Slingshot aliases
	 */
	var $html = Alfresco.util.encodeHTML;

	Alfresco.module.SitePageLink = function(htmlId)
	{
		return Alfresco.module.SitePageLink.superclass.constructor.call(this, "Alfresco.module.SitePageLink", htmlId, ["button", "menu", "container"]);
	};

	YAHOO.extend(Alfresco.module.SitePageLink, Alfresco.component.Base,
	{
        onReady: function() {
            if(Dom.get(this.id) == null) return;
			var tokens = Alfresco.constants.URI_TEMPLATES,
			    page = this.id.replace(/^(.*)-(\w+)_(\w+)$/, "$3"),
			    site = tokens.lastsite,
				el = Dom.get(this.id),
				contents = el.innerHTML;
			if(page == "repository" || page == "documentlibrary") {
				page = site ? "documentlibrary" : "repository";
			}
            var url = Alfresco.util.uriTemplate("sitepage", {
                site: site,
                pageid: page,
            });
            console.log(el);
            return;
            this.widgets.button = new YAHOO.widget.Button(this.id, {
                type: "push",
                onclick: {
                    scope: this,
                    fn: function() {
                        window.location = url;
                    }
                },
            });
		},
	  
	});
})();
