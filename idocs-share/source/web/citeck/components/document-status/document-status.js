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
	Citeck = typeof Citeck != "undefined" ? Citeck : {};
	Citeck.widget = Citeck.widget || {};

	Citeck.widget.DocumentStatus = function(htmlid) {
		Citeck.widget.DocumentStatus.superclass.constructor.call(this, "Citeck.widget.DocumentStatus", htmlid, null);

		YAHOO.Bubbling.on("metadataRefresh", this.doRefresh, this);
	};
	
	YAHOO.extend(Citeck.widget.DocumentStatus, Alfresco.component.Base);
	
	YAHOO.lang.augmentObject(Citeck.widget.DocumentStatus.prototype, {
		// default values for options
		options: {
			// parent node reference
			nodeRef: null
		},
		doRefresh: function DocumentStatus_doRefresh()
		{
			YAHOO.Bubbling.unsubscribe("metadataRefresh", this.doRefresh, this);
			this.refresh('/citeck/components/document-status?nodeRef={nodeRef}' + (this.options.siteId ? '&site={siteId}' : ''));
		}

	}, true);

})();
