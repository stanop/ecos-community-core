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

    Citeck.TextRichSearch = function TRS_constructor(htmlId) {
        Citeck.TextRichSearch.superclass.constructor.call(this, "Citeck.TextRichSearch", htmlId, ["button", "menu", "container", "resize", "datasource", "datatable"]);
        this.valueHtmlId = htmlId;
        this.eventGroup = htmlId;
        this.controlId = htmlId + '-cntrl';
        return this;
    };

    YAHOO.extend(Citeck.TextRichSearch, Alfresco.component.Base, {

        options: {
            multipleSelectMode: null,
            field: null,
            mandatory: null,
            valueType: null,
            currentValue: null,
            formMode: null,
            defaultMode: "match_string",
            hideSelect: null
        },

        modeDefinition: {
            "match_string": { prefix: "-string-match", label: Alfresco.util.message("button.searchContains") },
            "empty": { prefix: "-string-empty", label: Alfresco.util.message("button.searchEmpty") },
            "not_empty": { prefix: "-string-notempty", label: Alfresco.util.message("button.searchNotEmpty") },
            "equal": { prefix: "-string-equal", label: Alfresco.util.message("button.searchEquals") },
            "not_equal": { prefix: "-string-notequal", label: Alfresco.util.message("button.searchNotEquals") }
        },

        onReady: function TRS_onReady() {
            this.widgets.searchModeButton = new YAHOO.widget.Button(this.controlId + "-search-mode-button", {
                type: "menu",
                menu: this.controlId + "-search-mode-select"
            });
            if(this.options.hideSelect=="true")
            {
                this.widgets.searchModeButton.setStyle("visibility","hidden");
            }
            this.widgets.searchModeButton.on("selectedMenuItemChange", this.onSelectSearchMode());
            this._setSearchMode(this.options.defaultMode);
        },

        onSelectSearchMode: function TRS_onSelectSearchMode(event) {
            var me = this;
            return function(event) {
                var oMenuItem = event.newValue;
                var mode = oMenuItem.value;
                me._setSearchMode(mode);
                return;
            }
        },

        _setSearchMode: function TRS_onSelectSearchMode(mode) {
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