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

    Alfresco.SelectWithEditControl = function Alfresco_SelectControl(htmlId) {
        return Alfresco.RichTextControl.superclass.constructor.call(this, "Alfresco.SelectWithEditControl", htmlId, ["button"]);
    }

    YAHOO.extend(Alfresco.SelectWithEditControl, Alfresco.component.Base, {

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
            responseType: YAHOO.util.DataSource.TYPE_JSARRAY,

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
             * Currently selected item
             */
            selectedItem: null

        },

        onReady: function() {

            this.dataSource = new YAHOO.util.XHRDataSource(this.options.optionsUrl);
            this.dataSource.responseType = this.options.responseType;
            if(this.options.responseSchema) {
                this.dataSource.responseSchema = this.options.responseSchema;
            } else {
                this.dataSource.responseSchema = {
                    fields: [ this.options.valueField, this.options.titleField ]
                };
            }
            this.dataSource.sendRequest(this.options.requestParam, {
                success: this.options.mode != "view" ? this.onLoadSuccess : this.onLoadSuccessView,
                failure: this.onLoadFailure,
                scope: this
            });
        },

        onLoadSuccess: function(request, response, payload) {
            var me =this;

            YAHOO.util.Event.addListener(
                this.id + "-editbox",
                "keyup", function() {
                    var target = Dom.get(me.id);
                    target.setAttribute('value', Dom.get(this.id).value);
                }
            );

            YAHOO.util.Event.addListener(
                this.id + "-editbox",
                "mousedown", function() {
                    var target = Dom.get(me.id);
                    target.setAttribute('value', Dom.get(this.id).value);
                }
            );

            YAHOO.util.Event.addListener(
                this.id + "-select",
                "click", function() {
                    if(Dom.get(this.id).value != 'other') {
                        var target = Dom.get(me.id);
                        target.setAttribute('value', Dom.get(this.id).value);
                    }
                }
            );
            var selectedValue = this.options.selectedItem || this.options.originalValue;
            Dom.get(this.id).value = selectedValue;

            var select = Dom.get(this.id + '-select');
            var option = document.createElement('OPTION');
            var selectedExist = false;
            for(var i in response.results) {
                if(!response.results.hasOwnProperty(i)) continue;
                var item = response.results[i],
                    value = item[this.options.valueField],
                    title = item[this.options.titleField] || value,
                    selected = value == selectedValue;
                option = document.createElement('OPTION');
                option.text = title;
                option.value = value;
                option.selected = selected;
                select.options.add(option);
                if (selected == true) {
                    selectedExist = true;
                }
            }
            if(!selectedExist && selectedValue != null) {
                var editbox = Dom.get(this.id + '-editbox');
                editbox.value = selectedValue == null ? "" : selectedValue;
                Dom.removeClass(this.id + '-editbox', "hidden");
            }
            option = document.createElement('OPTION');
            option.text = this.msg('form.select.other.label');
            option.value = "other";
            option.selected = !selectedExist && selectedValue;
            select.options.add(option);

            if(response.results.length == 0) {
                this.showFailureMessage(this.msg('message.no-options'));
            }
            YAHOO.Bubbling.fire("mandatoryControlValueUpdated");
        },

        onLoadSuccessView: function(request, response, payload) {
            var container = Dom.get(this.id);
            var selectedValue = this.options.selectedItem || this.options.originalValue;
            for(var i in response.results) {
                var item = response.results[i],
                    value = item[this.options.valueField],
                    title = item[this.options.titleField] || value,
                    selected = value == selectedValue;
                if(selected == true) {
                    container.innerHTML = title;
                }
            }
        },

        onLoadFailure: function() {
            this.showFailureMessage(this.msg('message.load-failed'));
        },

        showFailureMessage: function(message) {
            Dom.addClass(Dom.get(this.id), "hidden");
            Dom.get(this.id+"-error").innerHTML = message;
        }

    });

})();
