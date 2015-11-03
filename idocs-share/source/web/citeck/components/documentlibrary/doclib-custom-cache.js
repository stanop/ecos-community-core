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
 * Doclib Custom Cache.
 * Remembers registered actions and property renderers 
 *   and sets them on documentlists (Alfresco.DocumentList), that appear after registration.
 */
(function() {

var actions = {};
var renderers = {};

YAHOO.Bubbling.on("registerAction", function(layer, args) {
	var actionName = args[1].actionName;
	var fn = args[1].fn;
	actions[actionName] = fn;
});

YAHOO.Bubbling.on("registerRenderer", function(layer, args) {
	var propertyName = args[1].propertyName;
	var fn = args[1].renderer;
	renderers[propertyName] = fn;
});

var registerCached = function(layer, args) {

	var doclibComponent = args[1].scope;

	for(var name in actions) {
		if(!actions.hasOwnProperty(name)) continue;
		if(typeof doclibComponent.registerAction == "function") {
			doclibComponent.registerAction(name, actions[name]);
		}
	}
	
	for(var name in renderers) {
		if(!renderers.hasOwnProperty(name)) continue;
		if(typeof doclibComponent.registerRenderer == "function") {
			doclibComponent.registerRenderer(name, renderers[name]);
		}
	}

}

YAHOO.Bubbling.on("handyDoclistReady", registerCached);
YAHOO.Bubbling.on("postDocumentListOnReady", registerCached);
YAHOO.Bubbling.on("registerDoclibCustom", registerCached);

})();
