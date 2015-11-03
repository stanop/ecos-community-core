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
	Citeck = typeof Citeck != "undefined" ? Citeck : {};
	Citeck.util = Citeck.util || {};

	/**
	 * Error manager mix-in.
	 * Manages error messages, returned by server and converts them into readable messages.
	 * Requirements on objects, that augment ErrorManager:
	 * - they should have 'msg' method to retrieve i18n strings;
	 * - they should have 'config.errors' or 'options.errors' array, that contains error message configuration.
	 * Error message configuration is written as:
	 * errors: [
	 *   {
	 *     regexp: "server response regexp",
	 *     message: "message with arguments from regexp: {1}, {2}, {3}, ..."
	 *   },
	 *   ...
	 * ]
	 */
	Citeck.util.ErrorManager = function() {
	};
	
	YAHOO.lang.augmentObject(Citeck.util.ErrorManager.prototype, {

		// get error message from server response
		// utilize config.errors configuration
		// error messages are processed in order specified
		_getErrorMessage: function(response) {
			var config = this.config && this.config.errors || this.options && this.options.errors;
			if(config) {
				for(var i = 0; i < config.length; i++) {
					var code = response.serverResponse.responseText,
						vars = code.match(this.msg(config[i].regexp));
					if (vars) {
						var message = YAHOO.lang.substitute(this.msg(config[i].message || "{0}"), vars);
						if(message.indexOf("\\u") > -1) {
							message = JSON.parse('"' + message.replace(/\\?"/, '\\"') + '"');
						}
						return message;
					}
				}
			}
			return this.msg("message.failure");
		},
		
		/**
		 * Standard failure handler.
		 * Shows error message.
		 */
		onFailure: function(response) {
			Alfresco.util.PopupManager.displayMessage({
				text: this._getErrorMessage(response),
			});
		},
		
	});

})();