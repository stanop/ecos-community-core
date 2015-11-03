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
_.extend(Citeck.format, {

	buttons: function() {
		return function(elCell, oRecord, oColumn, oData) {
			var data = oData || oRecord.getData(),
				Button = YAHOO.widget.Button;
			elCell.innerHTML = "";
			var editButton = new Button({
				container: elCell,
				type: 'link',
				title: Alfresco.util.message('button.edit'),
				href: Alfresco.constants.URL_PAGECONTEXT + "edit-metadata?nodeRef=" + data.nodeRef
			});
			var removeButton = new Button({
				container: elCell,
				type: 'push',
				title: Alfresco.util.message('button.remove'),
				onclick: { 
					fn: function() {
						YAHOO.Bubbling.fire("removeJournalRecord", oRecord.getData('nodeRef'));
    					}
				}
			});
			Dom.addClass(elCell, "actions-cell");
			editButton.addClass('edit');
			removeButton.addClass('remove');
		};
	},

});
