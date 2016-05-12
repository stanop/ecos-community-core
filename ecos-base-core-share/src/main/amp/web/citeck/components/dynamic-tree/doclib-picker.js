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

	var $buttonSubscribe = Citeck.widget.HasButtons.subscribe;
	var $buttonUnsubscribe = Citeck.widget.HasButtons.unsubscribe;

	var thisClass = Citeck.widget.DoclibPickerControl = function(htmlid, fieldId, pageHtmlId) {
		thisClass.superclass.constructor.call(this, htmlid, fieldId, pageHtmlId);
		YAHOO.Bubbling.on("metadataRefresh", this.onMetadataRefresh, this);
	};

	YAHOO.extend(Citeck.widget.DoclibPickerControl, Citeck.widget.UploadPickerControl, {

		options: YAHOO.lang.merge(Citeck.widget.UploadPickerControl.prototype.options, {
			// custom upload button label:
			uploadButtonLabel: null,
		}),

		_initCurrentValuesList: function() {
			this.widgets.table = new Citeck.widget.DynamicDoclibTable(this.id + "-currentValueDisplay", this.model, this.name);
			this.widgets.table.setContext("selected-items", "none");
			// when our documentlist is ready
			YAHOO.Bubbling.on("dynamicDoclibTableLoaded", function(layer, args) {
				// react only on our documentlist
				var eventGroup = args[1].eventGroup;
				if(eventGroup != this.id + "-currentValueDisplay") {
					return;
				}
				// register "unselect" action"
				YAHOO.Bubbling.fire("registerAction", {
					actionName: "onActionUnselect",
					fn: this.bind(this.onActionUnselect)
				});
			}, this);
			// subscribe on button events:
			$buttonSubscribe("itemUnselect", this.onItemUnselect, this, [this.widgets.table.id]);
		},

		onActionUnselect: function(record) {
			//*
			YAHOO.Bubbling.fire("itemUnselect", {
				from: "selected-items",
				item: record.nodeRef,
				id: this.id + "-currentValueDisplay"
			});
			//*/
		},

		onMetadataRefresh: function(layer, args) {
			var obj = args[1];
			if(obj && obj.highlightFile != null) {
				var items = this.model.getItemsByProperty("fileName", obj.highlightFile);
				for(var i in items) {
                    if(!items.hasOwnProperty(i)) continue;
					this.model.updateItem(items[i]);
				}
			}
		}

	});

})();
