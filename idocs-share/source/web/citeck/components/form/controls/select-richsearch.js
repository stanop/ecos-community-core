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

if (typeof Citeck == "undefined" || !Citeck) {
    var Citeck = {};
}


(function() {

    var Dom = YAHOO.util.Dom;

    Citeck.SelectRichSearch = function SRS_constructor(htmlId) {
        Citeck.SelectRichSearch.superclass.constructor.call(this, "Citeck.SelectRichSearch", htmlId, ["button", "menu", "container", "resize", "datasource", "datatable"]);
        this.valueHtmlId = htmlId;
        this.controlId = htmlId + '-cntrl';
        return this;
    };

    YAHOO.extend(Citeck.SelectRichSearch, Alfresco.component.Base, {

        options: {
            field: null,
            mandatory: null,
            currentValue: null,
            formMode: null,
            defaultMode: "any"
        },

        modeDefinition: {
            "equal": { prefix: "-string-equal", label: Alfresco.util.message("button.searchValue") },
            "notequal": { prefix: "-string-notequal", label: Alfresco.util.message("button.searchExeptValue") },
            "empty": { prefix: "-string-empty", label: Alfresco.util.message("button.searchEmpty") },
            "not_empty": { prefix: "-string-notempty", label: Alfresco.util.message("button.searchNotEmpty") },
            "any": { prefix: "-string-match", label: Alfresco.util.message("button.any") }
        },

        onReady: function SRS_onReady() {
            this.widgets.searchModeButton = new YAHOO.widget.Button(this.controlId + "-search-mode-button", {
                type: "menu",
                menu: this.controlId + "-search-mode-select"
            });
            this.widgets.searchModeButton.on("selectedMenuItemChange", this.onSelectSearchMode());
            YAHOO.util.Event.addListener(Dom.get(this.id + '-value-selectbox'), "change", this.onSelectValueChange());
            this._setSearchMode(this.options.defaultMode);
        },

        onSelectSearchMode: function SRS_onSelectSearchMode() {
            var me = this;
            return function(event) {
                var oMenuItem = event.newValue;
                var mode = oMenuItem.value;
                me._setSearchMode(mode);
            }
        },

        onSelectValueChange: function SRS_onSelectValueChange() {
            var me = this;
            return function(event) {
                Dom.get(me.id).value = Dom.get(me.id + '-value-selectbox').value
            }
        },
        
        _setSearchMode: function SRS_onSelectSearchMode(mode) {
            this.widgets.searchModeButton.set("value", this.modeDefinition[mode].label);
            this.widgets.searchModeButton.set("label", this.modeDefinition[mode].label);
            Dom.get(this.id).value = Dom.get(this.id + '-value-selectbox').value;
            var fieldName = this.options.field + this.modeDefinition[mode].prefix;
            $("#" + this.id).attr("name", fieldName);
            $('#' + this.id).prop('disabled', 'any' == mode);
            $('#' + this.id + '-value-selectbox').prop('disabled', 'empty' == mode || 'not_empty' == mode || 'any' == mode);
            if ('empty' == mode || 'not_empty' == mode || 'any' == mode) {
                $('#' + this.id + '-value-selectbox').hide();
            } else {
                $('#' + this.id + '-value-selectbox').show();
            }
        }

    });

})();