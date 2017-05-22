/*
 * Copyright (C) 2016 - 2017 Citeck LLC.
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

define(['lib/knockout', 'citeck/utils/knockout.utils', 'citeck/components/journals2/journals', 'citeck/components/invariants/invariants', 'lib/moment'], function(ko, koutils, journals, invariants, moment) {


    var koValue = function(value) {
        return typeof value == "function" ? value() : value;
    };

    var Get = YAHOO.util.Get;
    var Node = koutils.koclass('invariants.Node');

    // COMPONENTS
    // ----------

    ko.components.register("filter-criterion-value", {
        viewModel: function(params) {
            var self = this;
            initializeParameters.call(this, params);

            this.html = ko.observable("");
            this._value = ko.observable(null);

            this.nodetype = this.attribute().nodetype ? this.attribute().nodetype() : null;
            this.journalType = this.attribute().journalType ? this.attribute().journalType() : null;
            this.settings = this.attribute().settings ? this.attribute().settings() : null;

            this.drawFilterCriterionValueComponent = function() {
                Alfresco.util.Ajax.request({
                    url: Alfresco.constants.URL_PAGECONTEXT + "citeck/components/region/get-region?fieldId="
                                                            + self.fieldId + "&template=" + self.templateName,
                    successCallback: {
                        scope: this,
                        fn: function(response) {
                            self.html(prepareHTMLByTemplate(response.serverResponse.responseText));
                        }
                    }
                });
            };

            this.fillSelectControl = function(documentType) {
                var query = {
                    skipCount: 0,
                    maxItems: 50,
                    field_1: "type",
                    predicate_1: "type-equals",
                    value_1: "cm:category",
                    field_2: "parent",
                    predicate_2: "parent-equals",
                    value_2: documentType
                };

                Alfresco.util.Ajax.jsonPost({
                    url: Alfresco.constants.PROXY_URI + "search/criteria-search",
                    dataObj: query,
                    successCallback: {
                        fn: function(response) {
                            var result = _.map(response.json.results, function(node) {
                                return new Node(node);
                            });
                            var temporaryArray = [];

                            for (var i = 0; i < result.length; i ++) {
                                var currentObj = result[i];
                                var displayName = currentObj.properties['cm:title']
                                    ? currentObj.properties['cm:title'] : currentObj.properties['cm:name'];
                                var nodeRef = currentObj.nodeRef;
                                temporaryArray.push([nodeRef, displayName]);
                            }
                            if (temporaryArray.length > 0) {
                                selectData(temporaryArray);
                            }
                        }
                    }
                });

                var selectData = ko.observable([]);
                selectData.subscribe(function() {
                    self.fakeViewModel.options(selectData());
                    self.drawFilterCriterionValueComponent();
                });
            };

            if (this.datatype) {
                var datatypeMapping = {
                    'boolean': 'd:boolean'
                };
                var datatype = datatypeMapping[self.datatype] || self.datatype;

                // prepare fake viewModel
                this.fakeViewModel = {
                    "fieldId": this.fieldId,

                    "mandatory": ko.observable(false),
                    "protected": ko.observable(false),
                    "multiple": ko.observable(false),
                    "relevant": ko.observable(true),

                    "value": self.value,

                    "title": ko.computed(function() {
                        var displayName = function () {
                            return self._value().properties["cm:title"] ? self._value().properties["cm:title"]
                                : self._value().properties["cm:name"];
                        };
                        return self._value() ? displayName() : Alfresco.util.message("label.none");

                    }),

                    "nodetype": ko.observable(self.nodetype),
                    "datatype": ko.observable(datatype),

                    "options": ko.observable([]),
                    "optionsText": function(o) { return o.attributes["cm:name"]; },
                    "optionsValue": function(o) { return o.nodeRef; },

                    "cache": {
                        result: ko.observable([])
                    }
                };

                this.templateName = defineTemplateByDatatype(this.datatype, this.nodetype);

                if (this.datatype == "association" && this.nodetype) {
                    if (this.value() && Citeck.utils.isNodeRef(this.value().toString())) {
                        this._value(new Node(this.value()));
                    }

                    this.fakeViewModel.value = ko.computed({
                        read: function() { return self._value() ? self._value() : null; }, 
                        write: function(newValue) {
                            if (newValue) {
                                var object = newValue instanceof Array ? newValue[0] : newValue,
                                    node = isInvariantsObject(object) ? object : new Node(object);

                                self._value(node);
                            } else { self._value(newValue); }
                            
                        }
                    });

                    if (this.templateName == "journal" && this.journalType) {
                        this.fakeViewModel.cache.result.extend({ notify: 'always' });

                        this.fakeViewModel.journalType = this.journalType;
                        this.fakeViewModel.searchCriteria = eval("(" + this.settings.searchCriteria + ")");
                        this.fakeViewModel.filterOptions = function(criteria, pagination) {                              
                            var query = {
                                skipCount: 0,
                                maxItems: 10,
                                field_1: "type",
                                predicate_1: "type-equals",
                                value_1: self.nodetype
                            };

                            if (pagination) {
                                if (pagination.maxItems) query.maxItems = pagination.maxItems;
                                if (pagination.skipCount) query.skipCount = pagination.skipCount;
                            }

                            _.each(criteria, function(criterion, index) {
                                query['field_' + (index + 2)] = criterion.attribute;
                                query['predicate_' + (index + 2)] = criterion.predicate;
                                query['value_' + (index + 2)] = criterion.value;
                            });
                           

                            if(this.cache.query) {
                                if(_.isEqual(query, this.cache.query)) return this.cache.result();
                            }

                            this.cache.query = query;
                            if (_.some(_.keys(query), function(p) {
                                return _.some(["field", "predicate", "value"], function(ci) {
                                    return p.indexOf(ci) != -1;
                                });
                            })) {
                                Alfresco.util.Ajax.jsonPost({
                                    url: Alfresco.constants.PROXY_URI + "search/criteria-search",
                                    dataObj: query,
                                    successCallback: {
                                        scope: this.cache,
                                        fn: function(response) {
                                            var result = _.map(response.json.results, function(node) {
                                                return new Node(node);
                                            });
                                            result.pagination = response.json.paging;
                                            result.query = response.json.query;
                                            this.result(result);
                                        }
                                    }
                                });
                            }

                            return this.cache.result();
                        };
                    } else if (this.templateName == "orgstruct") {
                        this.fakeViewModel.title = ko.computed(function() {
                            var authNode = self._value();
                            if (authNode) {
                                switch (authNode.typeShort) {
                                    case "cm:person":
                                        return authNode.properties["cm:firstName"] + " " + 
                                               authNode.properties["cm:lastName"];
                                        break;

                                    case "cm:authorityContainer":
                                        return authNode.properties["cm:authorityDisplayName"] ||
                                               authNode.properties["cm:authorityName"];
                                        break;

                                    default:
                                        return authNode.properties["cm:name"];
                                        break;
                                }
                            } else { return Alfresco.util.message("label.none"); }
                        });

                        switch (this.nodetype) {
                            case "{http://www.alfresco.org/model/content/1.0}authorityContainer":
                                this.fakeViewModel.allowedAuthorityType = ko.observable("USER");
                                break;

                            case "{http://www.alfresco.org/model/content/1.0}authority":
                                this.fakeViewModel.allowedAuthorityType = ko.observable("USER, GROUP");
                                break;
                        }
                    }
                } else if (this.labels) {
                    this.templateName = "select";
                    this.fakeViewModel.options(_.pairs(this.labels));
                    this.fakeViewModel.optionsText = function(o) { return o[1]; };
                    this.fakeViewModel.optionsValue = function(o) { return o[0]; } 
                } else if (this.datatype == "category" && this.journalType && this.attribute().name() == "tk:kind") {
                    var docType = this.journalOptionsType();
                    if (!docType) return;

                    self.templateName = "select";
                    self.fakeViewModel.options([["1", Alfresco.util.message('form.select.loading')]]);
                    self.fakeViewModel.optionsText = function(o) { return o[1]; };
                    self.fakeViewModel.optionsValue = function(o) { return o[0]; };

                    Alfresco.util.Ajax.jsonGet({
                        url: Alfresco.constants.PROXY_URI
                        + "/citeck/search/get-property-default-value?nodeType="
                        + docType + "&property=tk:type",
                        successCallback: {
                            fn: function(response) {
                                if (response.json.defaultValue != null) {
                                    self.fillSelectControl(response.json.defaultValue);
                                }
                            }
                        }
                    });
                } else if (this.datatype == "datetime") {
                    this.fakeViewModel.mode = ko.observable("alfresco");
                    this.fakeViewModel.value = ko.computed({
                        read: function() {
                            return self.value() ? new Date(self.value()) : null; 
                        },
                        write: function(newValue) {
                            self.value(newValue instanceof Date ? newValue.toString("yyyy-MM-ddTHH:mm") : null);
                        }
                    })
                }

                this.drawFilterCriterionValueComponent();
            }

            this.valueContainerId = this.fieldId + "-value";
            this.html.subscribe(function(newValue) {
                var container = $("<div>", { "class": "criterion-value-control-container" });
                container.append($("<div>", { "html": newValue, "class": "criterion-value-field-input" }));

                if (self.datatype == "association") {
                    container
                        .append(
                            $("<div>", { "class": "criterion-value-field-view" })
                                .append($("<span>", { "data-bind": "text: title" }))
                        );
                }

                var criterionValueElement = $("#" + self.valueContainerId);
                if (!criterionValueElement.find(".criterion-value-control-container").empty()) {
                    criterionValueElement.html("");
                } 
                criterionValueElement.append(container);

                ko.cleanNode(container);
                ko.applyBindings(self.fakeViewModel, container[0]);
            });

            this._value.subscribe(function(newValue) {
                if (newValue && newValue.nodeRef != self.value()) {
                    self.value(newValue.nodeRef);
                } else { self.value(newValue); }
            });
        }, 
        template: 
           '<div class="criterion-value" data-bind="attr: { id: valueContainerId }"></div>'
    });

    // TODO:
    // - refactoring 'filter-criteria' and 'list-of-selected-criterion'. Combine methods

    ko.components.register('filter-criteria-table', {
        viewModel: function(params) {
            var self = this;
            initializeParameters.call(this, params);

            self.remove = function(data, event) {
                self.selectedFilterCriteria.remove(data);
            };

            this.getAttribute = function (data) {
                return ko.computed(function() {
                    if (self.journalType) return self.journalType.attribute(data.name());
                    return data;
                });
            };
            this.getJournalOptionsType = function (data) {
                return ko.computed(function () {
                    var options = self.journalType.options();
                    return options ? options.type : null;
                })
            };

            this.valueVisibility = function(predicate) {
                return predicate && predicate.indexOf("empty") == -1;
            }
        },
        template: 
           '<table class="filter-criteria-table">\
                <tbody>\
                    <!-- ko foreach: selectedFilterCriteria -->\
                        <tr class="criterion">\
                            <td class="criterion-actions"><a class="remove-criterion" data-bind="click: $component.remove">X</a></td>\
                            <!-- ko if: $component.journalType -->\
                                <td class="criterion-field"><span class="selected-criterion-name" data-bind="text: displayName"></span></td>\
                                <td class="criterion-predicate" data-bind="with: datatype">\
                                    <select data-bind="options: predicates,\
                                                       optionsText: \'label\',\
                                                       optionsValue: \'id\',\
                                                       value: $parent.predicateValue"></select>\
                                </td>\
                                <td class="criterion-value-selector">\
                                    <!-- ko if: $component.valueVisibility($data.predicateValue()) -->\
                                        <!-- ko component: { name: "filter-criterion-value", params: {\
                                            fieldId: $component.htmlId + "-criterion-" + $index(),\
                                            labels: labels(),\
                                            datatype: resolve(\'datatype.name\', null),\
                                            value: value,\
                                            attribute: $component.getAttribute($data),\
                                            journalOptionsType: $component.getJournalOptionsType($data)\
                                        }} --><!-- /ko -->\
                                    <!-- /ko -->\
                                </td>\
                            <!-- /ko -->\
                            <!-- ko ifnot: $component.journalType -->\
                                <td class="criterion-field"><span class="selected-criterion-name" data-bind="text: title"></span></td>\
                                <td class="criterion-predicate">\
                                    <select data-bind="options: predicates,\
                                                       optionsText: \'label\',\
                                                       optionsValue: \'id\',\
                                                       value: predicateValue"></select>\
                                </td>\
                                <td class="criterion-value-selector">\
                                    <!-- ko if: $component.valueVisibility($data.predicateValue()) -->\
                                        <!-- ko component: { name: "filter-criterion-value", params: {\
                                            fieldId: $component.htmlId + "-criterion-" + $index(),\
                                            datatype: resolve(\'datatype\', null),\
                                            value: value,\
                                            attribute: $component.getAttribute($data),\
                                        }} --><!-- /ko -->\
                                    <!-- /ko -->\
                                </td>\
                            <!-- /ko -->\
                        </tr>\
                    <!-- /ko -->\
                </tbody>\
            </table>'
    });

    ko.components.register("filter-criteria", {
        viewModel: function(params) {
            var self = this;
            initializeParameters.call(this, params);

            this.remove = function(data, event) {
                self.filter().criteria.remove(data);
            };

            this.getAttribute = function (data) {
                return ko.computed(function() {
                    return self.journalType.attribute(data.resolve("field.name", null));
                });
            };
            this.getJournalOptionsType = function (data) {
                return ko.computed(function () {
                    var options = self.journalType.options();
                    return options ? options.type : null;
                })
            }

            this.valueVisibility = function(predicate) {
                return predicate && predicate.id().indexOf("empty") == -1;
            }
        },
        template: 
           '<div class="filter-criteria" data-bind="\
                attr: { id: id + \'-filter-criteria\' },\
                foreach: filter().criteria()\
            ">\
                <div class="criterion">\
                    <div class="criterion-actions">\
                        <a class="criterion-remove"\
                           data-bind="click: $component.remove,\
                                      attr: { title: Alfresco.util.message(\'button.remove-criterion\') }\
                        "></a>\
                    </div>\
                    <div class="criterion-field" data-bind="with: field">\
                        <input type="hidden" data-bind="attr: { name: \'field_\' + $parent.id() }, value: name" />\
                        <label data-bind="text: customDisplayName()"></label>\
                    </div>\
                    <div class="criterion-predicate">\
                        <!-- ko if: resolve(\'field.datatype.predicates.length\', 0) == 0 -->\
                            <input type="hidden" data-bind="attr: { name: \'predicate_\' + id() }, value: predicate().id()" />\
                        <!-- /ko -->\
                        <!-- ko if: resolve(\'field.datatype.predicates.length\', 0) > 0 -->\
                            <select data-bind="attr: { name: \'predicate_\' + id() },\
                                               value: predicate,\
                                               options: resolve(\'field.datatype.predicates\'),\
                                               optionsText: \'label\'\
                            "></select>\
                        <!-- /ko -->\
                    </div>\
                    <!-- ko if: $component.valueVisibility($data.predicate()) -->\
                        <!-- ko component: { name: "filter-criterion-value", params: {\
                            fieldId: $component.id + "-criterion-" + id(),\
                            datatype: resolve(\'field.datatype.name\', null),\
                            labels: resolve(\'field.labels\', null),\
                            value: value,\
                            attribute: $component.getAttribute($data),\
                            journalOptionsType: $component.getJournalOptionsType($data)\
                        }} --><!-- /ko -->\
                    <!-- /ko -->\
                </div>\
            </div>'
    });

    // TODO:
    // - formatter for cell

    ko.components.register('journal', {
        viewModel: function(params) {
            if (!params.sourceElements) {
                throw new Error("Required parameters are missing");
                return;
            }

            var self = this;
            initializeParameters.call(this, params);

            // options
            this.options = {
                multiple: false,
                pagination: false,
                loading: false,
                localization: { 
                    nextPageLabel: "-->", 
                    nextPageTitle: "-->", 
                    previousPageLabel: "<--", 
                    previousPageTitle: "<--" 
                } 
            };
            Citeck.utils.concatOptions(this.options, params.options);

            
            if (!this.loading) { this.loading = this.options.loading; }
            if (!this.columns) { this.columns = null; }

            // methods
            this.selected = function(data) {
                return  self.hightlightSelection ? ko.computed(function() {
                    return params.targetElements() ? params.targetElements().indexOf(data) != -1 : null;
                }) : false;
            };

            this.selectElement = function(data, event) {
                if (self.targetElements) {
                    if (ko.isObservable(self.options.multiple) ? self.options.multiple() : self.options.multiple) {
                        if (self.targetElements.indexOf(data) == -1) { self.targetElements.push(data); }
                        else { self.targetElements.remove(data); }
                    } else {
                        self.targetElements([data]);
                    }
                };

                if (self.afterSelectionCallback) self.afterSelectionCallback(data, event);
            };

            this.nextPage = function(data, event) {
                self.page(self.page() + 1);
            };

            this.previousPage = function(data, event) {
                self.page(self.page() - 1);
            };

            this.displayText = function(value, attr) {
                if (value) {
                    // if string
                    if (typeof value == "string") {
                        if (attr.labels() && attr.labels()[value]) 
                            return attr.labels()[value];
                    }

                    // if object
                    if (typeof value == "object") {
                        if (value instanceof Date) return value.toLocaleString();
                        if (isInvariantsObject(value)) return value.name;
                        if (value.length && attr.labels()) {
                            var array = value.map(function (item) {
                                return attr.labels()[item] ? attr.labels()[item] : item;
                            });
                            return array.join(", ");
                        }
                    }

                    return value;
                }

                return null;
            };

            this.getTitle = function(data) {
                return ko.computed(function() {
                    var value = data.value(),
                        invariantNodeName = function(object) {
                            if (isInvariantsObject(object) && object.properties)
                                return object.properties["cm:title"] || object.properties["cm:name"];
                            return object;
                        };

                    if (data.multiple()) {
                        var assemblyTitle = [];
                        for (var v in value) {
                            var tempTitle = invariantNodeName(value[v]);
                            if (tempTitle) assemblyTitle.push(tempTitle);
                        }
                        if (assemblyTitle.length > 0) return assemblyTitle.join(", ");
                    }
                   
                    return data.valueTitle() || data.textValue();
                });
            };

            this.getDefaultHeaderTitle = function(attributeName) {
                return ko.computed(function() {
                    if (self.sourceElements().length) {
                        var firstElement = self.sourceElements()[0].impl(),
                        attribute, title, name;

                        if (attributeName) {
                            attribute = firstElement.attribute(attributeName);
                            if (attribute) return attribute.title();
                        } else {
                            title = firstElement.attribute("cm:title");
                            name = firstElement.attribute("cm:name");

                            if (title) return title.title();
                            return name.title();
                        }
                    }

                    return null;
                });
            };

            this.getDefaultTitle = function(element, attributeName) {
                return ko.computed(function() {
                    if (attributeName) {
                        var attribute = element.impl().attribute(attributeName);
                        if (attribute) return attribute.value()
                    } else {
                        var title = element.impl().attribute("cm:title"),
                            name = element.impl().attribute("cm:name");

                        if (title) return title.value();
                        return name.value();
                    }

                    return null;
                });
            };
        },
          

        template:
           '<!-- ko if: loading -->\
                <div class="loading"></div>\
            <!-- /ko -->\
            <div class="journal">\
                <table>\
                    <thead>\
                        <tr data-bind="if: !$component.journalType && !$component.columns">\
                            <th data-bind="text: $component.getDefaultHeaderTitle()"></th>\
                        </tr>\
                         <!-- ko if: !$component.journalType && $component.columns -->\
                            <tr data-bind="foreach: columns">\
                                <th data-bind="text: $component.getDefaultHeaderTitle($data)"></th>\
                            </tr>\
                        <!-- /ko -->\
                        <tr data-bind="if: $component.journalType && !columns">\
                            <!-- ko foreach:  $component.journalType.defaultAttributes -->\
                                <th data-bind="text: displayName"></th>\
                            <!-- /ko -->\
                        </tr>\
                        <!-- ko if: $component.journalType && columns -->\
                            <tr data-bind="foreach: columns">\
                                <!-- ko if: $component.journalType.attribute($data) -->\
                                    <!-- ko with: $component.journalType.attribute($data) -->\
                                        <th data-bind="text: displayName"></th>\
                                    <!-- /ko -->\
                                <!-- /ko -->\
                            </tr>\
                        <!-- /ko -->\
                    </thead>\
                    <tbody data-bind="foreach: sourceElements">\
                        <tr class="journal-element" data-bind="if: !$component.journalType && !$component.columns,\
                                                               attr: { id: nodeRef },\
                                                               click: $component.selectElement, clickBubble: false,\
                                                               event: { dblclick: $component.selectElement },\
                                                               css: { selected: $component.selected($data) }">\
                            <td data-bind="text: $component.getDefaultTitle($data)"></td>\
                        </tr>\
                        <!-- ko if: !$component.journalType && $component.columns -->\
                            <tr class="journal-element" data-bind="attr: { id: nodeRef },\
                                                                   foreach: $component.columns,\
                                                                   click: $component.selectElement, clickBubble: false,\
                                                                   event: { dblclick: $component.selectElement },\
                                                                   css: { selected: $component.selected($data) }">\
                               <td data-bind="text: $component.getDefaultTitle($parent, $data)"></td>\
                            </tr>\
                        <!-- /ko -->\
                        <!-- ko if: $component.journalType && !$component.columns -->\
                            <tr class="journal-element" data-bind="attr: { id: nodeRef },\
                                                                   foreach: $component.journalType.defaultAttributes,\
                                                                   click: $component.selectElement, clickBubble: false,\
                                                                   event: { dblclick: $component.selectElement },\
                                                                   css: { selected: $component.selected($data) }">\
                                <!-- ko if: $parent.properties[$data.name()] -->\
                                    <td data-bind="text: $component.displayText($parent.properties[$data.name()], $data)"></td>\
                                <!-- /ko -->\
                                <!-- ko ifnot: $parent.properties[$data.name()] -->\
                                    <!-- ko with: $parent.impl().attribute($data.name()) -->\
                                        <td data-bind="text: $component.getTitle($data)"></td>\
                                    <!-- /ko -->\
                                <!-- /ko -->\
                            </tr>\
                        <!-- /ko -->\
                        <!-- ko if: $component.journalType && $component.columns -->\
                            <tr class="journal-element" data-bind="attr: { id: nodeRef },\
                                                                   foreach: $component.columns,\
                                                                   click: $component.selectElement, clickBubble: false,\
                                                                   event: { dblclick: $component.selectElement },\
                                                                   css: { selected: $component.selected($data) }">\
                               <!-- ko if: $component.journalType.attribute($data) -->\
                                    <!-- ko with: $component.journalType.attribute($data) -->\
                                        <!-- ko if: $parents[1].properties[$data.name()] -->\
                                            <td data-bind="text: $component.displayText($parents[1].properties[$data.name()], $data)"></td>\
                                        <!-- /ko -->\
                                        <!-- ko ifnot: $parents[1].properties[$data.name()] -->\
                                            <!-- ko with: $parents[1].impl().attribute($data.name()) -->\
                                                <td data-bind="text: $component.getTitle($data)"></td>\
                                            <!-- /ko -->\
                                        <!-- /ko -->\
                                    <!-- /ko -->\
                                <!-- /ko -->\
                            </tr>\
                        <!-- /ko -->\
                    </tbody>\
                </table>\
            </div>\
            <!-- ko if: options.pagination && sourceElements -->\
                <!-- ko with: sourceElements().pagination -->\
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


    // BINDINGS
    // ----------

    ko.bindingHandlers.templateSetter = (function() {
        var updateTemplate = function(element, valueAccessor) {
            var cfg = valueAccessor(),
                url = cfg.url,
                fieldName = cfg.field,
                templateName = cfg.name;
            if(typeof templateName != "function") {
                return;
            }
            
            var templateEl = valueAccessor.templateEl;
            if(!templateEl) {
                templateEl = document.createElement("SCRIPT");
                templateEl.id = Alfresco.util.generateDomId();
                templateEl.type = "html/template";
                element.appendChild(templateEl);
                valueAccessor.templateEl = templateEl;
            }
            
            // once template is set, no change is needed
            if(templateName() == templateEl.id) {
                return;
            }
            
            // if it is loading - no more requests permited
            if(valueAccessor.loading) {
                return;
            }
            
            Alfresco.util.Ajax.request({
                url: url,
                execScripts: true,
                successCallback: {
                    scope: this,
                    fn: function(response) {
                        var html = response.serverResponse.responseText;
                        
                        // support value bindings
                        if(fieldName) {
                            html = _.reduce([
                                    'name="' + fieldName + '"', 
                                    "name='" + fieldName + "'", 
                                    'name="' + fieldName + '_added"', 
                                    "name='" + fieldName + "_added'"], 
                                function(html, pattern) {
                                    return html.replace(new RegExp('(' + pattern + ')', 'gi'), '$1 data-bind="value: value"');
                                }, html);
                        }
                        
                        templateEl.innerHTML = html;
                        templateName(templateEl.id);
                        valueAccessor.loading = false;
                    }
                },
                failureCallback: {
                    scope: this,
                    fn: function(response) {
                        valueAccessor.loading = false;
                    }
                }
            });
            valueAccessor.loading = true;
        };
        return {
            init: updateTemplate,
            update: updateTemplate
        };
    })();

    ko.bindingHandlers.gotoAddress = (function() {
        var gotoAddress = function(element, valueAccessor) {
            var value = koValue(valueAccessor());
            if(value) window.location = Alfresco.util.siteURL(value);
        };
        return {
            init: gotoAddress,
            update: gotoAddress
        };
    })();

    ko.virtualElements.allowedBindings.gotoAddress = true;

    ko.bindingHandlers.dependencies = (function() {
        var updateDependencies = function(element, valueAccessor) {
            if(!element.currentDeps) element.currentDeps = {};
            var currentDeps = element.currentDeps,
                oldDeps = _.keys(currentDeps),
                newDeps = koValue(valueAccessor());
            
            // old dependencies;
            _.each(_.difference(oldDeps, newDeps), function(dep) {
                if(typeof currentDeps[dep].purge == "function") {
                        currentDeps[dep].purge();
                }
                delete currentDeps[dep];
            });
            
                
            // new dependencies
            _.each(_.difference(newDeps, oldDeps), function(dep) {
                var config = {
                    data: dep,
                    onSuccess: function(o) {
    //                  valueAccessor().notifySubscribers(o.data, "loaded")
                        currentDeps[o.data] = o;
                    }
                };
                if(dep.match(/\.js$/)) {
                    Get.script(Alfresco.constants.URL_RESCONTEXT + dep, config);
                } else if(dep.match(/\.css$/)) {
                    Get.css(Alfresco.constants.URL_RESCONTEXT + dep, config);
                } else {
                    logger.warn("Unknown dependency type: " + dep);
                }
            });
        };
        return {
            init: updateDependencies,
            update: updateDependencies
        };
    })();

    ko.virtualElements.allowedBindings.dependencies = true;



    // FUNCTIONS LIBRARY
    //-------------------
    
    function initializeParameters(params) {
        for (var p in params) { this[p] = params[p] }
    }

    function defineTemplateByDatatype(datatype, nodetype) {
        var templateName =  _.contains(["text", "date", "datetime"], datatype) ? datatype : "";

        if (!templateName) {
            switch (datatype) {
                case "association":
                    var authorityTypes = [
                        "{http://www.alfresco.org/model/content/1.0}person",
                        "{http://www.alfresco.org/model/content/1.0}authorityContainer",
                        "{http://www.alfresco.org/model/content/1.0}authority"
                    ];

                    for (var a in authorityTypes) {
                        if (authorityTypes[a] == nodetype) templateName = "orgstruct";
                        break;
                    }

                    if (!templateName) templateName = "journal";
                    break;
                case "float":
                case "long":
                case "int":
                case "double":
                    templateName = "number";
                    break;
                case "boolean":
                    templateName = "checkbox";
                    break;
                case "mltext":
                default:
                    templateName = "text";
                    break;                 
            }
        }

        return templateName;
    }

    function prepareHTMLByTemplate(html) {
        var fixedHTML = html.replace("textInput: textValue", "value: value");
        fixedHTML = fixedHTML.replace("textInput: textValidationValue", "value: value");
        fixedHTML = fixedHTML.replace("value: textValue", "value: value");
        fixedHTML = fixedHTML.replace("optionsText: function(item) { return getValueTitle(item) }", 
                                      "optionsText: optionsText, optionsValue: optionsValue");
        return fixedHTML;
    }

    function isInvariantsObject(object) {
        return object ? object.toString().toLowerCase().indexOf("invariants") != -1 : null;
    }

    return {
        initializeParameters: initializeParameters,
        defineTemplateByDatatype: defineTemplateByDatatype,
        prepareHTMLByTemplate: prepareHTMLByTemplate,
        isInvariantsObject: isInvariantsObject
    };

});


