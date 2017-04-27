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
if(typeof Citeck.widget == "undefined") Citeck.widget = {};

var Dom = YAHOO.util.Dom,
	Event = YAHOO.util.Event;

Citeck.widget.AutoManualInput = function(htmlId) {
	Citeck.widget.AutoManualInput.superclass.constructor.call(this, "Citeck.widget.AutoManualInput", htmlId);
};

YAHOO.extend(Citeck.widget.AutoManualInput, Alfresco.component.Base, {

	onReady: function() {
		this.field = Dom.get(this.id);
		this.widgets.auto = Dom.get(this.id + "-auto");
		this.widgets.input = Dom.get(this.id + "-input");
	
		Event.addListener(this.widgets.auto, "change", this.onAutoChange, this, true);
		Event.addListener(this.widgets.input, "change", this.onInputChange, this, true);
		Event.addListener(this.widgets.input, "keydown", this.onInputChange, this, true);
		Event.addListener(this.widgets.input, "blur", this.onInputChange, this, true);
		
		// initial update:
		this._update();
	},
	
	onAutoChange: function() {
		this._update();
		if(!this.widgets.auto.checked) {
			this.widgets.input.focus();
		}
	},
	
	onInputChange: function() {
		this._update();
	},
	
	_update: function() {
		if(this.updating) return;
		this.updating = true;
		if(this.widgets.auto.checked) {
			this.widgets.input.disabled = true;
			this.field.value = "-";
		} else {
			this.widgets.input.disabled = false;
			this.field.value = this.widgets.input.value;
		}
		this.updating = false;
	},

});


})();
