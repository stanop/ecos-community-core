/*
 * Copyright (C) 2015-2017 Citeck LLC.
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
define(['lib/knockout', 'citeck/utils/knockout.utils', 'citeck/utils/knockout.components', 'citeck/components/invariants/invariants', 'citeck/components/journals2/journals', 'lib/moment'], function(ko, koutils, kocomponents, invariants, journals, moment) {

// ----------------
// GLOBAL FUNCTIONS 
// ----------------

var $html = Alfresco.util.encodeHTML,
    $combine = Alfresco.util.combinePaths,
    koclass = koutils.koclass;

var Event = YAHOO.util.Event,
    Dom = YAHOO.util.Dom;

var JournalType = koclass('JournalType');


// TODO: refactoring
// - integrate the calendar into a single function for the date and datetime controls

// ---------------
// HELP
// ---------------

YAHOO.widget.Tooltip.prototype.onContextMouseOut = function (e, obj) {
    var el = this,
        procIds = [ "showProcId", "hideProcId" ];

    if (obj._tempTitle) {
        el.title = obj._tempTitle;
        obj._tempTitle = null;
    }

    for (var p = 0; p < procIds.length; procIds++) {
        if (obj[procIds[p]]) {
            clearTimeout(obj[procIds[p]]);
            obj[procIds[p]] = null; 
        }
    }

    obj.fireEvent("contextMouseOut", el, e);

    if (!obj.cfg.getProperty("forceVisible")) {
        obj.hideProcId = setTimeout(function () { obj.hide(); }, obj.cfg.getProperty("hidedelay"));
    }
};

// TODO:
// - init tooltip only if text not empty

ko.components.register("help", {
    viewModel: function(params) {
        kocomponents.initializeParameters.call(this, params);
        var self = this;

        if (!this.tooltip) {
            this.tooltip = new YAHOO.widget.Tooltip(this.id + "-tooltip", {
                showDelay: 250,
                hideDelay: 250,
                xyoffset: [5, 0],
                autodismissdelay: 10000,
                context: [ this.id ]
            });

            this.tooltip.cfg.addProperty("forceVisible", { value: false });
            this.tooltip.body.setAttribute("style", "white-space: pre-wrap;");

            if (this.text()) { this.tooltip.cfg.setProperty("text", this.text()); }
        }

        this.tooltip.contextMouseOverEvent.subscribe(function() {
            var parent = $("#" + self.id).closest(".yui-panel-container"),
                zindex = parent.css("z-index") ? parseInt(parent.css("z-index")) + 1 : 10;
            self.tooltip.cfg.setProperty("zIndex", zindex);
        });

        this.text.subscribe(function(newValue) {
            if (newValue) this.tooltip.cfg.setProperty("text", newValue);
            this.tooltip.cfg.setProperty("disabled", !newValue);
        }, this);

        this.onclick = function(data, event) {
            data.tooltip.cfg.setProperty("forceVisible",  !data.tooltip.cfg.getProperty("forceVisible"));
        };
    },
    template:
       '<span data-bind="attr: { id: id }, if: text, click: onclick">?</span>'
});

// ---------------
// SELECT
// ---------------

ko.components.register("select", {
    viewModel: function(params) {
        kocomponents.initializeParameters.call(this, params);     
        this.data.options.extend({ throttle: 500 });

        if (!this.optionsText) {
            if (this.data.optionsText) { this.optionsText = this.data.optionsText; }
            else { this.optionsText = function(option) { return this.getValueTitle(option); }.bind(this.data) };
        }
        if (!this.optionsValue && this.data.optionsValue) { this.optionsValue = this.data.optionsValue; }
        if (!this.optionsAfterRender && this.data.optionsAfterRender) { this.optionsAfterRender = this.data.optionsAfterRender; }
    },
    template: 
       '<!--ko ifnot: data.multiple -->\
            <select data-bind="attr: { id: id },\
                disable: data.protected,\
                options: data.options,\
                optionsCaption: optionsCaption,\
                optionsText: optionsText, optionsValue: optionsValue, optionsAfterRender: optionsAfterRender,\
                value: data.value,\
                valueAllowUnset: true"></select>\
        <!-- /ko -->\
        <!-- ko if: data.multiple -->\
            <select data-bind="attr: { id: id, multiple: data.multiple },\
                disable: data.protected,\
                options: data.options,\
                optionsCaption: optionsCaption,\
                optionsText: optionsText, optionsValue: optionsValue, optionsAfterRender: optionsAfterRender,\
                selectedOptions: data.multipleValues,\
                valueAllowUnset: true"></select>\
        <!-- /ko -->'
});


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

        this.id = params.id;
        this.step = params.step && _.isNumber(params.step) ? params.step : "any";
        this.disable = params.disable;
        this.value = params.value;

        this.lastSymbolIsPeriod = this.value() && this.value().indexOf(".") == this.value().length - 1;
        this.hasPeriodSymbol = this.value() && this.value().indexOf(".") >= 0;
        this.noValue = _.isEmpty(this.value());

        this.validation = function(data, event) {
            var whiteNumbers = _.range(48, 58),
                whiteKeys = [8, 46, 37, 39],
                whitePeriod = [44, 46],
                code = event.keyCode || event.charCode;

            // return if code of key out of white list
            if (!_.contains(_.union(whiteNumbers, whiteKeys, whitePeriod), code)) return false;

            if (_.contains(whitePeriod, event.charCode)) {
                if (self.lastSymbolIsPeriod || self.hasPeriodSymbol || self.noValue) return false;
            };

            return true;
        };

        this.value.subscribe(function(newValue) {
            self.hasPeriodSymbol = (newValue && newValue.indexOf(".") >= 0) ? true : false;
            self.noValue = self.lastSymbolIsPeriod = _.isEmpty(newValue);
        });
    },
    template: 
       '<input type="number" data-bind="textInput: value, disable: disable, attr: { id: id, step: step }, event: { keypress: validation }" />'
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
            if (typeof result == "string") return result;

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

        this.mode = params.mode == "browser";
        this.fieldId = params["fieldId"];
        this.value = params["value"];
        this.disabled = params["protected"];

        if(!this.mode || !Citeck.HTML5.supportInput("datetime-local")){
            this.calendar = function() {
                if (!calendarDialog) {
                    var formContainer = $("#" + this.fieldId).closest(".yui-panel-container"),
                        zindex = formContainer.css("z-index") ? parseInt(formContainer.css("z-index")) + 1 : 15;

                    calendarDialog = new YAHOO.widget.Dialog(calendarDialogId, {
                        visible:    false,
                        context:    [calendarAccessorId, "tl", "bl"],
                        draggable:  false,
                        close:      true,
                        zindex:     zindex
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
        }

        this.textValue = ko.pureComputed({
            read: function() {
                return self.value() instanceof Date ? moment(self.value()).format("YYYY-MM-DD HH:mm:ss") : null;
            },
            write: function(newValue) {
                if (newValue) {                   
                    if (/\d{2,4}-\d{2}-\d{2}(, | )\d{2}:\d{2}(:\d{2}|)/.test(newValue)) {
                        var timeArray = newValue.split(/, | /);
                        timeArray[0] = timeArray[0].split(".").reverse().join("/");

                        var newDate = new Date(timeArray.join("T"));
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
       '<!-- ko if: Citeck.HTML5.supportInput("datetime-local") && mode -->\
            <input type="datetime-local" data-bind="value: dateValue, disable: disabled" />\
        <!-- /ko -->\
        <!-- ko if: !mode || !Citeck.HTML5.supportInput("datetime-local") -->\
            <input type="text" data-bind="value: textValue, disable: disabled, attr: { placeholder: localization.formatIE }" />\
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

        var localization = params.localization,
            mode = params.mode;
        
        var elementId = element.id.replace("-dateControl", ""),
            input = Dom.get(elementId);

        if (mode == "alfresco" || !Citeck.HTML5.supportedInputTypes.date) {
            var calendarDialogId = elementId + "-calendarDialog",
                calendarContainerId = elementId + "-calendarContainer",
                calendarAccessorId = elementId + "-calendarAccessor",
                calendarDialog, calendar;

            var showCalendarButton = document.getElementById(calendarAccessorId);
            showCalendarButton.classList.remove("hidden");

            Event.on(showCalendarButton, "click", function() {
                if (!calendarDialog) {
                    var formContainer = $(element).closest(".yui-panel-container"),
                        zindex = formContainer.css("z-index") ? parseInt(formContainer.css("z-index")) + 1 : 15;

                    calendarDialog = new YAHOO.widget.Dialog(calendarDialogId, { 
                        visible:    false, 
                        context:    [calendarAccessorId, "tl", "bl"], 
                        draggable:  false, 
                        close:      true,
                        zindex:     zindex
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
                            var selectedDate = calendar.getSelectedDates()[0];
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

            if (input) {
                input.setAttribute("max", (year + 50) + "-12-31");
                input.setAttribute("min", (year - 25) + "-12-31");

                Dom.setStyle(input, "color", "lightgray");
                value.subscribe(function(value) {
                  Dom.setStyle(input, "color", value ? "" : "lightgray");
                });
            }
        }
    }
};


// -------------
// JOURNAL
// -------------



ko.bindingHandlers.journalControl = {
  init: function(element, valueAccessor, allBindings, data, context) {
    var self = this;

    // html elements
    var button  = Dom.get(element.id + "-button"),
        panelId = element.id + "-journalPanel", panel;

    // binding variables
    var settings = valueAccessor(),
        value    = settings.value,
        multiple = settings.multiple,
        params   = allBindings().params(),
        removeSelection = params.removeSelection;
    // sorting
    var sortBy  = params.sortBy;

    // localization
    var localization = {
        title: Alfresco.util.message("form.select.label"),
        search: Alfresco.util.message("journal.search"),
        elementsTab: Alfresco.util.message("journal.elements"),
        filterTab: Alfresco.util.message("journal.filter"),
        createTab: Alfresco.util.message("journal.create"),
        selectedElements: Alfresco.util.message("journal.selected-elements"),
        applyCriteria: Alfresco.util.message("journal.apply-criteria"),
        addFilterCriterion: Alfresco.util.message("journal.add-filter-criterion"),
        submitButton: Alfresco.util.message("button.ok"),
        cancelButton: Alfresco.util.message("button.cancel"),
        nextPageLabel: Alfresco.util.message("journal.pagination.next-page-label"),
        nextPageTitle: Alfresco.util.message("journal.pagination.next-page-title"),
        previousPageLabel: Alfresco.util.message("journal.pagination.previous-page-label"),
        previousPageTitle: Alfresco.util.message("journal.pagination.previous-page-title")
    };

    // params
    var defaultVisibleAttributes    = params.defaultVisibleAttributes,
        defaultSearchableAttributes = params.defaultSearchableAttributes,
        defaultHiddenByType         = params.defaultHiddenByType,
        
        searchMinQueryLength        = params.searchMinQueryLength,
        searchScript                = _.contains(["criteria-search", "light-search"], params.searchScript) ? params.searchScript : "criteria-search",

        searchCriteria              = params.searchCriteria || data.searchCriteria,
        defaultCriteria             = params.defaultCriteria,
        hiddenCriteria              = params.hiddenCriteria || [],

        createVariantsVisibility    = params.createVariantsVisibility;

    if (defaultVisibleAttributes) {
        defaultVisibleAttributes = _.map(defaultVisibleAttributes.split(","), function(item) { return trim(item) });
    }

    if (defaultSearchableAttributes) {
        defaultSearchableAttributes = _.map(defaultSearchableAttributes.split(","), function(item) { return trim(item) });
    }

    if (defaultHiddenByType) {
        defaultHiddenByType = _.map(defaultHiddenByType.split(","), function(item) { return trim(item) });
    }

    // maxItems
    var maxItems = ko.observable($("body").hasClass("mobile") ? 5 : 10);

    //  initialize criteria
    var criteria = ko.observable([]);
    if (defaultCriteria) criteria(defaultCriteria);

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
        event.stopPropagation();
        event.preventDefault();

        if (!panel) {
            var selectedElements = ko.observableArray(), selectedFilterCriteria = ko.observableArray(), 
                loading = ko.observable(true), criteriaListShow = ko.observable(false), 
                searchBar = params.searchBar ? params.searchBar == "true" : true,
                mode = params.mode, dockMode = params.dock ? "dock" : "",
                pageNumber = ko.observable(1), skipCount = ko.computed(function() { return (pageNumber() - 1) * maxItems() }),
                additionalOptions = ko.observable([]), options = ko.computed(function(page) {
                    var actualCriteria = criteria();
                    if (hiddenCriteria) {
                        for (var hc in hiddenCriteria) {
                            if (!_.some(actualCriteria, function(criterion) { return _.isEqual(criterion, hiddenCriteria[hc]) }))
                                actualCriteria.push(hiddenCriteria[hc]);
                        }
                    }

                    var nudeOptions = data.filterOptions(actualCriteria, {
                            maxItems: maxItems(), 
                            skipCount: skipCount(), 
                            searchScript: searchScript,
                            sortBy: sortBy
                        }),
                        config = nudeOptions.pagination,
                        result;
              
                    var tempAdditionalOptions = additionalOptions();
                    _.each(additionalOptions(), function(o) {
                        if (_.contains(nudeOptions, o)) {
                            var index = tempAdditionalOptions.indexOf(o);
                            tempAdditionalOptions.splice(index, 1);
                        }
                    });
                    additionalOptions(tempAdditionalOptions);

                    if (additionalOptions().length > 0) {
                        if (nudeOptions.length < maxItems()) {
                            result = _.union(nudeOptions, additionalOptions());

                            if (result.length > maxItems()) result = result.slice(0, maxItems());
                            if (maxItems() - nudeOptions.length < additionalOptions().length) config.hasMore = true;
                            
                            result.pagination = config;
                            loading(_.isUndefined(nudeOptions.pagination));
                            return result;
                        } else {
                            if (!nudeOptions.pagination.hasMore)
                                nudeOptions.pagination.hasMore = true;
                        }
                    }

                    loading(_.isUndefined(nudeOptions.pagination));
                    return nudeOptions;
                });

            // reset page after new search
            criteria.subscribe(function(newValue) { pageNumber(1); });

            // show loading indicator if page was changed
            pageNumber.subscribe(function(newValue) { loading(true); });

            // extend notify
            criteria.extend({ notify: 'notifyWhenChangesStop' });
            pageNumber.extend({ notify: 'always' });
            options.extend({ rateLimit: { method: 'notifyWhenChangesStop', timeout: 0 } });

            var journalType = params.journalType ? new JournalType(params.journalType) : (data.journalType || null);
            if (!journalType) { /* so, it is fail */ }

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

            var optimalWidth = (function() {
                var maxContainerWidth = screen.width - 200,
                    countOfAttributes = (function() {
                        if (defaultVisibleAttributes) return defaultVisibleAttributes.length;
                        if (journalType.defaultAttributes()) return journalType.defaultAttributes().length;
                    })();

                if (countOfAttributes > 5) {
                    var potentialWidth = 150 * countOfAttributes;
                    return (potentialWidth >= maxContainerWidth ? maxContainerWidth : potentialWidth) + "px";
                }

                return "800px";
            })();

            panel = new YAHOO.widget.Panel(panelId, {
                width:          optimalWidth,
                visible:        false, 
                fixedcenter:    true,  
                draggable:      true,
                modal:          true,
                zindex:         5,
                close:          true
            });

            // hide dialog on click 'esc' button
            panel.cfg.queueProperty("keylisteners", new YAHOO.util.KeyListener(document, { keys: 27 }, {
                fn: panel.hide,
                scope: panel,
                correctScope: true
            }));

            panel.setHeader(localization.title || 'Journal Picker');
            panel.setBody('\
                <div class="journal-picker-header ' + mode + ' ' + dockMode + '" id="' + journalPickerHeaderId + '">\
                    <a id="' + elementsTabId + '" class="journal-tab-button ' + (mode == "collapse" ? 'hidden' : '') + ' selected">' + localization.elementsTab + '</a>\
                    <a id="' + filterTabId + '" class="journal-tab-button">' + localization.filterTab + '</a>\
                    <!-- ko if: createVariantsVisibility -->\
                        <!-- ko component: { name: "createObjectButton", params: {\
                            scope: scope,\
                            source: createVariantsSource,\
                            callback: callback,\
                            buttonTitle: buttonTitle,\
                            virtualParent: virtualParent,\
                            journalType: journalType\
                        }} --><!-- /ko -->\
                    <!-- /ko -->\
                    ' + (searchBar ? '<div class="journal-search"><input type="search" placeholder="' + localization.search + '" class="journal-search-input" id="' + searchId + '" /></div>' : '') + '\
                </div>\
                <div class="journal-picker-page-container ' + mode + '">\
                    <div class="filter-page hidden" id="' + filterPageId + '">\
                        <div class="selected-filter-criteria-container">\
                            <!-- ko component: { name: \'filter-criteria-table\',\
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
                                    hightlightSelection: hightlightSelection,\
                                    afterSelectionCallback: afterSelectionCallback,\
                                    options: {\
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
                        <!-- ko if: dock -->\
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
                        <!-- /ko -->\
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
                if (selectedElements() && selectedElements().length) {
                    value(removeSelection
                        ? (multiple() ? value().concat(selectedElements()) : selectedElements())
                        : ko.utils.unwrapObservable(selectedElements))
                }
                this.panel.hide();
            }, { selectedElements: removeSelection ? value() : selectedElements, panel: panel }, true);

            Event.on(cancelButtonId, "click", function(event) {
                this.hide();
            }, panel, true);

            panel.hideEvent.subscribe(function() {
                if (removeSelection) {
                    selectedElements([]);
                }
            });

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
                            var searchValue = search.value.trim();
                            if (searchMinQueryLength && searchValue.length < searchMinQueryLength) {
                                return false;
                            }

                            if (searchCriteria && searchCriteria.length > 0) {
                                criteria(_.map(searchCriteria, function(item) {
                                    return _.defaults({ value: searchValue }, item);
                                }));
                            } else {
                               criteria([{ attribute: "all", predicate: "string-contains", value: searchValue }]);
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
                dock: params.dock,
                hightlightSelection: params.hightlightSelection,
                afterSelectionCallback: function(data, event) {
                    if (!multiple() && event.type == "dblclick") { value(data); panel.hide(); }
                }
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
                journalType: journalType,
                createVariantsVisibility: createVariantsVisibility,
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
                            scCallback(node);
                        },
                        cancel: scCallback
                    }, 
                    { 
                        destination: variant.destination(),
                        fieldId: data.name()
                    });

                },
                virtualParent: params.virtualParent,
                createVariantsSource: params.createVariantsSource
            }, Dom.get(journalPickerHeaderId));

            if (value()) selectedElements(multiple() ? value() : [ value() ]);


            if (!Citeck.mobile.isMobileDevice()) {
                YAHOO.Bubbling.on("change-mobile-mode", function(l, args) { 
                    var itemsCount = args[1].mobileMode ? 5 : 10;
                    if (itemsCount != maxItems()) { 
                        pageNumber(1);
                        maxItems(itemsCount);
                    };
                });
            }

            // reload filterOptions request if was created new object
            YAHOO.Bubbling.on("object-was-created", function(layer, args) {
                if (args[1].fieldId == data.name()) {
                    if (args[1].value) {
                        additionalOptions(_.union(additionalOptions(), [ args[1].value ]));
                        selectedElements.push(args[1].value); 
                    }
                    
                    criteria(_.clone(criteria()));
                }
            });
        }
        
        panel.show();
    })
  }
}

