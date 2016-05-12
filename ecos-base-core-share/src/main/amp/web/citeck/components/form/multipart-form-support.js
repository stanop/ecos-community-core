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

var thisClass = MultipartFormSupport = {

	_findFormsRuntime: function(targetId) {

		var forms = document.getElementsByTagName("form");
		var form = null;
		for(var i = 0; i < forms.length; i++) {
			if(forms[i].target == targetId) {
				form = forms[i];
				break;
			}
		}
		if(!form) return null;

		var formUI = Alfresco.util.ComponentManager.get(form.id);
		if(!formUI) return null;

		return formUI.formsRuntime;

	},

	_processCallback: function(response, targetId, callbackName) {
		var formsRuntime = thisClass._findFormsRuntime(targetId);
		if(!formsRuntime || !formsRuntime.ajaxSubmitHandlers) return;
		
		var callback = formsRuntime.ajaxSubmitHandlers[callbackName];
		if(callback && callback.fn) {
			callback.fn.call(callback.scope || formsRuntime, { json: response }, callback.obj);
		}
	},

	onSubmitSuccess: function(response, targetId) {
		thisClass._processCallback(response, targetId, "successCallback");
	},

	onSubmitFailure: function(response, targetId) {
		thisClass._processCallback(response, targetId, "failureCallback");
	},

}

})();