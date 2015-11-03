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

    Citeck.NumberField = function Citeck_NumberField(htmlId) {
        Citeck.NumberField.superclass.constructor.call(this, "Citeck.NumberField", htmlId);
        return this;
    };

    YAHOO.extend(Citeck.NumberField, Alfresco.component.Base, {

        options: {
            round: null,
            value: null,
            formMode: null
        },

        onReady: function NF_onReady() {
            if (this.options.value && this.options.value != 'NaN') {
                var value = this.options.value;
                if (this.options.round && this.options.round != 'NaN') {
                    value = this.roundPlus(
                        value + '',
                        this.options.round
                    );
                }
                if ('view' == this.options.formMode) {
                    Dom.get(this.id).innerHTML = value;
                } else {
                    Dom.get(this.id).value = value;
                }
            }
        },

        roundPlus: function NF_roundPlus(number, n) {
            number = number.replace(/,/g, '.');
            var m = Math.pow(10, n);
            var result = Math.round(number*m)/m;
            return result;
        }

    });

})();