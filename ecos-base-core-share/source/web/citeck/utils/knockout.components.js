/*
 * Copyright (C) 2016 Citeck LLC.
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

define(['lib/knockout'], function(ko) {


    var koValue = function(value) {
        return typeof value == "function" ? value() : value;
    };

    var Get = YAHOO.util.Get;


    // COMPONENTS
    // ----------

    ko.components.register("filter-criterion-value", {
        viewModel: function(params) {
            var self = this;
            initializeParameters.call(this, params);

            this.html = ko.observable("");
            this.valueContainerId = this.fieldId + "-value";

            this.nestedViewModel = {
                "fieldId": this.fieldId,

                "mandatory": ko.observable(false),
                "protected": ko.observable(false),
                "multiple": ko.observable(false),
                "relevant": ko.observable(true),

                "value": this.value,

                "options": ko.observable([]),
                "optionsText": function(o) { return o.attributes["cm:name"]; },
                "optionsValue": function(o) { return o.nodeRef; }
 
            }

            if (this.datatype) {
                this.templateName = defineTemplateByDatatype(this.datatype);

                if (this.datatype == "association" && this.nodetype()) {
                    var query = {
                        field_1: "type",
                        predicate_1: "type-equals",
                        value_1: this.nodetype(),
                        skipCount: 0,
                        maxItems: 10,
                        sortBy: []
                    };

                    Alfresco.util.Ajax.jsonPost({
                        url: Alfresco.constants.PROXY_URI + "search/criteria-search",
                        dataObj: query,
                        successCallback: {
                            scope: this,
                            fn: function(response) { this.nestedViewModel.options(response.json.results); }
                        }
                    });
                }

                Alfresco.util.Ajax.request({
                    url: Alfresco.constants.URL_PAGECONTEXT + "citeck/components/region/get-region?fieldId=" + this.fieldId + "&template=" + this.templateName,
                    successCallback: {
                        scope: this,
                        fn: function(response) {
                            this.html(prepareHTMLByTemplate(response.serverResponse.responseText));
                        }
                    }
                });
            }

            this.html.subscribe(function(newValue) {            
                var zeroEl = $("<div>", { html: newValue });
                $("#" + self.valueContainerId).append(zeroEl);

                ko.cleanNode(zeroEl);
                ko.applyBindings(self.nestedViewModel, zeroEl[0]);
            });
        }, 
        template: 
           '<div class="criterion-value" data-bind="attr: { id: valueContainerId }"></div>'
    });

    ko.components.register("filter-criteria", {
        viewModel: function(params) {
            var self = this;
            initializeParameters.call(this, params);
      
            this.remove = function(data, event) {
                self.filter().criteria.remove(data);
            }

            this.nodetype = function(data) {
                return ko.computed(function() {
                    return self.journalType.attribute(data.resolve("field.name", null)).nodetype();
                });
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
                        <label data-bind="text: displayName"></label>\
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
                    <!-- ko component: { name: "filter-criterion-value", params: {\
                        fieldId: $component.id + "-criterion-" + id(),\
                        datatype: resolve(\'field.datatype.name\', null),\
                        nodetype: $component.nodetype($data),\
                        value: value\
                    }} --><!-- /ko -->\
                </div>\
            </div>'
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

    function defineTemplateByDatatype(datatype) {
        var templateName =  _.contains(["text", "date", "datetime"], datatype) ? datatype : "";
        if (!templateName) {
            switch (datatype) {
                case "association":
                    templateName = "select";
                    break;
                case "float":
                case "long":
                case "int":
                case "double":
                    templateName = "number";
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
        fixedHTML = fixedHTML.replace("value: textValue", "value: value");
        fixedHTML = fixedHTML.replace("optionsText: function(item) { return getValueTitle(item) }", 
                                      "optionsText: optionsText, optionsValue: optionsValue");
        return fixedHTML;
    }
});


