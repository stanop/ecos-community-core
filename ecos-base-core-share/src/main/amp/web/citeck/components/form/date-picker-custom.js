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

    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event,
        KeyListener = YAHOO.util.KeyListener;

    var $html = Alfresco.util.encodeHTML;

    Alfresco.DatePickerCustom = function(htmlId, valueHtmlId) {
        Alfresco.DatePickerCustom.superclass.constructor.call(this, htmlId, valueHtmlId);
        return this;
    };

    YAHOO.extend(Alfresco.DatePickerCustom, Alfresco.DatePicker, {

        onReady: function DatePicker_onReady()
        {
            var theDate = null;

            // calculate current date
            if (this.options.currentValue !== null && this.options.currentValue !== "")
            {
                theDate = Alfresco.util.fromISO8601(this.options.currentValue);
            }
            else
            {
                theDate = new Date();
            }

            var page = (theDate.getMonth() + 1) + "/" + theDate.getFullYear();
            var selected = (theDate.getMonth() + 1) + "/" + theDate.getDate() + "/" + theDate.getFullYear();
            var dateEntry = theDate.toString(this._msg("form.control.date-picker.entry.date.format"));
            var timeEntry = theDate.toString(this._msg("form.control.date-picker.entry.time.format"));

            // populate the input fields
            if (this.options.currentValue !== "") {
                // show the formatted date
                Dom.get(this.id + "-date").value = dateEntry;

                if (this.options.showTime)
                {
                    Dom.get(this.id + "-time").value = timeEntry;
                }
            } else if (this.options.defaultTimeValue) {
                Dom.get(this.id + "-time").value = this.options.defaultTimeValue;
            }


            // construct the picker
            this.widgets.calendar = new YAHOO.widget.Calendar(this.id, this.id, { title:this._msg("form.control.date-picker.choose"), close:true, navigator:true });
            this.widgets.calendar.cfg.setProperty("pagedate", page);
            this.widgets.calendar.cfg.setProperty("selected", selected);
            Alfresco.util.calI18nParams(this.widgets.calendar);

            // setup events
            this.widgets.calendar.selectEvent.subscribe(this._handlePickerChange, this, true);
            this.widgets.calendar.hideEvent.subscribe(function()
            {
                // Focus icon after calendar is closed
                Dom.get(this.id + "-icon").focus();
            }, this, true);
            Event.addListener(this.id + "-date", "keyup", this._handleFieldChange, this, true);
            Event.addListener(this.id + "-time", "keyup", this._handleFieldChange, this, true);

            var iconEl = Dom.get(this.id + "-icon");
            if (iconEl)
            {
                // setup keyboard enter events on the image instead of the link to get focus outline displayed
                Alfresco.util.useAsButton(iconEl, this._showPicker, null, this);
                Event.addListener(this.id + "-icon", "click", this._showPicker, this, true);
            }


            // register a validation handler for the date entry field so that the submit
            // button disables when an invalid date is entered
            YAHOO.Bubbling.fire("registerValidationHandler",
                {
                    fieldId: this.id + "-date",
                    handler: Alfresco.forms.validation.validDateTime,
                    when: "keyup"
                });

            // register a validation handler for the time entry field (if applicable)
            // so that the submit button disables when an invalid date is entered
            if (this.options.showTime)
            {
                YAHOO.Bubbling.fire("registerValidationHandler",
                    {
                        fieldId: this.id + "-time",
                        handler: Alfresco.forms.validation.validDateTime,
                        when: "keyup"
                    });
            }

            // render the calendar control
            this.widgets.calendar.render();

            // If value was set in visible fields, make sure they are validated and put in the hidden field as well
            if (this.options.currentValue !== "")
            {
                this._handleFieldChange(null);
            }
        }


    });

})();