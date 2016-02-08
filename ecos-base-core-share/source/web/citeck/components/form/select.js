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
 * Dynamic Select control.
 */
(function() {

	var Dom = YAHOO.util.Dom;

	Alfresco.SelectControl = function Alfresco_SelectControl(htmlId) {
	    Alfresco.SelectControl.superclass.constructor.call(this, "Alfresco.SelectControl", htmlId, ["button"]);
	    this.loaded = false;
	    return this;
	}

	YAHOO.extend(Alfresco.SelectControl, Alfresco.component.Base, {

		options: {

			/**
			 * Options URL - web script, returning name and title of 
			 */
			optionsUrl: "",
			
			/**
			 * Form mode: view, edit or create
			 */
			mode: "edit",
			
			/**
			 * Response type
			 */
			responseType: null,
			
			/**
			 * Response schema
			 */
			responseSchema: null,
			
			/**
			 * Request param
			 */
			requestParam: null,
			
			/**
			 * Title field
			 */
			titleField: "title",
			
			/**
			 * Value field
			 */
			valueField: "value",
			
			/**
			 * Results list path
			 */
			resultsList: null,
			
			/**
			 * Currently selected item
			 */
			selectedItem: null,
			
			/**
			 * Originally selected item (persisted)
			 */
			originalValue: null,
			
			/**
			 * Sort key - some field in response, 
			 *  that is used to sort options.
			 * If it is null, the sorting is not performed.
			 */
			sortKey: null
			
		},

		onReady: function() {

			this.options.valueField = this._protectField(this.options.valueField);
			this.options.titleField = this._protectField(this.options.titleField);

			this.dataSource = new YAHOO.util.XHRDataSource(this.options.optionsUrl);
			if(this.options.responseType) {
				this.dataSource.responseType = this.options.responseType;
			} else if(this.options.resultsList) {
				this.dataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
			} else {
				this.dataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
			}
			if(this.options.responseSchema) {
				this.dataSource.responseSchema = this.options.responseSchema;
			} else if(this.options.resultsList) {
				this.dataSource.responseSchema = {
					resultsList: this.options.resultsList,
					fields: [ 
						{ key: this.options.valueField }, 
						{ key: this.options.titleField }
					]
				};
			} else {
				this.dataSource.responseSchema = {
					fields: [ this.options.valueField, this.options.titleField ]
				};
			}
			
			if(this.options.mode != "view" || this.options.selectedItem || this.options.originalValue) {
			    this.dataSource.sendRequest(this.options.requestParam, {
			        success: this.options.mode != "view" ? this.onLoadSuccess : this.onLoadSuccessView,
			        failure: this.onLoadFailure,
			        scope: this
			    });
			}
		},
		
		selectItem: function(item) {
		    if(!item) item = "";
		    if(!this.loaded) { 
		        this.options.selectedItem = item;
		        return;
		    }
		    var select = Dom.get(this.id);
		    select.value = item;
		    this.onValueChanged();
		    if($.fn.simulate) {
		        $(select).simulate('change');
		    }
		},
		
		selectItems: function(items) {
		    return this.selectItem(items);
		},

		_protectField: function(name) {
			if(name.match(/[-:]/)) {
				return '["' + name + '"]';
			} else {
				return name;
			}
		},
		
		onValueChanged: function(e) {
		    var select = Dom.get(this.id),
		        addedField = Dom.get(this.id+'-added'),
		        removedField = Dom.get(this.id+'-removed');
		    this.options.selectedItem = select.value;
		    if (this.options.originalValue != select.value) {
		        if (addedField) addedField.value = select.value;
		        if (removedField) removedField.value = this.options.originalValue;
		    } else {
		        if (addedField) addedField.value = "";
		        if (removedField) removedField.value = "";
		    }
		    YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
		},

		onLoadSuccess: function(request, response, payload) {
			var select = Dom.get(this.id),
				selectedValue = this.options.selectedItem || this.options.originalValue;
			
			this.loaded = true;

			YAHOO.util.Event.addListener(this.id, "change", this.onValueChanged, this, true);

			var items = response.results;

			// optionally perform sorting
			if(this.options.sortKey) {
				var sortKey = this._protectField(this.options.sortKey);
				items = _.sortBy(items, sortKey);
			}

			for(var i in items) {
				if(!items.hasOwnProperty(i)) continue;
				var item = items[i],
					value = item[this.options.valueField],
					title = item[this.options.titleField] || value,
					selected = value == selectedValue,
					option = document.createElement('OPTION');
				option.text = title;
				option.value = value;
				option.selected = selected;
				select.options.add(option);
			}

			if(items.length == 0) {
				this.showFailureMessage(this.msg('message.no-options'));
			}
			if($.fn.simulate) {
				$(select).simulate('change');
			}
			
			YAHOO.Bubbling.fire("mandatoryControlValueUpdated", this);
		},

		onLoadSuccessView: function(request, response, payload) {
			var container = Dom.get(this.id),
			    selectedValue = this.options.selectedItem || this.options.originalValue,
			    selectedItem = _.find(response.results, function(item) {
			        return item[this.options.valueField] == selectedValue;
			    }, this);
			if(selectedItem) {
			    container.innerHTML = selectedItem[this.options.titleField] || selectedValue;
			} else {
			    this.showFailureMessage(this.msg('message.selected-item.not-available'));
			}
		},

		onLoadFailure: function() {
			this.showFailureMessage(this.msg('message.load-failed'));
		},

		showFailureMessage: function(message) {
			Dom.addClass(Dom.get(this.id), "hidden");
			Dom.get(this.id+"-error").innerHTML = message;
		},

	});

})();
