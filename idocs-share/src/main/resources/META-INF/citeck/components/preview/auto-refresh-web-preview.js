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
Alfresco.AutoRefreshWebPreview = function(htmlid) {
	Alfresco.AutoRefreshWebPreview.superclass.constructor.call(this, htmlid, "Alfresco.AutoRefreshWebPreview");
	
	// subscribe on metadata change:
	YAHOO.Bubbling.on("metadataRefresh", this.doRefresh, this);
};

YAHOO.extend(Alfresco.AutoRefreshWebPreview, Alfresco.WebPreview, {

	// add auto-refreshing:
	doRefresh: function() {
		YAHOO.Bubbling.unsubscribe("metadataRefresh", this.doRefresh, this);
		this.refresh("components/preview/web-preview?nodeRef=" + this.options.nodeRef);
	},

});