// -------------
// CREATE OBJECT
// -------------

var CreateVariant = koclass('CreateVariant'),
    CreateVariantsByJournal = koclass('controls.CreateVariantsByJournal'),
    CreateVariantsByType = koclass('controls.CreateVariantsByType'),
    CreateVariantsByView = koclass('controls.CreateVariantsByView'),
    CreateObjectButton = koclass('controls.CreateObjectButton'),
    CreateObjectLink = koclass('controls.CreateObjectLink');

CreateVariantsByJournal
    .key('journal', String)
    .property('createVariants', [CreateVariant])
    .load('createVariants', koutils.simpleLoad({
        url: Alfresco.constants.PROXY_URI + "api/journals/create-variants/journal/{journal}",
        resultsMap: { createVariants: 'createVariants' }
    }))
    ;

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
    .property('journalType', JournalType)
    .property('parentRuntime', String)
    .property('virtualParent', Boolean)
    .property('callback', Function)

    .shortcut('protected', 'scope.protected')
    .shortcut('nodetype', 'scope.nodetype')

    .computed('createVariants', function() {

        var list = null;

        if (this.source() == 'create-views' && this.nodetype()) {
            list = new CreateVariantsByView(this.nodetype());
        } else if (this.source() == 'type-create-variants' && this.nodetype()) {
            list = new CreateVariantsByType(this.nodetype());
        } else if (this.source() == 'journal-create-variants' && this.journalType()) {
            list = new CreateVariantsByJournal(this.journalType().id());
        }
        return list ? list.createVariants() : [];
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
       '<!-- ko if: protected() || createVariants().length == 0 --> \
            <button class="create-object-button" disabled="disabled" data-bind="text: buttonTitle"></button> \
        <!-- /ko --> \
        <!-- ko if: !protected() && createVariants().length == 1 --> \
            <button class="create-object-button" data-bind="text: buttonTitle, attr: { title: createVariants()[0].title() }, click: execute.bind($data, createVariants()[0])"></button> \
        <!-- /ko --> \
        <!-- ko if: !protected() && createVariants().length > 1 --> \
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
        var self = this;

        this.defaults = {
            criteria: [{ attribute: "all", predicate: "string-contains" }],
            searchScript: "criteria-search",
            maxItems: 10
        }

        this.labels = {
            label: Alfresco.util.message("autocomplete.label"),
            help: Alfresco.util.message("autocomplete.help"),
            empty: Alfresco.util.message("autocomplete.empty"),
            more: Alfresco.util.message("autocomplete.more")
        }

        this.cache = {
            criteria: [],
            options: []
        }

        // base variables
        this.element = params.element;
        this.data  = params.data;
        this.value = this.data["singleValue"];
        this.disabled = this.data["protected"];

        this.searchScript = params.searchScript || self.defaults.searchScript;
        this.minQueryLength = params.minQueryLength;
        this.maxItems = params.maxItems || self.defaults.maxItems;

        // observables
        this.containerVisibility = ko.observable(false);
        this.highlightedElement = ko.observable();
        this.searchQuery = ko.observable();
        this.searching = ko.observable(false);

        this.componentFocused = ko.observable(false);
        this.searchFocused = ko.observable(true);

        this.hasMore = ko.observable(false);
        this.skipCount = ko.observable(0);

        
        // computed
        this.label = ko.pureComputed(function() {
            return self.value() ? self.data.getValueTitle(self.value()) : self.labels.label; 
        });

        this.searchInput = ko.computed({
            read: function() {
                return self.searchQuery();
            },
            write: function(newValue) {
                if (!newValue || (!self.minQueryLength || (newValue.length >= self.minQueryLength))) {
                    self.searchQuery(newValue);
                    self.searching(true);
                }
            }
        });

        this.criteria = ko.pureComputed(function() {
            if (self.searchQuery()) {
                return _.map(params["criteria"] || self.defaults.criteria, function(item) {
                    return _.defaults({ value: self.searchQuery() }, item);
                });
            } else { return [] }
        }).extend({ rateLimit: { timeout: 500, method: "notifyWhenChangesStop" } });

        this.options = ko.pureComputed(function() {
            var result = self.data.filterOptions(self.criteria(), { 
                maxItems: 10, 
                skipCount: self.skipCount(), 
                searchScript: self.searchScript
            });
            if (result.pagination) return result;
        });
        
        this.visibleOptions = ko.pureComputed(function() {
            if (self.options() && self.options().pagination) {
                self.cache.options = self.cache.options.concat(self.options());
                return self.cache.options;
            }
        });


        // subscription and events
        this.containerVisibility.subscribe(function() {
            self.searchFocused(true);
        });

        this.criteria.subscribe(function(newValue) {
            self.skipCount(0);
            self.cache.options = [];
        });      
         
        this.options.subscribe(function(newValue) {
            if (newValue && newValue.pagination) {
                self.hasMore(newValue.pagination.hasMore);
                self.searching(false);

                if (newValue.length > 0 && self.skipCount() == 0) 
                    self.highlightedElement(newValue[0]);
                      
            }
        });

        // public methods
        this.clear = function(data, event) { if (event.which == 1) self.value(null) };
        this.toggleContainer = function(data, event) { if (event.which == 1) self.containerVisibility(!self.containerVisibility()); };
        this.renderHandler = function(element, data) { if(this.foreach()[this.foreach().length - 1] === data) self.searching(false); };

        this.more = function(element, data) { 
            self.skipCount(self.skipCount() + 10);
            self.searchFocused(true);
        };
        
        this.selectItem = function(data, event) {
            if (event.which == 1) {
                self.value(data);
                self.containerVisibility(false);
                self.highlightedElement(data);
            }
        };

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
                    var selectedIndex = self.visibleOptions().indexOf(self.highlightedElement()),
                        nextSelectIndex = event.keyCode == 38 ? selectedIndex - 1 : selectedIndex + 1;
                    
                    if (selectedIndex != -1 && self.visibleOptions()[nextSelectIndex]) { 
                        self.highlightedElement(self.visibleOptions()[nextSelectIndex]) 
                    };

                    // TODO:
                    // - select 'more' throuth keyboard
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

        // blur
        $("body").click(function(event, a) {
            var node = event.target,
                body = document.getElementById("Share");

            while (node != body) {
                if (node == self.element) {
                    return;
                }
                
                node = node.parentNode;
            }

            self.containerVisibility(false);
        });
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
                        <div class="help-message" data-bind="text: labels.help"></div>\
                    <!-- /ko -->\
                    <input type="text" class="autocomplete-search"\
                        data-bind="\
                            textInput: searchInput,\
                            event: { keydown: keyAction },\
                            keydownBubble: false,\
                            attr: { placeholder: labels.help },\
                            hasFocus: searchFocused">\
                    <div class="loading-indicator" data-bind="css: { hidden: !searching() }"></div>\
                </div>\
                <!-- ko if: visibleOptions() && !searching() -->\
                    <!-- ko if: visibleOptions().length > 0 -->\
                        <ul class="autocomplete-list" data-bind="foreach: { data: visibleOptions, afterRender: renderHandler }">\
                            <li data-bind="\
                                event: { mousedown: $parent.selectItem }, mousedownBubble: false,\
                                css: { selected: $parent.highlightedElement() == $data }">\
                                <a data-bind="text: $parent.data.getValueTitle($data)"></a>\
                            </li>\
                        </ul>\
                    <!-- /ko -->\
                    <!-- ko if: visibleOptions().length == 0 -->\
                        <span class="autocomplete-message empty-message" data-bind="text: labels.empty"></span>\
                    <!-- /ko -->\
                <!-- /ko -->\
                <!-- ko if: hasMore -->\
                    <div class="autocomplete-more">\
                        <a data-bind="click: more, attr: { title: labels.more }">...</a>\
                    </div>\
                <!-- /ko -->\
            </div>\
        <!-- /ko -->'
});

