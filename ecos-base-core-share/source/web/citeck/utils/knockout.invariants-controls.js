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
define(['lib/knockout', 'citeck/utils/knockout.utils', 'citeck/components/invariants/invariants', 'citeck/components/journals2/journals'], function(ko, koutils, invariants, journals) {

// ----------------
// GLOBAL FUNCTIONS 
// ----------------

var $html = Alfresco.util.encodeHTML,
    $combine = Alfresco.util.combinePaths,
    koclass = koutils.koclass;

var Event = YAHOO.util.Event,
    Dom = YAHOO.util.Dom;


// TODO: refactoring
// - integrate the calendar into a single function for the date and datetime controls

// ---------------
// NUMBER-GENERATE
// ---------------

ko.components.register("number-generate", {
    viewModel: function(params) {
        var self = this;
        this.id = params.id;
        this.label = params.label || "Generate";
        this.mode = params.mode;
        this.disable = params.disable;

        this.generate = function() { 
            var generator = ko.computed(function() { 
                return params.enumeration.getNumber(params.template, params.node()); 
            }, this, { deferEvaluation: true });

            var number = generator();
            if (number) {
                params.value(number);
                generator.dispose();
            } else {
                koutils.subscribeOnce(generator, function(number) { 
                    params.value(number);
                    generator.dispose();
                });
            } 
        };

        // flag for 'checkbox' mode
        this.flag = ko.observable(false);
        this.flag.subscribe(function(flag) {
            if (flag) {
                self.generate();
                Dom.setAttribute(self.id, "disabled", "disabled");
            } else {
                var input = Dom.get(self.id);
                if (input) input.removeAttribute("disabled");
            }
        });

        // define mode
        this.isButtonMode = this.mode == "button";
        this.isCheckboxMode = this.mode == "checkbox";
    },
    template: 
       '<!-- ko if: isButtonMode -->\
            <button data-bind="text: label, disable: disable, click: generate"></button>\
        <!-- /ko -->\
        <!-- ko if: isCheckboxMode -->\
            <input style="position: relative; top: 2px;" type="checkbox" name="number-generate" data-bind="checked: flag" />\
            <label style="margin-left: 10px;" data-bind="text: label"></label>\
        <!-- /ko -->\
        '
});

// ---------------
// NUMBER
// ---------------

ko.components.register("number", {
    viewModel: function(params) {
        var self = this;

        this.disable = params.disable;
        this.id = params.id;
        this.step = params.step && _.isNumber(params.step) ? params.step : "any";
        this.value = params.value;
        this.onlyNumbers = params.onlyNumbers && _.isBoolean(params.onlyNumbers) ? params.onlyNumbers : false;

        this.koValidation = function(data, event) {
            if (Citeck.HTML5.supportInput("number")) {
                return _.contains([43, 44, 45, 46, 101, 69, 188, 190, 110], event.keyCode) ? false : true;
            } else {
                return event.keyCode >= 48 && event.keyCode <= 57;
            }; 
        }
    },
    template: 
       '<!-- ko if: onlyNumbers -->\
            <input type="number"\
                data-bind="textInput: value, disable: disable, attr: { id: id, step: step }, event: { keypress: koValidation }" />\
        <!-- /ko -->\
        <!-- ko ifnot: onlyNumbers -->\
            <input type="number" data-bind="textInput: value, disable: disable, attr: { id: id, step: step }" />\
        <!-- /ko -->'
});

// ---------------
// FREE-CONTENT
// ---------------

ko.components.register("free-content", {
    viewModel: function(params) {
        var self = this;
        this.func = params.func;

        if (!this.func) {
            throw Error('Parameter "func" should by specified')
        }

        this.content = ko.computed(function() {
            var result = self.func();
            if (result instanceof HTMLElement) return result.outerHTML;
            if (result instanceof String) return result;

            throw Error('Parameter "func" should return a String or an HTMLElement');
            return null;
        });
    },
    template: 
       '<div data-bind="html: content">'
})

// ---------------
// CHECKBOX
// ---------------

ko.components.register("checkbox-radio", {
    viewModel: function(params) {
        var self = this;

        this.groupName = params["groupName"];
        this.optionText = params["optionText"];
        this.options = params["options"];
        this.value = params["value"];
        this.multiple = params["multiple"] || false;
    },
    template: 
        '<!-- ko foreach: options -->\
            <span class="checkbox-option">\
                <label>\
                    <!-- ko if: $parent.multiple -->\
                      <input type="checkbox" data-bind="checked: ko.computed({\
                        read: function() { if ($parent.value()) return $parent.value().indexOf($data) != -1; },\
                        write: function(newValue) {\
                          var selectedOptions = $parent.value() || [];\
                          newValue ? selectedOptions.push($data) : selectedOptions.splice(selectedOptions.indexOf($data), 1);\
                          $parent.value(selectedOptions);\
                        }\
                      })" />\
                    <!-- /ko -->\
                    <!-- ko ifnot: $parent.multiple -->\
                      <input type="radio" data-bind="checked: ko.computed({\
                        read: function() { if ($parent.value()) return $parent.value().id; },\
                        write: function(newValue) { $parent.value($data.nodeRef); }\
                      }), attr: { value: $data.id, name: $parent.groupName }" />\
                    <!-- /ko -->\
                    <!-- ko text: $parent.optionText($data) --><!-- /ko -->\
                </label>\
            </span>\
        <!-- /ko -->'
});

// ---------------
// DATETIME
// ---------------

ko.components.register("datetime", {
    viewModel: function(params) {
        var self = this,
            calendarAccessorId = params.fieldId + "-calendarAccessor",
            calendarDialogId = params.fieldId + "-calendarDialog", calendarContainerId = params.fieldId + "-calendarContainer", 
            calendarDialog, calendar;
            localization = params.localization;

        this.fieldId = params["fieldId"];
        this.value = params["value"];
        this.disabled = params["protected"];

        this.calendar = function() {
            if (!calendarDialog) {
                calendarDialog = new YAHOO.widget.Dialog(calendarDialogId, { 
                    visible:    false, 
                    context:    [calendarAccessorId, "tl", "bl"], 
                    draggable:  false, 
                    close:      true,
                    zindex:     15
                });
                calendarDialog.setHeader(localization.labels.header);
                calendarDialog.setBody("<div id=\"" + calendarContainerId + "\"></div>");
                calendarDialog.render(document.body); 
            }

            if (!calendar) {
                calendar = new YAHOO.widget.Calendar(calendarContainerId, { 
                    LOCALE_WEEKDAYS: "short",
                    LOCALE_MONTHS: "long",
                    START_WEEKDAY: 1,

                    iframe: false, 
                    navigator: {
                        strings: {
                            month:  localization.labels.month,
                            year:   localization.labels.year,
                            submit: localization.buttons.submit,
                            cancel: localization.buttons.cancel
                        }
                    }
                }); 

                // localization months and days
                calendar.cfg.setProperty("MONTHS_LONG", localization.months.split(",")); 
                calendar.cfg.setProperty("WEEKDAYS_SHORT", localization.days.split(","));

                // selected date
                calendar.selectEvent.subscribe(function() {
                    if (calendar.getSelectedDates().length > 0) {
                        var selectedDate = calendar.getSelectedDates()[0],
                            nowDate = new Date;

                        selectedDate.setHours(nowDate.getHours());
                        selectedDate.setMinutes(nowDate.getMinutes());

                        self.value(selectedDate);
                    }
                    calendarDialog.hide();
                });

                calendar.render();      
            }

            if (calendarDialog) calendarDialog.show();
        };

        this.textValue = ko.pureComputed({
            read: function() { return self.value() ? self.value().toLocaleString() : null },
            write: function(newValue) {
                if (newValue) {
                    if (/\d{2}.\d{2}.\d{2,4}(, | )\d{2}:\d{2}(:\d{2}|)/.test(newValue)) {
                        var timeArray = newValue.split(/, | /);
                        timeArray[0] = timeArray[0].split(".").reverse().join("/");

                        var newDate = new Date(timeArray.join(", "));
                        if (newDate != "Invalid Date") {
                            self.value(newDate);
                            return;
                        }
                    }
                } 
                
                if (self.value() != null) self.value(null)
            }
        });

        this.dateValue = ko.computed({
            read: function() { 
                return  Alfresco.util.toISO8601(self.value(), { milliseconds: false, hideTimezone: true });
            },
            write: function(newValue) { 
                if (newValue) {
                    var newDate = new Date(newValue);

                    if (newDate != "Invalid Date") {
                        newDate.setFullYear(newDate.getUTCFullYear());
                        newDate.setMonth(newDate.getUTCMonth());
                        newDate.setDate(newDate.getUTCDate());
                        newDate.setHours(newDate.getUTCHours());
                        newDate.setMinutes(newDate.getUTCMinutes());

                        self.value(newDate);
                        return;
                    }
                } 

                if (self.value() != null) self.value(null)
            }
        });
    },
    template: 
       '<!-- ko if: Citeck.HTML5.supportInput("datetime-local") -->\
            <input type="datetime-local" data-bind="value: dateValue, disable: disabled" />\
        <!-- /ko -->\
        <!-- ko ifnot: Citeck.HTML5.supportInput("datetime-local") -->\
            <input type="text" data-bind="value: textValue, disable: disabled" />\
            <!-- ko if: disabled -->\
                <img src="/share/res/components/form/images/calendar.png" class="datepicker-icon">\
            <!-- /ko -->\
            <!-- ko ifnot: disabled -->\
                <a class="calendar-link-button" data-bind="disable: disabled, click: calendar, clickBubble: false, attr: { id: fieldId + \'-calendarAccessor\' }">\
                    <img src="/share/res/components/form/images/calendar.png" class="datepicker-icon">\
                </a>\
            <!-- /ko -->\
        <!-- /ko -->'
});


// ---------------
// DATE
// ---------------

ko.bindingHandlers.dateControl = {
    init: function(element, valueAccessor, allBindings) {
        var value = valueAccessor(),
            params = allBindings();

        var localization = params.localization;

        var elementId = element.id.replace("-dateControl", ""),
            input = Dom.get(elementId);

        if (!Citeck.HTML5.supportedInputTypes.date) {
            input.setAttribute("placeholder", localization.placeholder);

            var calendarDialogId = elementId + "-calendarDialog",
                calendarContainerId = elementId + "-calendarContainer",
                calendarAccessorId = elementId + "-calendarAccessor",
                calendarDialog, calendar;

            var showCalendarButton = document.getElementById(calendarAccessorId);
            showCalendarButton.classList.remove("hidden");

            Event.on(showCalendarButton, "click", function() {
                if (!calendarDialog) {
                    calendarDialog = new YAHOO.widget.Dialog(calendarDialogId, { 
                        visible:    false, 
                        context:    [calendarAccessorId, "tl", "bl"], 
                        draggable:  false, 
                        close:      true,
                        zindex:     15
                    });
                    calendarDialog.setHeader(localization.labels.header);
                    calendarDialog.setBody("<div id=\"" + calendarContainerId + "\"></div>");
                    calendarDialog.render(document.body); 
                }

                if (!calendar) {
                    calendar = new YAHOO.widget.Calendar(calendarContainerId, { 
                        LOCALE_WEEKDAYS: "short",
                        LOCALE_MONTHS: "long",
                        START_WEEKDAY: 1,

                        iframe: false, 
                        navigator: {
                            strings: {
                                month:  localization.labels.month,
                                year:   localization.labels.year,
                                submit: localization.buttons.submit,
                                cancel: localization.buttons.cancel
                            }
                        }
                    }); 

                    // localization months and days
                    calendar.cfg.setProperty("MONTHS_LONG", localization.months.split(",")); 
                    calendar.cfg.setProperty("WEEKDAYS_SHORT", localization.days.split(","));

                    // selected date
                    calendar.selectEvent.subscribe(function() {
                        if (calendar.getSelectedDates().length > 0) {
                            var selectedDate = calendar.getSelectedDates()[0],
                                dStr = selectedDate.getDate(),
                                mStr = selectedDate.getMonth(),
                                yStr = selectedDate.getFullYear();
                                
                            value(selectedDate.toString("yyyy-MM-dd"));                           
                        }
                        calendarDialog.hide();
                    });

                    calendar.render();      
                }

                if (calendarDialog) calendarDialog.show();
            });
        } else {
            // set max and min attributes
            var date = new Date(), 
                year = date.getFullYear()

            input.setAttribute("max", (year + 50) + "-12-31");
            input.setAttribute("min", (year - 25) + "-12-31");

            Dom.setStyle(input, "color", "lightgray");
            value.subscribe(function(value) {
              Dom.setStyle(input, "color", value ? "" : "lightgray");
            });
        }
    }
};


// -------------
// JOURNAL
// -------------


// TODO:
// - by default searchable columns  in parameters
// - hide drop down menu out block
// - progress bar for download fields of criteria

ko.components.register('value-of-selected-criterion', {
    viewModel: function(params) {
        var self = this;

        self.htmlId  = params.htmlId;
        self.itemId  = params.itemId;
        self.fieldId = params.field;
        self.name    = params.name;
        self.value   = params.value;

        self.template = ko.observable();

        var URL = Alfresco.constants.URL_PAGECONTEXT + "citeck/components/form-control?htmlid=" + self.htmlId + "&itemKind=type&itemId=" + self.itemId + "&field=" + self.fieldId + "&name=" + self.name;

        YAHOO.util.Connect.asyncRequest('GET', URL, {
            success: function(response) {
                var result = response.responseText;

                // prepared result
                result = prepareResultString(result, self.htmlId)

                // support value bindings
                if(self.name) {
                    result = _.reduce(
                        [ 
                            'name="' + self.name + '"', 
                            "name='" + self.name + "'", 
                            'name="' + self.name + '_added"', 
                            "name='" + self.name + "_added'"
                        ], 
                        function(html, pattern) { 
                            return html.replace(new RegExp('(' + pattern + ')', 'gi'), '$1 data-bind="value: value"') 
                        }, 
                        result
                    );
                }

                self.template(result);

                var fieldContainer = Dom.get(self.htmlId);
                if (fieldContainer) {
                    ko.cleanNode(fieldContainer);
                    ko.applyBindings({ value: self.value }, fieldContainer);
                }
            },

            failure: function(response) {
                // error
            }
        });
    },
    template: 
        '<div data-bind="html: template"></div>'
});

function deleteLabelFromField(fieldString) {
    if (fieldString && typeof fieldString == "string") return fieldString.replace(/<label.+\/label>/, "");
    return fieldString;
}

function prepareResultString(fieldString, fieldId) {
    var container = $("<div>", { html: fieldString }),
        field = $(".form-field", container);

    // set id
    if (field) field.attr("id", fieldId);

    // remove label
    $("label", field).remove();

    return container.html();
}

ko.components.register('list-of-selected-criterion', {
    viewModel: function(params) {
        var self = this;

        self.htmlId                 = params.htmlId;
        self.itemId                 = params.itemId;
        self.journalType            = params.journalType;
        self.selectedFilterCriteria = params.selectedFilterCriteria;
        self.defaultFilterCriteria  = params.defaultFilterCriteria;

        self.remove = function(data, event) {
            self.selectedFilterCriteria.remove(data);
        };
    },
    template: 
       '<table class="selected-criteria-list">\
            <tbody>\
                <!-- ko foreach: selectedFilterCriteria -->\
                    <tr>\
                        <td class="action-col"><a class="remove-selected-criterion" data-bind="click: $component.remove">X</a></td>\
                        <td class="name-col"><span class="selected-criterion-name" data-bind="text: displayName"></span></td>\
                        <td class="predicate-col" data-bind="with: datatype">\
                            <select class="predicate" data-bind="options: predicates,\
                                                                 optionsText: \'label\',\
                                                                 optionsValue: \'id\',\
                                                                 value: $parent.predicateValue"></select>\
                        </td>\
                        <td class="value-col">\
                            <!-- ko component: { name: \'value-of-selected-criterion\',\
                                params: {\
                                    htmlId: $component.htmlId + \'_\'  + $index(),\
                                    itemId: $component.itemId,\
                                    name: "criterionValue_" + $index(),\
                                    field: name(),\
                                    value: value\
                                }\
                            } --><!-- /ko -->\
                        </td>\
                    </tr>\
                <!-- /ko -->\
            </tbody>\
        </table>'
});


// TODO: 
// - right name for field if it node


ko.components.register('journal', {
    viewModel: function(params) {
        if (!params.sourceElements && !params.journalType) {
            throw new Error("Required parameters are missing");
            return;
        }

        var self = this;

        // required params
        self.sourceElements = params.sourceElements;
        self.targetElements = params.targetElements;
        self.journalType    = params.journalType;
        self.page           = params.page;
        self.callback       = params.callback;
        self.loading        = params.loading;
        self.columns        = params.columns;
        self.hidden         = params.hidden;

        // options
        self.options = {
            multiple: false,  
            pagination: false,
            localization: { 
                nextPageLabel: "-->", 
                nextPageTitle: "-->", 
                previousPageLabel: "<--", 
                previousPageTitle: "<--" 
            } 
        };
        concatOptions(self.options, params.options);

        // computed
        self.sortedElements = ko.computed(function() {
            var elements = self.sourceElements(),
                pagination = elements ? elements.pagination : undefined;

            if (self.options.sortBy && self.options.sortBy()) {
                if (elements.length > 0) {
                    var assocArray = _.map(elements, function(item) { 
                        return { data: item, key: item.properties[self.options.sortBy()] }; 
                    });

                    var order = "ASC";
                    if (self.options.orderBy && self.options.orderBy()) order = self.options.orderBy();

                    switch (order) {
                        case "ASC": 
                            assocArray.sort(function(a, b) { return a.key > b.key; });
                            break;

                        case "DESC":
                            assocArray.sort(function(a, b) { return a.key < b.key; });
                            break;
                    };

                    elements = _.map(assocArray, function(item) { return item.data; });
                    elements.pagination = pagination;
                }
            }

            if (self.hidden && self.hidden.length > 0) {
                return _.filter(elements, function(item) {
                   return !_.contains(self.hidden, item.type) && !_.contains(self.hidden, item.typeShort);
                });
            }

            return elements;
        });

        // methods
        self.selectElement = function(data, event) {
            if (self.targetElements) {
                if (self.options.multiple && (ko.isObservable(self.options.multiple) ? self.options.multiple() : self.options.multiple)) {
                    if (self.targetElements.indexOf(data) == -1) self.targetElements.push(data);
                } else {
                    self.targetElements([data]);
                }
            };

            if (self.callback) self.callback(data, event);
        };

        self.nextPage = function(data, event) {
            self.page(self.page() + 1);
        };

        self.previousPage = function(data, event) {
            self.page(self.page() - 1);
        };

        self.displayText = function(value, attr) {
            if (value) {
                // if string
                if (typeof value == "string") {
                    if (attr.labels() && attr.labels()[value]) 
                        return attr.labels()[value];
                }

                // if object
                if (typeof value == "object") {
                    if (isInvariantsObject(value)) return value.name 
                }

                return value;
            }

            return null;
        };
    },
    template:
       '<!-- ko if: loading -->\
            <div class="loading"></div>\
        <!-- /ko -->\
        <table class="journal">\
            <thead>\
                <!-- ko if: columns ? true : false -->\
                    <tr data-bind="foreach: columns">\
                        <!-- ko if: $component.journalType.attribute($data) -->\
                            <!-- ko with: $component.journalType.attribute($data) -->\
                                <th data-bind="text: displayName"></th>\
                            <!-- /ko -->\
                        <!-- /ko -->\
                    </tr>\
                <!-- /ko -->\
                <!-- ko ifnot: columns ? true : false -->\
                    <tr data-bind="foreach: $component.journalType.defaultAttributes">\
                        <th data-bind="text: displayName"></th>\
                    </tr>\
                <!-- /ko -->\
            </thead>\
            <tbody data-bind="foreach: sortedElements">\
                <!-- ko if: $component.columns ? true : false -->\
                    <tr class="journal-element" data-bind="attr: { id: nodeRef },\
                                                           foreach: $component.columns,\
                                                           click: $component.selectElement, clickBubble: false">\
                       <!-- ko if: $component.journalType.attribute($data) ? true : false -->\
                            <!-- ko with: $component.journalType.attribute($data) -->\
                                <td data-bind="text: $component.displayText($parents[1].properties[$data.name()], $data)"></td>\
                            <!-- /ko -->\
                        <!-- /ko -->\
                    </tr>\
                <!-- /ko -->\
                <!-- ko ifnot: $component.columns ? true : false -->\
                    <tr class="journal-element" data-bind="attr: { id: nodeRef },\
                                                           foreach: $component.journalType.defaultAttributes,\
                                                           click: $component.selectElement, clickBubble: false">\
                        <td data-bind="text: $component.displayText($parent.properties[$data.name()], $data)"></td>\
                    </tr>\
                <!-- /ko -->\
            </tbody>\
        </table>\
        <!-- ko if: options.pagination && sortedElements -->\
            <!-- ko with: sortedElements().pagination -->\
                <!-- ko if: ($component.page() - 1 > 0) || hasMore -->\
                    <div class="journal-pagination">\
                        <span class="previous-page">\
                            <!-- ko if: $component.page() - 1 > 0 -->\
                                <a data-bind="click: $component.previousPage,\
                                              text: $component.options.localization.previousPageLabel,\
                                              attr: { title: $component.options.localization.previousPageTitle }"><--</a>\
                            <!-- /ko -->\
                            <!-- ko ifnot: $component.page() - 1 > 0 -->\
                                <!-- ko text: $component.options.localization.previousPageLabel --><!-- /ko -->\
                            <!-- /ko -->\
                        </span>\
                        <span class="page-label">\
                            <span class="start-page" data-bind="text: $component.page() * maxItems - maxItems + 1"></span>\
                            <span class="dash">-</span>\
                            <span class="end-page" data-bind="text: $component.page() * maxItems"></span>\
                        </span>\
                        <span class="next-page">\
                            <!-- ko if: hasMore -->\
                                <a data-bind="click: $component.nextPage,\
                                              text: $component.options.localization.nextPageLabel,\
                                              attr: { title: $component.options.localization.nextPageTitle }">--></a>\
                            <!-- /ko -->\
                            <!-- ko ifnot: hasMore -->\
                                <!-- ko text: $component.options.localization.nextPageLabel --><!-- /ko -->\
                            <!-- /ko -->\
                        </span>\
                    </div>\
                <!-- /ko -->\
            <!-- /ko -->\
        <!-- /ko -->'
});

ko.bindingHandlers.journalControl = {
  init: function(element, valueAccessor, allBindings, data, context) {
    var self = this;

    //  Citeck global objects
    var JournalType = koclass('JournalType');

    // html elements
    var button  = Dom.get(element.id + "-button"),
        panelId = element.id + "-journalPanel", panel;

    // binding variables
    var settings = valueAccessor(),
        value    = settings.value,
        multiple = settings.multiple,
        params   = allBindings().params();

    // sorting
    var sortBy  = ko.observable(params.sortBy),
        orderBy = ko.observable(params.orderBy || "ASC");

    // params
    var defaultVisibleAttributes    = params.defaultVisibleAttributes,
        defaultSearchableAttributes = params.defaultSearchableAttributes,
        defaultHiddenByType         = params.defaultHiddenByType,
        
        searchMinQueryLength        = params.searchMinQueryLength,
        searchScript                = _.contains(["criteria-search", "light-search"], params.searchScript) ? params.searchScript : "criteria-search",
        searchCriteria              = params.searchCriteria,
        
        localization                = params.localization;

    if (defaultVisibleAttributes) {
        defaultVisibleAttributes = _.map(defaultVisibleAttributes.split(","), function(item) { return trim(item) });
    }

    if (defaultSearchableAttributes) {
        defaultSearchableAttributes = _.map(defaultSearchableAttributes.split(","), function(item) { return trim(item) });
    }

    if (defaultHiddenByType) {
        defaultHiddenByType = _.map(defaultHiddenByType.split(","), function(item) { return trim(item) });
    }

    var selectedElements = ko.observableArray(), selectedFilterCriteria = ko.observableArray(), 
        loading = ko.observable(true), criteriaListShow = ko.observable(false), criteria = ko.observable([]), 
        journalType = params.journalType ? new JournalType(params.journalType) : null,
        searchBar = params.searchBar ? params.searchBar == "true" : true,
        mode = params.mode ? params.mode : "collapse",
        maxItems = ko.observable(10), pageNumber = ko.observable(1), skipCount = ko.computed(function() { return (pageNumber() - 1) * maxItems() }),
        options = ko.computed(function(page) { return data.filterOptions(criteria(), { maxItems: maxItems(), skipCount: skipCount() }); });

    // reset page after new search
    criteria.subscribe(function(newValue) {
        pageNumber(1);
    });

    // show loading indicator if page was changed
    pageNumber.subscribe(function(newValue) {
        loading(true);
    })

    // hide loading indicator if options got elements
    options.subscribe(function(newValue) {
        loading(_.isUndefined(newValue.pagination));
    })

    // extend notify
    criteria.extend({ notify: 'always' });
    pageNumber.extend({ notify: 'always' });
    options.extend({ notify: 'always' });

    if (!journalType) {
        // TODO: other way to get journalType
    }
    
    // get default criteria
    var defaultCriteria = ko.computed(function() {
        if (defaultSearchableAttributes) return journalType.attributes();
        return journalType.defaultAttributes();
    });

    // add default criteria to selectedFilterCriteria
    koutils.subscribeOnce(ko.computed(function() {
        selectedFilterCriteria.removeAll();
        var dc = defaultCriteria();

        if (defaultSearchableAttributes) {
            var validAttributes = [];
            for (var i = 0; i < dc.length; i++) {
                if (defaultSearchableAttributes.indexOf(dc[i].name()) != -1) validAttributes.push(dc[i]);
            }
            dc = validAttributes;
        }

        if (dc) {
            for (var i in dc) {
                var newCriterion = _.clone(dc[i]);
                newCriterion.value = ko.observable();
                newCriterion.predicateValue = ko.observable();
                selectedFilterCriteria.push(newCriterion);
            }
        }
    }), defaultCriteria.dispose);

    var submitButtonId           = panelId + "-submitInput",
        cancelButtonId           = panelId + "-cancelInput",
        elementsTabId            = panelId + "-elementsTab",
        elementsPageId           = panelId + "-elementsPage",
        filterTabId              = panelId + "-filterTab",
        filterPageId             = panelId + "-filterPage",
        createTabId              = panelId + "-createTab",
        createPageId             = panelId + "-createPage", 
        journalId                = panelId + "-elementsTable",
        selectedJournalId        = panelId + "-selectedElementsTable",
        searchId                 = panelId + "-search",
        filterCriteriaVariantsId = panelId + "-filterCriteriaVariants",
        journalPickerHeaderId    = panelId + "-journal-picker-header";

    // open dialog
    Event.on(button, "click", function(event) {
        if (!panel) {
            panel = new YAHOO.widget.Panel(panelId, {
                width:          "800px", 
                visible:        false, 
                fixedcenter:    true,  
                draggable:      true,
                modal:          true,
                zindex:         5,
                close:          true
            });

            panel.setHeader(localization.title || 'Journal Picker');
            panel.setBody('\
                <div class="journal-picker-header ' + mode + '" id="' + journalPickerHeaderId + '">\
                    <a id="' + elementsTabId + '" class="journal-tab-button ' + (mode == "collapse" ? 'hidden' : '') + ' selected">' + localization.elementsTab + '</a>\
                    <a id="' + filterTabId + '" class="journal-tab-button">' + localization.filterTab + '</a>\
                    <!-- ko component: { name: "createObjectButton", params: {\
                        scope: scope,\
                        source: \"create-views\",\
                        callback: callback,\
                        buttonTitle: buttonTitle\
                    }} --><!-- /ko -->\
                    ' + (searchBar ? '<div class="journal-search"><input type="search" placeholder="' + localization.search + '" class="journal-search-input" id="' + searchId + '" /></div>' : '') + '\
                </div>\
                <div class="journal-picker-page-container ' + mode + '">\
                    <div class="filter-page hidden" id="' + filterPageId + '">\
                        <div class="selected-filter-criteria-container">\
                            <!-- ko component: { name: \'list-of-selected-criterion\',\
                                params: {\
                                    htmlId: htmlId,\
                                    itemId: itemId,\
                                    journalType: journalType,\
                                    selectedFilterCriteria: selectedFilterCriteria,\
                                    defaultFilterCriteria: defaultFilterCriteria\
                                }\
                            } --> <!-- /ko -->\
                        </div>\
                        <div class="filter-criteria-actions">\
                            <ul>\
                                <li class="filter-criteria-option">\
                                    <a class="apply-criteria filter-criteria-button" data-bind="click: applyCriteria">' + localization.applyCriteria + '</a>\
                                </li>\
                                <li class="filter-criteria-option">\
                                    <a class="filter-criteria-button" data-bind="click: addFilterCriterion">' + localization.addFilterCriterion + '</a>\
                                    <div class="filter-criteria-variants" data-bind="visible: criteriaListShow">\
                                        <ul class="filter-criteria-list" data-bind="foreach: journalType.searchableAttributes">\
                                            <li class="filter-criteria-list-option">\
                                                <a class="filter-criterion" data-bind="text: displayName, click: $root.selectFilterCriterion"></a>\
                                            </li>\
                                        </ul>\
                                    </div>\
                                </li>\
                            </ul>\
                        </div>\
                    </div>\
                    <div class="elements-page" id="' + elementsPageId + '">\
                        <div class="journal-container" id="' + journalId + '">\
                            <!-- ko component: { name: \'journal\',\
                                params: {\
                                    sourceElements: elements,\
                                    targetElements: selectedElements,\
                                    journalType: journalType,\
                                    columns: columns,\
                                    hidden: hidden,\
                                    page: page,\
                                    loading: loading,\
                                    options: {\
                                        sortBy: sortBy,\
                                        orderBy: orderBy,\
                                        multiple: multiple,\
                                        pagination: true,\
                                        localization: {\
                                            nextPageLabel: "' + localization.nextPageLabel + '",\
                                            nextPageTitle: "' + localization.nextPageTitle + '",\
                                            previousPageLabel: "' + localization.previousPageLabel + '",\
                                            previousPageTitle: "' + localization.previousPageTitle + '"\
                                        }\
                                    },\
                                }\
                            } --><!-- /ko -->\
                        </div>\
                        <div class="journal-capture">' + localization.selectedElements + '</div>\
                        <div class="journal-container selected-elements" id="' + selectedJournalId + '">\
                            <!-- ko component: { name: \'journal\',\
                                params: {\
                                    sourceElements: selectedElements,\
                                    journalType: journalType,\
                                    columns: columns\
                                }\
                            } --><!-- /ko -->\
                        </div>\
                    </div>\
                    <div class="create-page hidden" id="' + createPageId + '"></div>\
                </div>\
            ');
            panel.setFooter('\
                <div class="buttons">\
                    <input type="submit" value="' + localization.submitButton + '" id="' + submitButtonId + '">\
                    <input type="button" value="' + localization.cancelButton + '" id="' + cancelButtonId + '">\
                </div>\
            ');

            panel.render(document.body);


            // panel submit and cancel buttons
            Event.on(submitButtonId, "click", function(event) {
                value(ko.utils.unwrapObservable(this.selectedElements));
                this.panel.hide();
            }, { selectedElements: selectedElements, panel: panel }, true);

            Event.on(cancelButtonId, "click", function(event) {
                selectedElements(value());
                this.hide();
            }, panel, true);

            // tabs listener
            Event.on(journalPickerHeaderId, "click", function(event) {
                event.stopPropagation();

                var filterTab = Dom.get(filterTabId),
                    elementsTab = Dom.get(elementsTabId);

                var filterPage = Dom.get(filterPageId), 
                    elementsPage = Dom.get(elementsPageId),
                    createPage = Dom.get(createPageId);

                if (event.target.tagName == "A") {
                    if ($(event.target).hasClass("journal-tab-button")) {
                        switch (mode) {
                            case "full":
                              $(event.target)
                                  .addClass("selected")
                                  .parent()
                                  .children()
                                  .filter(".selected:not(#" + event.target.id + ")")
                                  .removeClass("selected");

                                var pageId = event.target.id.replace(/Tab$/, "Page"),
                                    page = Dom.get(pageId);

                                $(page)
                                    .removeClass("hidden")
                                    .parent()
                                    .children()
                                    .filter("div:not(#" + pageId +")")
                                    .addClass("hidden");

                                $("button.selected", $(event.target).parent())
                                    .removeClass("selected");

                                break;

                            case "collapse":
                                // switch page if elements hidden
                                if ($(elementsPage).hasClass("hidden")) {
                                  $(createPage).addClass("hidden");
                                  $(elementsPage).removeClass("hidden");
                                }

                                // clear tab selection
                                var buttons = Dom.getElementsBy(function(element) {
                                    return element.className.indexOf("selected") != -1
                                  }, "button", journalPickerHeaderId);

                                _.each(buttons, function(element) {
                                  element.classList.remove("selected");
                                });

                                $(filterTab).toggleClass("selected");
                                $(filterPage).toggleClass("hidden");
                                break;
                        }
                    }
                }
            })

            // search listener
            if (searchBar) {
                Event.on(searchId, "keypress", function(event) {
                    if (event.keyCode == 13) {
                        event.stopPropagation();

                        var search = Dom.get(searchId);
                        if (search.value) {
                            if (searchMinQueryLength && search.value.length < searchMinQueryLength) {
                                return false;
                            }

                            if (searchCriteria && searchCriteria.length > 0) {
                                criteria(_.map(searchCriteria, function(item) {
                                    return _.defaults({ value: search.value }, item);
                                }));
                            } else {
                               criteria([{ attribute: "all", predicate: "string-contains", value: search.value }]); 
                            }
                        } else {
                            criteria([]);
                        }
                    }
                }); 
            }


            // say knockout that we have something on elements page
            ko.applyBindings({
                elements: options,
                selectedElements: selectedElements,
                multiple: multiple,
                journalType: journalType,
                page: pageNumber,
                loading: loading,
                columns: defaultVisibleAttributes,
                hidden: defaultHiddenByType,
                sortBy: sortBy,
                orderBy: orderBy
            }, Dom.get(elementsPageId));

            // say knockout that we have something on search page
            ko.applyBindings({
                htmlId: element.id,
                itemId: data.nodetype(),
                journalType: journalType,
                defaultFilterCriteria: defaultSearchableAttributes,
                selectedFilterCriteria: selectedFilterCriteria,
                criteria: criteria,
                criteriaListShow: criteriaListShow,
                selectFilterCriterion: function(data, event) {
                    // clone criterion and add value observables
                    var newCriterion = _.clone(data);
                    newCriterion.value = ko.observable();
                    newCriterion.predicateValue = ko.observable();
                    selectedFilterCriteria.push(newCriterion);

                    // hide drop-down menu
                    criteriaListShow(!criteriaListShow());
                },
                applyCriteria: function(data, event) {
                    var criteriaList = [], 
                        selectedCriteria = selectedFilterCriteria();
                    
                    if (selectedCriteria.length == 0) {
                        criteria([])
                    } else {
                        for (var i in selectedCriteria) {
                            if (selectedCriteria[i].value() && selectedCriteria[i].predicateValue() && selectedCriteria[i].name()) {
                                criteriaList.push({
                                    attribute: selectedCriteria[i].name(),
                                    predicate: selectedCriteria[i].predicateValue(),
                                    value: selectedCriteria[i].value()
                                })
                            }
                        }
                        criteria(criteriaList);
                    }
                },
                addFilterCriterion: function(data, event) {
                    criteriaListShow(!criteriaListShow());
                }
            }, Dom.get(filterPageId));

            // say knockout that we have something on create tab for create page
            ko.applyBindings({
                scope: data,
                buttonTitle: localization.createTab,
                callback: function(variant) {
                    var scCallback = function(node) {
                        if (mode == "collapse") {
                            // clear create page
                            var createPage = Dom.get(createPageId);
                            Dom.addClass(createPage, "hidden");
                            createPage.innerHTML = "";

                            // show elements page
                            var elementsPage = Dom.get(elementsPageId);
                            Dom.removeClass(elementsPage, "hidden");

                            // change tab selection
                            var buttons = Dom.getElementsBy(function(element) {
                                return element.className.indexOf("selected") != -1
                              }, "button", journalPickerHeaderId);

                            _.each(buttons, function(element) {
                            element.classList.remove("selected");
                            });
                        }
                    };

                    Citeck.forms.formContent(variant.type(), variant.formId(), {
                        response: function(response) {
                            Dom.get(createPageId).innerHTML = response;

                            // hide other pages and remove selection from other tabs
                            Dom.removeClass(elementsTabId, "selected");
                            Dom.removeClass(filterTabId, "selected");
                            Dom.addClass(elementsPageId, "hidden");
                            Dom.addClass(filterPageId, "hidden");

                            // show create page and hightlight tab
                            Dom.removeClass(createPageId, "hidden");
                            var createButton = Dom.getElementsBy(function(el) {
                                return el.tagName == "BUTTON";
                            }, "button", journalPickerHeaderId);
                            Dom.addClass(createButton, "selected");
                        },

                        submit: function(node) {
                            selectedElements(node);
                            scCallback(node);
                        },
                        cancel: scCallback
                    }, 
                    { 
                        destination: variant.destination(),
                        fieldId: data.name()
                    });

                }
            }, Dom.get(journalPickerHeaderId));
        }
        
        panel.show();
    })

    // reload filterOptions request if was created new object
    YAHOO.Bubbling.on("object-was-created", function(layer, args) {
        if (args[1].fieldId == data.name()) {

            // TODO:
            // - update table after added new node to runtime

            // dirty hack, but it's sometimes work
            loading(true);
            setTimeout(function() {
              criteria(_.clone(criteria()));
            }, 5000);
        }
    })
  }
}

// -------------
// CREATE OBJECT
// -------------

var CreateVariant = koclass('CreateVariant'),
    CreateVariantsByType = koclass('controls.CreateVariantsByType'),
    CreateVariantsByView = koclass('controls.CreateVariantsByView'),
    CreateObjectButton = koclass('controls.CreateObjectButton'),
    CreateObjectLink = koclass('controls.CreateObjectLink');

CreateVariantsByType
    .key('type', String)
    .property('createVariants', [CreateVariant])
    .load('createVariants', koutils.simpleLoad({
        url: Alfresco.constants.PROXY_URI + "api/journals/create-variants/nodetype/{type}",
        resultsMap: { createVariants: 'createVariants' }
    }))
    ;

CreateVariantsByView
    .key('type', String)
    .property('createVariants', [CreateVariant])
    .load('createVariants', koutils.simpleLoad({
        url: Alfresco.constants.PROXY_URI + "citeck/invariants/create-views?type={type}",
        resultsMap: { createVariants: 'createVariants' }
    }))
    ;
 
CreateObjectButton
    .key('id', String)
    .property('scope', Object)
    .property('constraint', Function)
    .property('constraintMessage', String)
    .property('source', String)
    .property('buttonTitle', String)
    .property('parentRuntime', String)
    .property('virtualParent', Boolean)
    .property('callback', Function)
    .computed('createVariants', function() {
        if(!this.scope().nodetype()) return [];
        var list = this.source() == 'create-views' 
            ? new CreateVariantsByView(this.scope().nodetype())
            : new CreateVariantsByType(this.scope().nodetype());
        return list.createVariants();
    })
    .method('execute', function(createVariant) {
        if (this.callback() && _.isFunction(this.callback())) {
            var callback = this.callback();
            callback(createVariant);
        } else {
            Citeck.forms.dialog(createVariant.type(), createVariant.formId(),
                {
                    scope: this, 
                    fn: function(value) {
                        if (this.constraint() && _.isFunction(this.constraint())) {                       
                            var constraint = ko.computed(this.constraint(), { 
                                oldValue: this.scope().multipleValues(), 
                                newValue: value 
                            });

                            koutils.subscribeOnce(ko.pureComputed(function() {
                                var result = constraint();
                                if (result != null || result != undefined) return result;
                            }), function(result) {
                                if (_.isBoolean(result)) {
                                    if (result) { this.scope().lastValue(value); }
                                    else { 
                                        Alfresco.util.PopupManager.displayMessage({
                                            text: this.constraintMessage() || Alfresco.util.message("create-object.message")
                                        });
                                    }
                                }
                            }, this);
                        } else {
                            this.scope().lastValue(value);
                        }

                        YAHOO.Bubbling.fire("object-was-created", { 
                            fieldId: this.scope().name(),
                            value: value
                        });
                    }
                }, 
                { 
                    title: this.buttonTitle() + ": " + createVariant.title(), 
                    destination: createVariant.destination(),
                    parentRuntime: this.parentRuntime(),
                    virtualParent: this.virtualParent()
                }
            ); 
        }
    })
    ;



ko.components.register('createObjectButton', {
    viewModel: CreateObjectButton,
    template: 
        '<!-- ko if: createVariants().length == 0 --> \
            <button class="create-object-button" disabled="disabled" data-bind="text: buttonTitle"></button> \
        <!-- /ko --> \
        <!-- ko if: createVariants().length == 1 --> \
            <button class="create-object-button" data-bind="text: buttonTitle, attr: { title: createVariants()[0].title() }, click: execute.bind($data, createVariants()[0])"></button> \
        <!-- /ko --> \
        <!-- ko if: createVariants().length > 1 --> \
            <div class="yui-overlay yuimenu button-menu" data-bind="attr: { id: id() + \'-create-menu\' }"> \
                <div class="bd"> \
                    <ul data-bind="foreach: createVariants" class="first-of-type"> \
                        <li class="yuimenuitem"> \
                            <a class="yuimenuitemlabel" data-bind="text: title, click: $parent.execute.bind($parent, $data), css: { \'default-create-variant\': isDefault }"></a> \
                        </li> \
                    </ul> \
                </div> \
            </div> \
            <span class="create" data-bind="yuiButton: { type: \'menu\', menu: id() + \'-create-menu\' }"> \
                <span class="first-child"> \
                    <button data-bind="text: buttonTitle"></button> \
                </span> \
            </span> \
        <!-- /ko -->'

});


// ------------
// AUTOCOMPLETE
// ------------

ko.components.register("autocomplete", {
    viewModel: function(params) {
        this.defaults = {
            criteria: [{ attribute: "all", predicate: "string-contains" }],
            searchScript: "criteria-search",
            minQueryLength: 3,

            // messages
            labelMessage: "Select...",
            helpMessage:  "Start typing..."
        }

        var self = this;

        this.data  = params["data"];
        this.value = this.data["singleValue"];
        this.disabled = this.data["protected"];
        this.searchScript = params["searchScript"] || self.defaults.searchScript;
        this.minQueryLength = params["minQueryLength"] || self.defaults.minQueryLength;

        // observables
        this.containerVisibility = ko.observable(false);
        this.highlightedElement = ko.observable();
        this.componentFocused = ko.observable(false);
        this.searchQuery = ko.observable();
        this.searchInput = ko.observable();
        this.searching = ko.observable(false);
        this.searchFocused = ko.observable(false);

        
        // computed
        this.label = ko.pureComputed(function() {
            var defaultLabel = params.labelMessage || self.defaults.labelMessage;
            return self.value() ? self.data.getValueTitle(self.value()) : defaultLabel; 
        });

        this.search = ko.computed(function() {
            var input = self.searchInput();
            if (input && input.length >= self.minQueryLength) {
                self.searching(true);
                self.searchQuery(input);
            }
        }).extend({ rateLimit: { timeout: 500, method: "notifyWhenChangesStop" } });

        this.criteria = ko.pureComputed(function() {
            var query = self.searchQuery();
            return _.map(params["criteria"] || self.defaults.criteria, function(item) {
                return _.defaults({ value: query }, item);
            });
        });

        this.options = ko.pureComputed(function() {
            return self.criteria().length > 0 ? self.data.filterOptions(self.criteria(), { 
                                                    maxItems: 10, 
                                                    skipCount: 0, 
                                                    searchScript: self.searchScript 
                                                }) : [];
        }).extend({ notify: 'always' });
        

        // subscription and events
        this.options.subscribe(function(newValue) {
            // highlight first element of list
            if (newValue && newValue.pagination) {
                if (newValue.length > 0) self.highlightedElement(newValue[0]);
                if (newValue.length == 0) self.searching(false);
            }
        });

        // public methods
        this.toggleContainer = function(data, event) { if (event.which == 1) self.containerVisibility(!self.containerVisibility()) };
        this.helpMessage = function() { return params.helpMessage || self.defaults.helpMessage };
        this.searchBlur = function() { self.containerVisibility(false) };

        this.selectItem = function(data, event) {
            if (event.which == 1) {
                self.value(data);
                self.containerVisibility(false);
            }
        };

        this.clear = function(data, event) { if (event.which == 1) self.value(null) };

        this.keyAction = function(data, event) {
            if ([9, 13, 27, 38, 40].indexOf(event.keyCode) != -1) {
                // apply element for value
                if (event.keyCode == 9 || event.keyCode == 13) {
                    self.value(self.highlightedElement());
                }

                // close container
                if (event.keyCode == 9 || event.keyCode == 13 || event.keyCode == 27) {
                    self.containerVisibility(false);

                    // restore focus on component
                    self.componentFocused(true);
                }

                // move selection
                if (event.keyCode == 38 || event.keyCode == 40) {
                    var selectedIndex = self.options().indexOf(self.highlightedElement()),
                        nextSelectIndex = event.keyCode == 38 ? selectedIndex - 1 : selectedIndex + 1;
                    if (selectedIndex != -1 && self.options()[nextSelectIndex]) { self.highlightedElement(self.options()[nextSelectIndex]) };
                }

                return false;
            }

            return true;
        };

        this.keyManagment = function(data, event) {
            if ([40, 46].indexOf(event.keyCode) != -1) {
                // open container if 'down'
                if (event.keyCode == 40) self.containerVisibility(true);

                // clear if 'delete'
                if (event.keyCode == 46) self.value(null);

                return false;
            }

            return true;
        };

        this.renderHandler = function(element, data) {
            // stop loading when all elements was rendred
            if(this.foreach()[this.foreach().length - 1] === data) self.searching(false);
        };
    },
    template: 
       '<!-- ko if: disabled -->\
            <div class="autocomplete-select disabled" tabindex="0">\
                <span class="autocomplete-value" data-bind="text: label"></span>\
                <div class="autocomplete-twister"></div>\
            </div>\
        <!-- /ko -->\
        <!-- ko ifnot: disabled -->\
            <div class="autocomplete-select" tabindex="0"\
                data-bind="\
                    event: { mousedown: toggleContainer, keydown: keyManagment }, mousedownBubble: false,\
                    css: { opened: containerVisibility },\
                    hasFocus: componentFocused">\
                <span class="autocomplete-value" data-bind="text: label"></span>\
                <!-- ko if: value -->\
                    <a class="clear-button" data-bind="event: { mousedown: clear }, mousedownBubble: false">x</a>\
                <!-- /ko -->\
                <div class="autocomplete-twister"></div>\
            </div>\
        <!-- /ko -->\
        <!-- ko if: containerVisibility -->\
            <div class="autocomplete-container" data-bind="css: { loading: searching() }">\
                <div class="autocomplete-search-container">\
                    <!-- ko ifnot: Citeck.HTML5.supportAttribute("placeholder") -->\
                        <div class="help-message" data-bind="text: helpMessage()"></div>\
                    <!-- /ko -->\
                    <input type="text" class="autocomplete-search"\
                        data-bind="\
                            textInput: searchInput,\
                            event: { keydown: keyAction, blur: searchBlur },\
                            keydownBubble: false,\
                            attr: { placeholder: helpMessage() },\
                            hasFocus: true">\
                    <div class="loading-indicator" data-bind="css: { hidden: !searching() }"></div>\
                </div>\
                <!-- ko if: searchQuery() && options() -->\
                    <!-- ko if: options().length > 0 -->\
                        <ul class="autocomplete-list" data-bind="foreach: { data: options, afterRender: renderHandler }">\
                            <li data-bind="\
                                event: { mousedown: $parent.selectItem }, mousedownBubble: false,\
                                css: { selected: $parent.highlightedElement() == $data }">\
                                <a data-bind="text: $parent.data.getValueTitle($data)"></a>\
                            </li>\
                        </ul>\
                    <!-- /ko -->\
                <!-- /ko -->\
            </div>\
        <!-- /ko -->'
});


// -----------
// FILE UPLOAD
// -----------

ko.bindingHandlers.fileUploadControl = {                                                                    
    init: function(element, valueAccessor, allBindings, data, context) {
        var settings = valueAccessor(),
            value = settings.value,
            multiple = settings.multiple;

        // Invariants global object
        var Node = koutils.koclass('invariants.Node');

        // check browser support
        if (!window.File && !window.FileList) {
          throw new Error("The File APIs are not supported in this browser.")
          return;
        }

        // elements
        var input = Dom.get(element.id + "-fileInput"),
            openFileUploadDialogButton = Dom.get(element.id + "-openFileUploadDialogButton");

        // global variables
        var lastUploadedFiles = [];


        // click on input[file] button
        Event.on(openFileUploadDialogButton, 'click', function(event) {
            $(input).click();
        });

        // get files from input[file]
        Event.on(input, 'change', function(event) {
            var files = event.target.files;

            // uploaded files library
            var uploadedFiles = ko.observableArray();
            uploadedFiles.subscribe(function(array) {
                if (files.length > 0 && files.length == array.length) {

                    // delete old nodes
                    if (lastUploadedFiles.length > 0 && array != lastUploadedFiles) {
                        for (var i in lastUploadedFiles) {
                            deleteNode(lastUploadedFiles[i])
                        }
                    }

                    value(array);

                    // set last uploaded files
                    lastUploadedFiles = array;
                }
            })

            if (files.length == 0 || files != lastUploadedFiles) {
                value(null);
                uploadedFiles.removeAll();
            }

            for (var i = 0; i < files.length; i++) {
                var request = new XMLHttpRequest();

                (function(file){
                    // loading started
                    request.addEventListener("loadstart", function(event) {
                        // set fake data for content class
                        if (data.datatype() == "d:content") {
                            var fileObject = {
                                filename: file.name,
                                mimetype: file.type,
                                size:     file.size
                            };

                            if (multiple()) {
                                var values = updateList(value(), function(option) { 
                                    return { 
                                        filename: option.filename, 
                                        mimetype: option.mimetype, 
                                        size: option.size
                                    };
                                });

                                values.push(fileObject)
                                value(values);
                            } else {
                                value(fileObject)
                            }
                        }

                        $(element).addClass("loading");
                        $(openFileUploadDialogButton).attr("disabled", "disabled");  
                    }, false);
                })(files[i])

                // loading progress
                // request.addEventListener("progress", function(event) {
                //     var percent = Math.round((event.loaded * 100) / event.total);
                //     console.log("progress", percent);
                // }, false);

                // loading failure.
                request.addEventListener("error", function(event) {
                    console.log("loaded failure")
                }, false);
                
                // request finished
                request.addEventListener("readystatechange", function(event) {
                    var target = event.target;
                    if (target.status == 200 && target.readyState == 4) {
                        var result = JSON.parse(target.responseText || "{}");

                        // instance for invariant.node
                        new Node(result);

                        // push new file to uploaded files library
                        uploadedFiles.push(result.nodeRef);

                        $(element).removeClass("loading");
                        $(openFileUploadDialogButton).removeAttr("disabled"); 
                    }
                }, false)

                var formData = new FormData;
                formData.append("filedata", files[i]);
                formData.append("filename", files[i].name);
                formData.append("destination", "workspace://SpacesStore/attachments-root");
                formData.append("siteId", null);
                formData.append("containerId", null);
                formData.append("uploaddirectory", null);
                formData.append("majorVersion", false);
                formData.append("overwrite", false);
                formData.append("thumbnails", null);

                request.open("POST",  Alfresco.constants.PROXY_URI + "api/citeck/upload?assoctype=sys:children&details=true", true);
                request.send(formData);
            }
        });
    }
}


// ---------
// ORGSTRUCT
// ---------

ko.bindingHandlers.orgstructControl = {
    init: function(element, valueAccessor, allBindings) {
        var self = this;

        // default option
        var options = {
            allowedAuthorityType: "USER",
            allowedGroupType: ""
        }

        var settings = valueAccessor(),
            value = settings.value,
            multiple = settings.multiple,
            params = allBindings().params();

        var showVariantsButton = Dom.get(element.id + "-showVariantsButton"),
            orgstructPanelId = element.id + "-orgstructPanel", orgstructPanel, resize,
            tree, selectedItems;

        // concat default and new options
        if (params) concatOptions(options, params);


        Event.on(showVariantsButton, "click", function(event) {
            if (!orgstructPanel) {
                orgstructPanel = new YAHOO.widget.Panel(orgstructPanelId, {
                    width:          "800px", 
                    visible:        false, 
                    fixedcenter:    true,  
                    draggable:      true,
                    modal:          true,
                    zindex:         5,
                    close:          true
                });

                // hide dialog on click 'esc' button
                orgstructPanel.cfg.queueProperty("keylisteners", new YAHOO.util.KeyListener(document, { keys: 27 }, { 
                    fn: orgstructPanel.hide,
                    scope: orgstructPanel,
                    correctScope: true 
                }));

                var orgstructSearchBoxId = orgstructPanelId + "-searchBox",
                    orgstructSearchId = orgstructPanelId + "-searchInput",
                    orgstructTreeId = orgstructPanelId + "-treePicker",
                    orgstructSubmitButtonId = orgstructPanelId + "-submitInput",
                    orgstructCancelButtonId = orgstructPanelId + "-cancelInput";

                orgstructPanel.setHeader('Orgstruct Picker');
                orgstructPanel.setBody('\
                    <div class="orgstruct-header">\
                        <div class="orgstruct-search" id="' + orgstructSearchBoxId + '">\
                            <input class="search-input" type="text" value="" id="' + orgstructSearchId + '">\
                            <div class="search-icon"></div>\
                        </div>\
                    </div>\
                    <div class="yui-g orgstruct-layout">\
                        <div class="yui-u first panel-left resizable-panel" id="first-panel">\
                            <div class="orgstruct-tree" id="' + orgstructTreeId + '"></div>\
                        </div>\
                        <div class="yui-u panel-right" id="second-panel">\
                            <ul class="orgstruct-selected-items"></ul>\
                        </div>\
                    </div>\
                ');
                orgstructPanel.setFooter('\
                    <div class="buttons">\
                        <input type="submit" value="' + params.submitButtonTitle + '" id="' + orgstructSubmitButtonId + '">\
                        <input type="button" value="' + params.cancelButtonTitle + '" id="' + orgstructCancelButtonId + '">\
                    </div>\
                ');

                orgstructPanel.render(document.body);

                // initialize resize
                resize = new YAHOO.util.Resize("first-panel", { 
                    handles: ['r'],
                    minWidth: 200, 
                    maxWidth: 600
                });

                resize.on('resize', function(ev) {
                    Dom.setStyle(secondPanel, 'width', (800 - ev.width - 10) + 'px');
                });

                //  initialize tree
                var tree = new YAHOO.widget.TreeView(orgstructTreeId),
                    firstPanel = Dom.get("first-panel"),
                    secondPanel = Dom.get("second-panel");

                selectedItems = Dom.getElementsByClassName("orgstruct-selected-items", tree.body)[0];

                // initialize tree function
                tree.fn = {
                    loadNodeData: function(node, fnLoadComplete) {
                        YAHOO.util.Connect.asyncRequest('GET', tree.fn.buildTreeNodeUrl(node.data.shortName), {
                            success: function (oResponse) {
                                var results = YAHOO.lang.JSON.parse(oResponse.responseText), item, treeNode;

                                if (results) {
                                    for (var i = 0; i < results.length; i++) {
                                        item = results[i];

                                        treeNode = this.buildTreeNode(item, node, false);
                                        if (item.authorityType == "USER") {
                                            treeNode.isLeaf = true;
                                        }
                                    }
                                }

                                oResponse.argument.fnLoadComplete();
                            },

                            failure: function(oResponse) {
                                // error
                            },

                            scope: tree.fn,
                            argument: {
                              "node": node,
                              "fnLoadComplete": fnLoadComplete
                            }
                        });
                    },

                    loadRootNodes: function(tree, scope, query) {
                        YAHOO.util.Connect.asyncRequest('GET', tree.fn.buildTreeNodeUrl("_orgstruct_home_", query), {
                            success: function(oResponse) {
                                var results = YAHOO.lang.JSON.parse(oResponse.responseText), 
                                    rootNode = tree.getRoot(), treeNode,
                                    expanded = true;

                                if (results) {
                                    tree.removeChildren(rootNode);

                                    if (results.length > 1) expanded = false;
                                    for (var i = 0; i < results.length; i++) {
                                        treeNode = this.buildTreeNode(results[i], rootNode, expanded);
                                        if (results[i].authorityType == "USER") {
                                            treeNode.isLeaf = true;
                                        }
                                    }
                                }

                                tree.draw(); 
                            },

                            failure: function(oResponse) {
                                // error
                            },

                            scope: tree.fn
                        });
                    },

                    buildTreeNode: function(p_oItem, p_oParent, p_expanded) {
                        var textNode = new YAHOO.widget.TextNode({
                                label: $html(p_oItem.displayName || p_oItem.shortName),
                                nodeRef: p_oItem.nodeRef,
                                shortName: p_oItem.shortName,
                                displayName: p_oItem.displayName,
                                fullName: p_oItem.fullName,
                                authorityType: p_oItem.authorityType,
                                groupType: p_oItem.groupType,
                                editable : false
                        }, p_oParent, p_expanded);

                        // add nessesary classes
                        if (p_oItem.authorityType) textNode.contentStyle += " authorityType-" + p_oItem.authorityType;
                        if (p_oItem.groupType) textNode.contentStyle += " groupType-" + p_oItem.groupType.toUpperCase();

                        // selectable elements
                        if (options.allowedAuthorityType.indexOf(p_oItem.authorityType) != -1) {
                            if (p_oItem.authorityType == "GROUP") {
                                if (!options.allowedGroupType || options.allowedGroupType.indexOf(p_oItem.groupType.toUpperCase()) != -1) {
                                    textNode.className = "selectable";
                                }
                            }

                            if (p_oItem.authorityType == "USER") {
                                textNode.className = "selectable";
                            }
                        }

                        return textNode;
                    },

                    buildTreeNodeUrl: function (group, query) {
                        var uriTemplate ="api/orgstruct/group/" + Alfresco.util.encodeURIPath(group) + "/children?branch=true&role=true&group=true&user=true";
                        if (query) uriTemplate += "&filter=" + encodeURI(query) + "&recurse=true";
                        return  Alfresco.constants.PROXY_URI + uriTemplate;
                    },

                    onNodeClicked: function(args) {
                        var textNode = args.node,
                            object = textNode.data,
                            event = args.event;

                        var existsSelectedItems = [];

                        $("li.selected-object", this.selectedItems).each(function() {
                            existsSelectedItems.push(this.id);
                        })

                        // return if element exists
                        if (existsSelectedItems.indexOf(textNode.data.nodeRef) != -1) return false; 

                        if (options.allowedAuthorityType.indexOf(object.authorityType) != -1) {
                            if (object.authorityType == "GROUP") {
                                if (options.allowedGroupType && options.allowedGroupType.indexOf(object.groupType.toUpperCase()) == -1) {
                                    return false;
                                }
                            }

                            if (options.nodeSelectConstraintCallback) {
                                if (!options.nodeSelectConstraintCallback(textNode, options.context)) { return false; }
                            }


                            if (existsSelectedItems.length == 0 || (existsSelectedItems.length > 0 && multiple())) {
                                $(this.selectedItems).append(createSelectedObject({
                                    id: object.nodeRef, 
                                    label: object.displayName, 
                                    aType: textNode.data.authorityType,
                                    gType: textNode.data.groupType 
                                }));

                                // remove selectable state
                                $("table.selectable", textNode.getEl())
                                    .first()
                                    .removeClass("selectable")
                                    .addClass("unselectable selected");

                                return false;
                            }
                        }

                        return false;
                    },

                    onSearch: function(event) {
                        if(event.which == 13) {
                            event.stopPropagation();
                            
                            var input = event.target,
                                query = input.value;

                            if (query.length > 1) {
                                this.fn.loadRootNodes(this, this.fn, query)
                            } else if (query.length == 0) {
                                this.fn.loadRootNodes(this, this.fn)
                            }
                        }
                    }
                }

                 // initialise treeView
                tree.setDynamicLoad(tree.fn.loadNodeData);
                tree.fn.loadRootNodes(tree, tree);

                // Register tree-level listeners
                tree.subscribe("clickEvent", tree.fn.onNodeClicked, { 
                    selectedItems: selectedItems, 
                    multiple: multiple
                }, true);

                // search listener 
                Event.addListener(orgstructSearchId, "keypress", tree.fn.onSearch, tree, true);

                // value subscribe
                value.subscribe(function(newValue) {
                    clearUnselectedElements(tree);
                    updatedControlValue(newValue, selectedItems, tree);
                });


                // second panel delete listener
                Event.addListener(secondPanel, "click", function(event) {
                    if (event.target.tagName == "LI") {
                        var node = tree.getNodeByProperty("nodeRef", event.target.id);
                        if (node) { $("table", node.getEl()).first().removeClass("selected unselectable").addClass("selectable") };
                        $(event.target).remove();
                    }
                });


                // panel button listentes
                Event.addListener(orgstructSubmitButtonId, "click", function(event) {
                    this.hide();

                    var selectedItemsNodeRefs = [];

                    $("li.selected-object", selectedItems).each(function(index) { 
                        selectedItemsNodeRefs.push(this.id);
                    });

                    if (selectedItemsNodeRefs.length > 0) {
                        value(selectedItemsNodeRefs);
                    } else {
                        value(null);
                    }
                }, orgstructPanel, true);

                Event.addListener(orgstructCancelButtonId, "click", function(event) {
                    this.hide();
                    clearUnselectedElements(tree);
                    updatedControlValue(value(), selectedItems, tree);
                }, orgstructPanel, true);

                // for first run
                updatedControlValue(value(), selectedItems, tree);
            }

            orgstructPanel.show();
        })
    }
}


// ----------------
// PRIVATE FUNCTION
// ----------------

function clearUnselectedElements(tree) {
    $("table.unselectable.selected", tree.getEl())
        .removeClass("unselectable selected")
        .addClass("selectable");
}

function concatOptions(defaultOptions, newOptions) {
    for (var key in newOptions) {
        var newValue = newOptions[key],
            oldValue = defaultOptions[key];

        if (newValue && newValue != oldValue) {
            defaultOptions[key] = newOptions[key];
        }
    }
}

function createSelectedObject(options) {
    if (!options.id || !options.label) {
        throw new Error("Required parameters not found");
        return;
    }

    var li = $("<li>", { "class": "selected-object", html: options.label, id: options.id });
    li.click(function() { $(this).remove() });

    if (options.aType) li.addClass("authorityType-" + options.aType);
    if (options.gType) li.addClass("groupType-" + options.gType.toUpperCase());

    return li;
}

function updatedControlValue(valueObject, ulSelected, tree) {
    $(ulSelected).html("");

    if (valueObject) {
        var valueArray = [];

        if (typeof valueObject == "object") {
            if (valueObject instanceof Array) {
               for (var i in valueObject) {
                    valueArray.push(valueObject[i].nodeRef)
                }
            } else {
                valueArray.push(valueObject.nodeRef);
            }
        }

        if (typeof valueObject == "string") {
            valueArray = field.value.split(",");
        }

        for (var i in valueArray) {
            YAHOO.util.Connect.asyncRequest(
                "GET", 
                Alfresco.constants.PROXY_URI + "api/orgstruct/authority?nodeRef=" + valueArray[i], 
                {
                    success: function(response) {
                        var results = YAHOO.lang.JSON.parse(response.responseText);
                        if (results) {
                            var newLi = createSelectedObject({ 
                                    id: results.nodeRef, 
                                    label: results.displayName,
                                    aType: results.authorityType,
                                    gType: results.groupType
                                }),
                                existFlag = false,
                                textNode = tree.getNodeByProperty("nodeRef", results.nodeRef);

                            $(ulSelected)
                                .children()
                                .each(function(index) {
                                    if ($(this).attr("id") == newLi.attr("id")) { existFlag = true; }
                                });

                            if (!existFlag) {
                                $(ulSelected).append(newLi);
                            }

                            $("table.selectable", textNode.getEl())
                                .first()
                                .removeClass("selectable")
                                .addClass("unselectable selected");
                        }
                    },

                    failure: function(response) {
                    // error
                },
                scope: self
            });
        }
    }
}

function updateList(list, eachCallback) {
    var newList = [];
    if (list && list.length > 0) {
        for (var i in list) {
            newList[i] = eachCallback(list[i]);
        }
    } 

    return newList;
}

function deleteNode(nodeRef, callback) {
    YAHOO.util.Connect.asyncRequest('DELETE', 
                                    Alfresco.constants.PROXY_URI + "citeck/node?nodeRef=" + nodeRef, 
                                    callback);
}

function printableLongFileName(name, limit) {
    if (!name) {
        throw new Error("Does not exist 'name' argument");
        return null;
    }

    if (!limit) {
        throw new Error("Does not exist 'limit' argument");
        return null;
    }

    if (name.length > limit) {
        var startName = name.slice(0, limit/2-2),
            endName = name.slice(-(limit/2-2));

        return startName + "..." + endName;
    }

    return name;
}

function truncate(string, limit) {
    if (!string) {
        throw new Error("Does not exist 'string' argument");
        return null;
    }

    if (!limit) {
        throw new Error("Does not exist 'limit' argument");
        return null;
    }

    if (string.length > limit) {
        return string.slice(0, limit-3) + "..."
    }

    return string;
}

function getAttribute(node, attributeName) {
    var impl = node.impl(),
        attribute = impl.attribute(attributeName);
    return attribute && attribute.value() || null;
}

function getAttributeObservable(node, attributeName) {
    var attributeValueObservable = ko.observable();

    node.impl().attributes.subscribe(function() {
        attributeValueObservable(getAttribute(node, attributeName));
    })

    return attributeValueObservable;
}

function attributeValue(node, attributeName, callback) {
    var attributeValue = getAttribute(node, attributeName);
    if (attributeValue) {
       callback(attributeValue);
    } else {
        getAttributeObservable(node, attributeName).subscribe(function(newAttributeValue) {
            callback(newAttributeValue);
        }) 
    }
}

function isInvariantsObject(object) {
    return object.toString().toLowerCase().indexOf("invariants") != -1
}

function trim(string) {
    return String(string).replace(/^\s+|\s+$/g, '');
}

function sortingCreateVariants(variants) {
    var defaultVariantIndex,
        defaultVariant;

    for (var i in variants) {
        if (variants[i].isDefault) {
            defaultVariantIndex = i;
            break;
        }
    }

    if (defaultVariantIndex) {
        defaultVariant = variants.splice(defaultVariantIndex, 1);
        variants.splice(0, 0, defaultVariant[0]);
    }
}

})