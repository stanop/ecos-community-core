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
Citeck.namespace('module');

/**
 * CreateVariants Module
 *
 * @namespace Citeck.module
 * @class CreateVariants
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

    thisClass = Citeck.module.CreateVariants = function(htmlId)
    {
        return thisClass.superclass.constructor.call(this, "Citeck.module.CreateVariants", htmlId, ["button", "menu", "container"]);
    };

    YAHOO.extend(thisClass, Alfresco.component.Base,
        {
            onReady: function() {
                if(Dom.get(this.id) == null) return;
                Dom.setStyle(this.id, 'display', 'none');
                var site = Alfresco.constants.URI_TEMPLATES.lastsite;
                if(!site) {
                    return;
                }

                Alfresco.util.Ajax.jsonGet({
					url: Alfresco.constants.PROXY_URI + "api/journals/create-variants/site/" + site + "?writable=true",
					successCallback:
					{
						fn: this.onDataLoaded,
						scope: this
					},
					failureCallback:
					{
						fn: this.onDataFailure,
						scope: this
					}
				});
            },

            onDataLoaded: function (response)
            {
                var createVariants = response.json.createVariants;
				if(createVariants.length == 0) {
					return;
				}
				
				var menu = this.widgets.createMenu = new YAHOO.widget.Menu("create-menu");
				menu.addItems(_.map(createVariants, function(createVariant) {
					return {
						text: createVariant.title,
						url: YAHOO.lang.substitute(Alfresco.constants.URL_PAGECONTEXT + "node-create?type={type}&viewId={formId}&destination={destination}", createVariant)
					};
				}));
				menu.render(document.body);
				
				this.widgets.createButton = new YAHOO.widget.Button(this.id, {
					type: "menu",
					menu: menu
				});
				Dom.setStyle(this.id, 'display', '');
            },

            onDataFailure: function (response)
            {
				var responseStatusCode = response.serverResponse.status;
				if(responseStatusCode == 404) {
					// site not found - ignore
					return;
				}
				
				var msg = this.bind(this.msg);
				Alfresco.util.PopupManager.displayPrompt({
					title: msg("message.failure"),
					text: (response.json && response.json.message || response.serverResponse.statusText) + " (" + responseStatusCode + ")"
				});
            },

        });
})();