// ---------------
// SELECT 2
// ---------------

ko.components.register("select2", {
    viewModel: function(params) {
        var self = this;
        kocomponents.initializeParameters.call(this, params);      

        this.id = this.element.id;

        this._listMode = self.mode == "list";
        this._tableMode = self.mode == "table";

        if (this.forceOptions) {
            this.forceOptions = this.forceOptions();
            this.forceOptions.extend({ rateLimit: { timeout: 250, method: "notifyWhenChangesStop" } });
        }

        if (this._tableMode) {
            if (this.defaultVisibleAttributes)
                this.defaultVisibleAttributes =  _.map(this.defaultVisibleAttributes.split(","), function(a) { return trim(a); })

            this.journalType = this.journalTypeId ? new JournalType(this.journalTypeId) : null;
            if (!this.journalType) { /* so, it is fail */ }
        }


        // localization
        var localization = this.localization = {
            select: Alfresco.util.message("button.select"),

            // table
            title: Alfresco.util.message("form.select.label"),
            search: Alfresco.util.message("journal.search"),
            filterTab: Alfresco.util.message("journal.filter"),
            createTab: Alfresco.util.message("journal.create"),
            selectedElements: Alfresco.util.message("journal.selected-elements"),
            applyCriteria: Alfresco.util.message("journal.apply-criteria"),
            addFilterCriterion: Alfresco.util.message("journal.add-filter-criterion"),
            submitButton: Alfresco.util.message("button.ok"),
            cancelButton: Alfresco.util.message("button.cancel"),
            nextPageLabel: Alfresco.util.message("journal.pagination.next-page-label"),
            nextPageTitle: Alfresco.util.message("journal.pagination.next-page-title"),
            previousPageLabel: Alfresco.util.message("journal.pagination.previous-page-label"),
            previousPageTitle: Alfresco.util.message("journal.pagination.previous-page-title"),

            // list
            label: Alfresco.util.message("autocomplete.label"),
            help: Alfresco.util.message("autocomplete.help"),
            empty: Alfresco.util.message("autocomplete.empty"),
            more: Alfresco.util.message("autocomplete.more")
        }


        // private methods
        // ---------------

        this._optionTitle = function(option) {
            if (self.optionsText) return self.optionsText(option);
            return self.getValueTitle(option);
        };

        this._addValues = function(values) {
            self.value(self.multiple() ? _.union(self.value(), values) : values[0]);
        }


        // observables
        // -----------

        this.containerVisibility = ko.observable(false);
        this.highlightedElement = ko.observable();
        this.searchQuery = ko.observable();

        this.hasMore = ko.observable(false);
        this.count = ko.observable(this.step);
        this.page = ko.observable(1);

        // for list mode
        this.componentFocused = ko.observable(false);
        this.searchFocused = ko.observable(true);

        // for table mode
        this.panel;
        this.selectedElements = ko.observableArray();
        this.selectedFilterCriteria = ko.observableArray();
        this.additionalOptions = ko.observable([]);
        this._criteriaListShow = ko.observable(false);


        // computed
        // --------

        this.label = ko.pureComputed(function() {
            return self.value() ? self.getValueTitle(self.value())() : self.localization.label;
        });
      
        this.visibleOptions = ko.pureComputed(function() {
            var preparedOptions = self.forceOptions ? self.forceOptions() : self.options();

            if (self.additionalOptions().length) {
                preparedOptions = _.union(preparedOptions, self.additionalOptions()); 
            }
               
            if (self.searchQuery()) {
                preparedOptions = _.filter(preparedOptions, function(option) {
                    var searchString = self.searchQuery().toLowerCase(),
                        labelString  = self._optionTitle(option)();

                    if (labelString) {
                        labelString = labelString.toLowerCase();
                        switch (self.searchPredicat) {
                            case "startsWith":
                                return labelString.startsWith(searchString);

                            case "contains":
                                return labelString.indexOf(searchString) != -1;
                        }
                    }

                    return false;
                });
            }

            // pagination for list
            if (self._listMode) {
                if (self.count() < preparedOptions.length) {
                    self.hasMore(true);
                    return preparedOptions.slice(0, self.count());
                }
            }

            // pagination for table
            if (self._tableMode) {
                var startIndex = self.step * self.page() - self.step, endIndex = self.step * self.page();
                self.hasMore(self.step * self.page() < preparedOptions.length);
                return preparedOptions.slice(startIndex, endIndex);
            }

            self.hasMore(false);
            return preparedOptions;
        });


        // extends
        // -------

        this.searchQuery.extend({ rateLimit: { timeout: 250, method: "notifyWhenChangesStop" } });


        // subscription and events
        // -----------------------

        this.searchQuery.subscribe(function() { self.count(self.step); });
        if (this._listMode) {
            this.containerVisibility.subscribe(function() { self.searchFocused(true); });
            this.visibleOptions.subscribe(function(newValue) { if (newValue.length > 0) self.highlightedElement(newValue[0]); });
        }


        // public methods
        // --------------

        this.clear = function(data, event) { if (event.which == 1) self.value(null) };
        this.toggleContainer = function(data, event) { if (event.which == 1) self.containerVisibility(!self.containerVisibility()); };

        this.selectItem = function(data, event) {
            if (event.which == 1) {
                if (self.optionsValue) {
                    var optionValue = self.optionsValue(data);
                    self.value(optionValue());
                } else { self.value(data);  }    // put item to value
                self.containerVisibility(false); // close container after select item
                self.highlightedElement(data);   // highlight the selected item
            }
        };

        this.keyAction = function(data, event) {
            if ([9, 13, 27, 38, 40].indexOf(event.keyCode) != -1) {
                // apply element for value
                if (event.keyCode == 9 || event.keyCode == 13) {
                    self.value(self.highlightedElement());
                }

                // close container
                if (event.keyCode == 9 || event.keyCode == 13 || event.keyCode == 27) {
                    self.containerVisibility(false); // close container after select item
                    self.componentFocused(true);     // restore focus on component
                }

                // move selection
                if (event.keyCode == 38 || event.keyCode == 40) {
                    var selectedIndex = self.options().indexOf(self.highlightedElement()),
                        nextSelectIndex = event.keyCode == 38 ? selectedIndex - 1 : selectedIndex + 1;

                    if (selectedIndex != -1 && self.options()[nextSelectIndex]) {
                        self.highlightedElement(self.options()[nextSelectIndex]); // highlight next or previous item
                    };
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

        this.more = function(element, data) {
            self.count(self.count() + self.step);
        };

        var elementsPageId          = this.id + "-panel-elementsPage",
            filterPageId            = this.id + "-panel-filterPage",
            createPageId            = this.id + "-panel-createPage",
            journalPickerHeaderId   = this.id + "-panel-journalPickerHeader";

        this.journalPicker = function(data, event) {
            console.log("click on journalPicker button", data, event, this);

            if (!data.panel) {
                // Auto-fit width
                var optimalWidth = (function() {
                    var maxContainerWidth = screen.width - 200,
                        countOfAttributes = (function() {
                            if (data.defaultVisibleAttributes) return data.defaultVisibleAttributes.length;
                            if (data.journalType) return data.journalType.defaultAttributes().length;
                            return 1;
                        })();

                    if (countOfAttributes > 5) {
                        var potentialWidth = 150 * countOfAttributes;
                        return (potentialWidth >= maxContainerWidth ? maxContainerWidth : potentialWidth) + "px";
                    }

                    return "800px";
                })();

                // initialize panel
                data.panel = new YAHOO.widget.Panel(data.id + "-panel", {
                    width:          optimalWidth,
                    visible:        false, 
                    fixedcenter:    true,  
                    draggable:      true,
                    modal:          true,
                    zindex:         5,
                    close:          true
                });

                // hide dialog on click 'esc' button
                data.panel.cfg.queueProperty("keylisteners", new YAHOO.util.KeyListener(document, { keys: 27 }, {
                    fn: data.panel.hide,
                    scope: data.panel,
                    correctScope: true
                }));

                // build panel header, body and footer
                data.panel.setHeader(data.localization.title);
                data.panel.setBody('\
                    <div class="journal-picker-header collapse" id="' + journalPickerHeaderId + '">\
                        <!-- ko if: createVariantsVisibility -->\
                            <!-- ko component: { name: "createObjectButton", params: {\
                                scope: scope,\
                                source: createVariantsSource,\
                                callback: callback,\
                                buttonTitle: labels.createTab,\
                                virtualParent: virtualParent,\
                                journalType: journalType\
                            }} --><!-- /ko -->\
                        <!-- /ko -->\
                        <div class="journal-search">\
                            <input type="search" class="journal-search-input" data-bind="\
                                textInput: searchQuery,\
                                attr: { placeholder: labels.search }\
                            " />\
                        </div>\
                    </div>\
                    <div class="journal-picker-page-container">\
                        <div class="elements-page" id="' + elementsPageId + '">\
                            <div class="journal-container">\
                                <!-- ko component: { name: \'journal\',\
                                    params: {\
                                        sourceElements: elements,\
                                        targetElements: selectedElements,\
                                        journalType: journalType,\
                                        columns: columns,\
                                        hightlightSelection: true,\
                                        afterSelectionCallback: afterSelectionCallback,\
                                        options: { multiple: multiple, pagination: false },\
                                    }\
                                } --><!-- /ko -->\
                                <div class="journal-pagination">\
                                    <span class="previous-page">\
                                        <!-- ko if: page() - 1 > 0 -->\
                                            <a data-bind="click: previousPage,\
                                                          text: labels.previousPageLabel,\
                                                          attr: { title: labels.previousPageTitle }"><--</a>\
                                        <!-- /ko -->\
                                        <!-- ko ifnot: page() - 1 > 0 -->\
                                            <!-- ko text: labels.previousPageLabel --><!-- /ko -->\
                                        <!-- /ko -->\
                                    </span>\
                                    <span class="page-label">\
                                        <span class="start-page" data-bind="text: page() * maxItems - maxItems + 1"></span>\
                                        <span class="dash">-</span>\
                                        <span class="end-page" data-bind="text: page() * maxItems"></span>\
                                    </span>\
                                    <span class="next-page">\
                                        <!-- ko if: hasMore -->\
                                            <a data-bind="click: nextPage,\
                                                          text: labels.nextPageLabel,\
                                                          attr: { title: labels.nextPageTitle }">--></a>\
                                        <!-- /ko -->\
                                        <!-- ko ifnot: hasMore -->\
                                            <!-- ko text: labels.nextPageLabel --><!-- /ko -->\
                                        <!-- /ko -->\
                                    </span>\
                                </div>\
                            </div>\
                        </div>\
                        <div class="create-page hidden" id="' + createPageId + '"></div>\
                    </div>\
                ');
                data.panel.setFooter('\
                    <div class="buttons">\
                        <input type="submit" data-bind="value: labels.submitButton, click: submit, clickBubble: false" >\
                        <input type="button" data-bind="value: labels.cancelButton, click: cancel, clickBubble: false" >\
                    </div>\
                ');

                data.panel.render(document.body);


                // bindings for journal panel of table mode
                // ----------------------------------------

                ko.applyBindings({
                    // header
                    labels: data.localization,
                    searchQuery: data.searchQuery,
                    createVariantsVisibility: data.createVariantsVisibility,
                    callback: function(variant) {
                        var scCallback = function(node) {
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
                        };

                        Citeck.forms.formContent(variant.type(), variant.formId(), {
                            response: function(response) {
                                Dom.get(createPageId).innerHTML = response;

                                // hide other pages and remove selection from other tabs
                                // Dom.removeClass(elementsTabId, "selected");
                                // Dom.removeClass(filterTabId, "selected");
                                Dom.addClass(elementsPageId, "hidden");
                                // Dom.addClass(filterPageId, "hidden");

                                // show create page and hightlight tab
                                Dom.removeClass(createPageId, "hidden");
                                var createButton = Dom.getElementsBy(function(el) {
                                    return el.tagName == "BUTTON";
                                }, "button", journalPickerHeaderId);
                                Dom.addClass(createButton, "selected");
                            },

                            submit: function(node) {
                                self.additionalOptions(_.union(self.additionalOptions(), [node ]));
                                scCallback(node);
                            },
                            cancel: scCallback
                        }, 
                        { 
                            destination: variant.destination(),
                            fieldId: data.name()
                        });

                    },
                    virtualParent: data.virtualParent,
                    createVariantsSource: data.createVariantsSource,
                    scope: data,

                    // pagination
                    maxItems: data.step,
                    page: data.page,
                    hasMore: data.hasMore,
                    previousPage: function(data, event) { data.page(data.page() - 1); },
                    nextPage: function(data, event) { data.page(data.page() + 1); },

                    // body
                    elements: data.visibleOptions,
                    selectedElements: data.selectedElements,
                    multiple: data.multiple,
                    journalType: data.journalType,
                    columns: data.defaultVisibleAttributes,
                    afterSelectionCallback: function(data, event) {
                        if (!self.multiple() && event.type == "dblclick") {
                            self._addValues([ data ]); 
                            self.panel.hide();
                            self.selectedElements.removeAll();
                        }
                    },
                    createPageVisibility: data._createPageVisibility,
                    elementPageVisibility: data._elementPageVisibility
                }, data.panel.body);

                ko.applyBindings({
                    labels: data.localization,
                    submit: function(el, data) {
                        self._addValues(ko.utils.unwrapObservable(self.selectedElements));
                        self.panel.hide();
                        self.selectedElements.removeAll();
                    },
                    cancel: function(el, data) { 
                        self.panel.hide();
                        self.selectedElements.removeAll();
                    }
                }, data.panel.footer);
            }
            
            data.panel.show();
        }

        // blur
        if (this._listMode) {
            $("body").click(function(event, a) {
                var node = event.target, body = document.getElementById("Share");

                while (node && node != body) {
                    if (node == self.element) return;
                    node = node.parentNode;
                }

                self.containerVisibility(false);
            });
        }

    },
    template:
       '<!-- ko if: _listMode -->\
            <!-- ko if: disabled -->\
                <div class="select2-select disabled" tabindex="0">\
                    <span class="select2-value" data-bind="text: label"></span>\
                    <div class="select2-twister"></div>\
                </div>\
            <!-- /ko -->\
            <!-- ko ifnot: disabled -->\
                <div class="select2-select" tabindex="0"\
                    data-bind="\
                        event: { mousedown: toggleContainer, keydown: keyManagment }, mousedownBubble: false,\
                        css: { opened: containerVisibility },\
                        hasFocus: componentFocused">\
                    <span class="select2-value" data-bind="text: label"></span>\
                    <!-- ko if: value -->\
                        <a class="clear-button" data-bind="event: { mousedown: clear }, mousedownBubble: false">x</a>\
                    <!-- /ko -->\
                    <div class="select2-twister"></div>\
                </div>\
            <!-- /ko -->\
            <!-- ko if: containerVisibility -->\
                <div class="select2-container">\
                    <div class="select2-search-container">\
                        <!-- ko ifnot: Citeck.HTML5.supportAttribute("placeholder") -->\
                            <div class="help-message" data-bind="text: localization.help"></div>\
                        <!-- /ko -->\
                        <input type="text" class="select2-search" data-bind="\
                            textInput: searchQuery,\
                            event: { keydown: keyAction },\
                            keydownBubble: false,\
                            attr: { placeholder: localization.help },\
                            hasFocus: searchFocused">\
                    </div>\
                    <!-- ko if: visibleOptions -->\
                        <!-- ko if: visibleOptions().length > 0 -->\
                            <ul class="select2-list" data-bind="foreach: visibleOptions">\
                                <li data-bind="\
                                    event: { mousedown: $parent.selectItem }, mousedownBubble: false,\
                                    css: { selected: $parent.highlightedElement() == $data }">\
                                    <a data-bind="text: $component._optionTitle($data)"></a>\
                                </li>\
                            </ul>\
                        <!-- /ko -->\
                        <!-- ko if: visibleOptions().length == 0 -->\
                            <span class="select2-message empty-message" data-bind="text: localization.empty"></span>\
                        <!-- /ko -->\
                    <!-- /ko -->\
                    <!-- ko if: hasMore -->\
                        <div class="select2-more">\
                            <a data-bind="click: more, attr: { title: localization.more }">...</a>\
                        </div>\
                    <!-- /ko -->\
                </div>\
            <!-- /ko -->\
        <!-- /ko -->\
        <!-- ko if: _tableMode -->\
            <button class="select2-control-button" data-bind="disable: disabled, text: localization.select, click: journalPicker"></button>\
        <!-- /ko -->'
});


// -----------
// FILE UPLOAD
// -----------

ko.bindingHandlers.fileUploadControl = {                                                                    
    init: function(element, valueAccessor, allBindings, data, context) {
        var settings = valueAccessor(),
            value = settings.value,
            multiple = settings.multiple,
            type = settings.type,
            properties = settings.properties;

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

        // click on input[file] button
        Event.on(openFileUploadDialogButton, 'click', function(event) {
            $(input).click();
        });

        // get files from input[file]
        Event.on(input, 'change', function(event) {
            var files = event.target.files,
                loadedFiles = ko.observable(0);

            if (files.length === 0) {
                return;
            }

            loadedFiles.subscribe(function(newValue) {
                if (newValue == files.length) {
                    // enable button
                    $(element).removeClass("loading");
                    $(openFileUploadDialogButton).removeAttr("disabled");
                }
            });

            // disable upload button
            $(element).addClass("loading");
            $(openFileUploadDialogButton).attr("disabled", "disabled");

            for (var i = 0; i < files.length; i++) {
                var request = new XMLHttpRequest();

                (function(file){
                    // loading failure.
                    request.addEventListener("error", function(event) {
                        console.log("loaded failure")
                        loadedFiles(loadedFiles() + 1);
                    }, false);
                    
                    // request finished
                    request.addEventListener("readystatechange", function(event) {
                        var target = event.target;
                        if (target.readyState == 4) {
                            var result = JSON.parse(target.responseText || "{}");
                            
                            if (target.status == 200) {
                                // push new file to uploaded files library
                                if (multiple()) {
                                    var currentValues = value();
                                    currentValues.push(result.nodeRef);
                                    value(currentValues);
                                } else {
                                    //TODO: remove previous node if parent == attachments-root?
                                    value(result.nodeRef);
                                }
                            }

                            if (target.status == 500) {
                                Alfresco.util.PopupManager.displayPrompt({ title: target.statusText, text: result.message });
                            }

                            loadedFiles(loadedFiles() + 1);
                        }
                    }, false)
                })(files[i])

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

                if (properties) {
                    for (var p in properties) {
                        formData.append("property_" + p, properties[p]);
                    }
                }

                var href = Alfresco.constants.PROXY_URI + "api/citeck/upload?assoctype=sys:children&details=true";
                if (type) href += "&contenttype=" + type;

                request.open("POST", href, true);
                request.send(formData);
            }
        });
    }
}


// ---------
// ORGSTRUCT
// ---------

ko.bindingHandlers.orgstructControl = {
    init: function(element, valueAccessor, allBindings, data, context) {
        var self = this;

        // default option
        var options = {
            allowedAuthorityType: "USER",
            allowedGroupType: "",
            rootGroup: ko.observable("_orgstruct_home_")
        };

        // from fake model option
        if (data.allowedAuthorityType && data.allowedAuthorityType())
            options.allowedAuthorityType = data.allowedAuthorityType();

        var settings = valueAccessor(),
            value = settings.value,
            multiple = settings.multiple,
            params = allBindings().params();

        var showVariantsButton = Dom.get(element.id + "-showVariantsButton"),
            orgstructPanelId = element.id + "-orgstructPanel", orgstructPanel, resize,
            tree, selectedItems;

        var rootGroupFunction;
        if (!params.rootGroup && params.rootGroupFunction && _.isFunction(params.rootGroupFunction)) {
            rootGroupFunction = ko.computed(params.rootGroupFunction);
            rootGroupFunction.subscribe(function (newValue) { options.rootGroup(newValue) });
        }

        // concat default and new options
        if (params) concatOptionsWithObservable(options, params);

        Event.on(showVariantsButton, "click", function(event) {
            event.stopPropagation();
            event.preventDefault();

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

                orgstructPanel.setHeader(Alfresco.util.message("orgstruct.picker"));
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
                                if (params && params.excludeFields) {
                                    results = results.filter(function(item) {
                                        return item.shortName.indexOf(params.excludeFields) == -1;
                                    });
                                }

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
                        YAHOO.util.Connect.asyncRequest('GET', tree.fn.buildTreeNodeUrl(options.rootGroup(), query), {
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
                                //draw empty tree, if group not found
                                if (oResponse.status == 404) {
                                    tree.removeChildren(tree.getRoot());
                                    tree.draw();
                                }
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
                        });

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
                };

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

                // update tree after change rootGroup
                options.rootGroup.subscribe(function(newValue) {
                    tree.fn.loadRootNodes(tree, tree);
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
            valueArray = valueObject.split(",");
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

function concatOptionsWithObservable(defaultOptions, newOptions) {
    for (var key in newOptions) {
        var newValue = newOptions[key],
            oldValue = defaultOptions[key];

        if (newValue && newValue != oldValue) {
            if (ko.isObservable(oldValue)) {
                if (oldValue() != newValue) defaultOptions[key](newOptions[key]);
            } else {
               defaultOptions[key] = newOptions[key];
            }
        }
    }
}

})