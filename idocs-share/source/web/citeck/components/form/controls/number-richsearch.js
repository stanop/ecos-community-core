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

    Citeck.NumberRichSearch = function NRS_constructor(htmlId) {
        Citeck.NumberRichSearch.superclass.constructor.call(this, "Citeck.NumberRichSearch", htmlId, ["button", "menu", "container", "resize", "datasource", "datatable"]);
        this.valueHtmlId = htmlId;
        this.eventGroup = htmlId;
        this.controlId = htmlId + '-cntrl';
        return this;
    };

    YAHOO.extend(Citeck.NumberRichSearch, Alfresco.component.Base, {

        options: {
            multipleSelectMode: null,
            field: null,
            mandatory: null,
            valueType: null,
            currentValue: null,
            formMode: null,
            defaultMode: "equal"
        },

        modeDefinition: {
            "equal": { prefix: "-number-equal", label: " = "+Alfresco.util.message("button.searchEqual") },
            "not_equal": { prefix: "-number-notequal", label: " != "+Alfresco.util.message("button.searchNotEqual") },
            "more_than_exclusive": { prefix: "-number-more-than-exclusive", label: " > "+Alfresco.util.message("button.searchMoreThanExclusive") },
            "more_than_inclusive": { prefix: "-number-more-than-inclusive", label: " >= "+Alfresco.util.message("button.searchMoreThanInclusive") },
            "less_than_exclusive": { prefix: "-number-less-than-exclusive", label: " < "+Alfresco.util.message("button.searchLessThanExclusive") },
            "less_than_inclusive": { prefix: "-number-less-than-inclusive", label: " <= "+Alfresco.util.message("button.searchLessThanInclusive")  },
            "empty": { prefix: "-number-empty", label: Alfresco.util.message("button.searchEmpty") },
            "not_empty": { prefix: "-number-notempty", label: Alfresco.util.message("button.searchNotEmpty") }
        },

        onReady: function NRS_onReady() {
            this.widgets.searchModeButton = new YAHOO.widget.Button(this.controlId + "-search-mode-button", {
                type: "menu",
                menu: this.controlId + "-search-mode-select"
            });
            this.widgets.searchModeButton.on("selectedMenuItemChange", this.onSelectSearchMode());
            this._setSearchMode(this.options.defaultMode);
        },

        onSelectSearchMode: function NRS_onSelectSearchMode(event) {
            var me = this;
            return function(event) {
                var oMenuItem = event.newValue;
                var mode = oMenuItem.value;
                me._setSearchMode(mode);
                return;
            }
        },

        _setSearchMode: function NRS_onSelectSearchMode(mode) {
            this.widgets.searchModeButton.set("value", this.modeDefinition[mode].label);
            this.widgets.searchModeButton.set("label", this.modeDefinition[mode].label);
            var fieldName = this.options.field + this.modeDefinition[mode].prefix;
            $("#" + this.id).attr("name", fieldName);
            if (mode == "empty" || mode == "not_empty") {
                Dom.get(this.id).value = "";
            }
        }

    });

})();