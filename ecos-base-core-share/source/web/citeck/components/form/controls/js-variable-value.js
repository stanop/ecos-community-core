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

    Citeck.JSVariableField = function Citeck_JSVariableField(htmlId) {
        Citeck.JSVariableField.superclass.constructor.call(this, "Citeck.JSVariableField", htmlId);
        return this;
    };

    YAHOO.extend(Citeck.JSVariableField, Alfresco.component.Base, {

        options: {
            variableName: null,
            formMode: null
        },

        value: null,

        onReady: function JSVF_onReady() {
            if (this.options.variableName) {
                this.value = eval('(' + this.options.variableName + ')');
            }

            if ('view' == this.options.formMode) {
                //NOT SUPPORTED
            } else {
                if (this.value) {
                    Dom.get(this.id).value = this.value;
                }
            }
        }

    });

})();