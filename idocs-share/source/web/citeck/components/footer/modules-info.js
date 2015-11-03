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

if(typeof Citeck == "undefined") Citeck = {};
Citeck.component = Citeck.component || {};

var thisClass = 
Citeck.component.ModulesInfo = function(id) {
	thisClass.superclass.constructor.call(this, "Citeck.component.ModulesInfo", id);
}

YAHOO.extend(thisClass, Alfresco.component.Base, {

	onReady: function() {
	
		Alfresco.util.Ajax.jsonGet({
			url: Alfresco.constants.PROXY_URI + "modules/info",
			successCallback: {
				scope: this,
				fn: function(response) {
					var modules = response.json.modules;
					this.renderModulesInfo(this.id + "-repo", modules);
				}
			}
		});
	
		Alfresco.util.Ajax.jsonGet({
			url: Alfresco.constants.URL_SERVICECONTEXT + "modules/info",
			successCallback: {
				scope: this,
				fn: function(response) {
					var modules = response.json.modules;
					this.renderModulesInfo(this.id + "-share", modules);
				}
			}
		});
	
	},
	
	renderModulesInfo: function(id, modules) {
		var moduleStrings = [];
		for(var i = 0; i < modules.length; i++) {
			moduleStrings[i] = '<span class="module-info" title="' + modules[i].title + '">' + modules[i].id + ': ' + modules[i].version + '</span>';
		}
		Dom.get(id).innerHTML = moduleStrings.join('');
	},

});

})();