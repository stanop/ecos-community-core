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
define(['lib/knockout', 'citeck/utils/knockout.utils'], function(ko, koutils) {
    
    var logger = Alfresco.logger,
        koclass = koutils.koclass,
        $isNodeRef = Citeck.utils.isNodeRef,
        $isFilename = Citeck.utils.isFilename;
    
    var s = String,
        n = Number,
        b = Boolean,
        d = Date,
        o = Object,
        InvariantScope = koclass('invariants.InvariantScope'),
        Invariant = koclass('invariants.Invariant'),
        InvariantSet = koclass('invariants.InvariantSet'),
        DefaultModel = koclass('invariants.DefaultModel'),
        Message = koclass('invariants.Message'),
        Feature = koclass('invariants.Feature'),
        AttributeInfo = koclass('invariants.AttributeInfo'),
        Attribute = koclass('invariants.Attribute'),
        Node = koclass('invariants.Node'),
        NodeImpl = koclass('invariants.NodeImpl'),
        QName = koclass('invariants.QName'),
        Content = koclass('invariants.Content'),
        ContentFileImpl = koclass('invariants.ContentFileImpl'),
        ContentTextImpl = koclass('invariants.ContentTextImpl'),
        ContentFakeImpl = koclass('invariants.ContentFakeImpl'),
        Runtime = koclass('invariants.Runtime');
    
    var EnumerationServiceImpl = {
            
        _templates: {},
        
        _numbers: {},
        
        getTemplate: function(id) {
            if($isNodeRef(id)) return new Node(id);
            if(this._templates[id]) return this._templates[id]();
            var template = this._templates[id] = new ko.observable();
            Alfresco.util.Ajax.jsonGet({
                url: Alfresco.constants.PROXY_URI + "citeck/enumeration/template?id=" + id,
                successCallback: function(response) {
                    template(new Node(response.json));
                }
            });
            return template();
        },
        
        isTemplate: function(id) {
            var template = this.getTemplate(id);
            if(template == null) return false;
            return template.isSubType('count:autonumberTemplate');
        },
        
        getNumber: function(template, model, count) {
            var templateId = template instanceof Node ? template.nodeRef : 
                    _.isString(template) ? template : null;
            if(templateId == null) {
                throw "Can not get template id from specified argument: " + template;
            }
            
            var data = model instanceof Node ? { node: model.impl().allData.peek().attributes } :
                    _.isString(model) ? { node: model } :
                    _.clone(model);
            data.count = count || null;
            
            var templateNumbers = this._numbers[templateId];
            if(!templateNumbers) {
                templateNumbers = this._numbers[templateId] = [];
            }
            
            var numberRecord = _.find(templateNumbers, function(number) {
                return _.isEqual(data, number.model);
            });
            
            if(!numberRecord) {
                templateNumbers.push(numberRecord = {
                    model: data,
                    number: ko.observable()
                });
                
                Alfresco.util.Ajax.jsonPost({
                    url: Alfresco.constants.PROXY_URI + "citeck/enumeration/number?template=" + templateId,
                    dataObj: data,
                    successCallback: { fn: function(response) {
                        numberRecord.number(response.json.number);
                    } },
                    failureCallback: { fn: function(response) {
                        var index = templateNumbers.indexOf(numberRecord);
                        if(index != -1) templateNumbers.splice(index, 1);
                        Alfresco.util.PopupManager.displayPrompt({
                            text: response.json.message
                        });
                    } }
                });
            }
            
            return numberRecord.number();
        }
    };
    
    var DDClasses = koclass('dictionary.Classes'),
        DDClass = koclass('dictionary.Class'),
        DDProperty = koclass('dictionary.Property'),
        DDAssociation = koclass('dictionary.Association');
    
    DDClass
        .key('name', s)
        .property('qname', QName)
        .property('title', s)
        .property('isAspect', b)
        ;
    
    DDClasses
        .key('filter', s)
        .property('classes', [DDClass])
        .load('classes', koutils.simpleLoad({
            url: Alfresco.constants.PROXY_URI + "api/classesWithFullQname?cf={filter}",
            resultsMap: function(response) {
                return {
                    // note: for the purposes of UI we sort this array
                    // though it is not mandatory
                    classes: _.sortBy(_.map(response, function(item) {
                        return {
                            name: item.prefixedName,
                            qname: {
                                shortQName: item.prefixedName,
                                fullQName: item.name
                            },
                            title: item.title,
                            isAspect: item.isAspect
                        }
                    }), 'name')
                }
            }
        }))
        ;
    
    var DictionaryServiceImpl = {
        
        getAllTypes: function() {
            return _.invoke(new DDClasses('type').classes(), 'name');
        },
        
        getAllAspects: function() {
            return _.invoke(new DDClasses('aspect').classes(), 'name');
        },
        
    };
    
    var UtilsImpl = {
        
        shortQName: function(name) {
            return new QName(name).shortQName();
        },
        
        longQName: function(name) {
            return new QName(name).fullQName();
        }
        
    }
    
    var rootObjects = {
        message: function(key) {
            var value = new Message(key).value();
            if(value == null) return key;
            return YAHOO.lang.substitute(value, _.rest(arguments));
        },
        ko: ko,
        koutils: koutils,
        utils: UtilsImpl,
        dictionary: DictionaryServiceImpl,
        enumeration: EnumerationServiceImpl
    };
    
    function evalJavaScript(expression, model) {
        with(rootObjects) {
            with(model) {
                try {
                    return eval(expression);
                } catch(e) {
                    return undefined;
                }
            }
        }
    }
    
    // TODO support freemarker
    function evalFreeMarker(expression, model) {
        try {
            return _.template(expression, model, {
                interpolate: /\$\{(.+?)\}/g
            });
        } catch(e) {
            return undefined;
        }
    }
    
    function evalCriteriaQuery(criteria, model, pagination) {
        var query = {
                skipCount: 0,
                maxItems: 50
        };

        if (pagination) {
            if (pagination.maxItems) query.maxItems = pagination.maxItems;
            if (pagination.skipCount) query.skipCount = pagination.skipCount;
        }

        try {
            _.each(criteria, function(criterion, index) {
                query['field_' + index] = criterion.attribute;
                query['predicate_' + index] = criterion.predicate;
                var value = evalFreeMarker(criterion.value, model);
                if(value == null) {
                    throw {
                        message: "Expression evaluated with errors",
                        expression: criterion.value
                    };
                }
                query['value_' + index] = value;
            });
        } catch(e) {
            return null;
        }

        return query;
    }
    
    function evalCriteria(criteria, model, pagination) {
        var cache = model.cache;
        if(_.isUndefined(cache.result)) {
            cache.result = ko.observable(null);
        }
        
        var query = evalCriteriaQuery(criteria, model, pagination);
        if(query == null) {
            return null;
        }
     
        var previousResult = cache.result(); // always call this to create dependency
        if(cache.query) {
            if(_.isEqual(query, cache.query)) {
                return previousResult;
            }
        }
        
        cache.query = query;
        Alfresco.util.Ajax.jsonPost({
            url: Alfresco.constants.PROXY_URI + "search/criteria-search",
            dataObj: query,
            successCallback: {
                fn: function(response) {
                    var result = response.json.results;
                    result.pagination = response.json.paging;
                    result.query = response.json.query;
                    cache.result(result);
                }
            }
        });

        return undefined;
    }
    
    InvariantScope
        .property('class', s)
        .property('classKind', s)
        .property('attribute', s)
        .property('attributeKind', s)
        ;
    
    Invariant
        .property('scope', InvariantScope)
        .property('feature', s)
        .property('final', b)
        .property('description', s)
        .property('language', s)
        .property('expression', o)
        .method('evaluate', function(model) {
            if(this.language() == 'javascript') {
                return evalJavaScript(this.expression(), model);
            }
            if(this.language() == 'freemarker') {
                return evalFreeMarker(this.expression(), model);
            }
            if(this.language() == 'explicit') {
                return this.expression();
            }
            if(this.language() == 'criteria') {
                return evalCriteria(this.expression(), model);
            }
            throw "Language is not supported: " + this.language();
        })
        ;
    
    Message
        .key('key', s)
        .property('value', s)
        .load('value', koutils.bulkLoad(Citeck.utils.messageLoader, 'key', 'value'))
        ;
    
    DefaultModel
        .key('key', s)
        .property('person', Node)
        .property('companyhome', Node)
        .property('userhome', Node)
        .property('view', o)
        ;
    
    InvariantSet
        .key('key', s)
        .property('type', s)
        .property('aspects', [s])
        .property('attributes', [s])
        .property('invariants', [Invariant])
        .property('defaultModel', DefaultModel)
        .load([ 'invariants', 'defaultModel' ], koutils.simpleLoad({
            url: Alfresco.constants.PROXY_URI + "citeck/invariants?type={type}&aspects={aspects}&attributes={attributes}",
            resultsMap: { invariants: "invariants", defaultModel: "model" }
        }))
        ;
    
    var featureInvariants = function(featureName) {
        return function() {
            if(!this.invariants()) return [];
            var attrName = this.name(),
                attrKind = this.type(),
                attrType = attrKind == 'property' ? this.datatype() : this.nodetype();
            return _.filter(this.invariants().invariants(), function(invariant) {
                var scopeName = invariant.scope().attribute(),
                    scopeKind = invariant.scope().attributeKind();
                return invariant.feature() == featureName && (
                        scopeKind == attrKind           && (scopeName == attrName || scopeName == null) ||
                        scopeKind == attrKind + "_type" && (scopeName == attrType || scopeName == null)
                );
            }, this);
        }
    };
    
    var evalInvariants = function(invariants, model, requiredClass, defaultValue, isTerminate) {
        var invariantValue = null;
        var invariant = _.find(invariants, function(invariant) {
            invariantValue = invariant.evaluate(model);
            return isTerminate(invariantValue, invariant);
        }, this);
        return {
            invariant: invariant,
            value: koutils.instantiate(isTerminate(invariantValue, invariant) ? invariantValue : defaultValue, requiredClass)
        };
    }
    
    var featuredProperty = function(featureName, requiredClass, defaultValue, isTerminate) {
        return function() {
            return evalInvariants(
                    this[featureName + 'Invariants'](), 
                    this.invariantsModel(), 
                    requiredClass, 
                    defaultValue, 
                    isTerminate).value;
        };
    };
    
    var notNull = function(value) { return value !== null; }
    var isFalse = function(value) { return value === false; }
    
    var classMapping = {
        'java.lang.Object': o,
        'java.lang.String': s,
        'java.util.Date': d,
        'java.lang.Long': n,
        'java.lang.Float': n,
        'java.lang.Double': n,
        'java.lang.Integer': n,
        'java.lang.Boolean': b,
        'java.util.Locale': s,
        'org.alfresco.service.cmr.repository.MLText': s,
        'org.alfresco.service.cmr.repository.NodeRef': Node,
        'org.alfresco.service.namespace.QName': QName,
        'org.alfresco.service.cmr.repository.ContentData': Content,
        'org.alfresco.util.VersionNumber': s,
        'org.alfresco.service.cmr.repository.Period': s
    };
        
    var attributeLoader = new Citeck.utils.BulkLoader({
        url: Alfresco.constants.PROXY_URI + "citeck/invariants/attributes",
        method: "POST",
        emptyFn: function() { return {names:[]} },
        addFn: function(query, name) {
            if(query.names.indexOf(name) == -1) {
                query.names.push(name);
                return true;
            } else {
                return false;
            }
        },
        getFn: function(response) {
            var attributes = response.json.attributes;
            return _.object(_.pluck(attributes, 'name'), attributes);
        }
    });
    
    AttributeInfo
        .key('name', s)
        .property('type', s) // one of: property, association, child-association, ...
        .property('nodetype', s)
        .property('datatype', s)
        .property('javaclass', s)
        .load('*', koutils.bulkLoad(attributeLoader, 'name'))
        ;
    
    Attribute
        .key('key', s)
        .property('info', AttributeInfo)
        .shortcut('name', 'info.name')
        .shortcut('type', 'info.type')
        .shortcut('nodetype', 'info.nodetype')
        .shortcut('datatype', 'info.datatype')
        .shortcut('javaclass', 'info.javaclass')
        .constructor([Node, String], function(node, name) {
            var attr = new Attribute({
                key: node.key() + ":" + name,
                info: name
            });
            attr.node(node);
            return attr;
        }, true)
        .constructor([Node, String, Boolean], function(node, name, persisted) {
            var attr = new Attribute(node, name);
            attr.persisted(persisted);
            return attr;
        }, true)
        .constructor([Node, String, Boolean, Object], function(node, name, persisted, value) {
            var attr = new Attribute(node, name, persisted);
            attr.persistedValue(value);
            return attr;
        }, true)
        
        .computed('valueClass', function() {
            return classMapping[this.javaclass()] || null;
        })
        
        .method('convertValue', function(value, multiple) {
            var isArray = _.isArray(value),
                instantiate = _.partial(koutils.instantiate, _, this.valueClass());
            if(value == null) {
                return multiple 
                        ? []
                        : null;
            } else if(isArray) {
                return multiple
                        ? _.map(value, instantiate)
                        : instantiate(value[0]);
            } else {
                return multiple
                        ? [ instantiate(value) ]
                        :   instantiate(value) ;
            }
        })
        
        .property('node', Node)
        .shortcut('invariants', 'node.impl.invariants')
        
        .method('getInvariantsModel', function(value, cache) {
            var model = {};
            if(this.invariants() != null) {
                _.each(this.invariants().defaultModel(), function(property, name) {
                    Object.defineProperty(model, name, _.isFunction(property) ? { get: property } : { value: property });
                });
            }
            Object.defineProperty(model, 'node', { get: this.node });
            Object.defineProperty(model, 'value', typeof value == "function" ? { get: value } : { value: value });
            Object.defineProperty(model, 'cache', { value: cache });
            return model;
        })
        
        .computed('invariantsModel', function() {
            return this.getInvariantsModel(this.value, this.cache = this.cache || {});
        })
        
        // invariants lists
        .computed('valueInvariants', featureInvariants('value'))
        .computed('defaultInvariants', featureInvariants('default'))
        .computed('optionsInvariants', featureInvariants('options'))
        
        .computed('titleInvariants', featureInvariants('title'))
        .computed('descriptionInvariants', featureInvariants('description'))
        .computed('valueTitleInvariants', featureInvariants('value-title'))
        .computed('valueDescriptionInvariants', featureInvariants('value-description'))

        .computed('relevantInvariants', featureInvariants('relevant'))
        .computed('multipleInvariants', featureInvariants('multiple'))
        .computed('mandatoryInvariants', featureInvariants('mandatory'))
        .computed('protectedInvariants', featureInvariants('protected'))
        .computed('validInvariants', featureInvariants('valid'))
        
        // value properties:
        .property('newValue', o) // value, set by user
        .property('persistedValue', o) // value, persisted in repository
        .computed('invariantValue', featuredProperty('value', o, null, notNull))
        .computed('invariantDefault', featuredProperty('default', o, null, notNull))
        .computed('defaultValue', function() {
            return this.convertValue(this.invariantDefault(), this.multiple());
        })
        .shortcut('default', 'defaultValue', null)
        .computed('rawValue', function() {
            var invariantValue = this.invariantValue();
            if(invariantValue != null) return invariantValue;
            if(this.newValue.loaded()) return this.newValue();
            if(this.persisted()) return this.persistedValue();
            return this.node().impl().inViewMode() ? null : this.invariantDefault();
        })
        
        .property('persisted', b)
        
        .computed('value', {
            read: function() {
                return this.convertValue(this.rawValue(), this.multiple());
            },
            write: function(value) {
                // save as array to protect from loosing values
                this.newValue(this.convertValue(value, true));
            }
        })
        
        .computed('singleValue', {
            read: function() {
                return this.convertValue(this.rawValue(), false);
            },
            write: function(value) {
                this.value(value);
            }
        })
        
        .computed('multipleValues', {
            read: function() {
                return this.convertValue(this.rawValue(), true);
            },
            write: function(value) {
                this.value(value);
            }
        })
        
        .computed('lastValue', {
            read: function() {
                return this.single() ? this.value() : 
                    this.value().length == 0 ? null :
                    _.last(this.value());
            },
            write: function(value) {
                value = this.convertValue(value, false);
                if(this.single()) {
                    this.value(value);
                } else {
                    var currentValues = this.value();
                    if(!_.contains(currentValues, value)) {
                        this.value(_.union(currentValues, [value]))
                    }
                }
            }
        })
        
        .method('remove', function(index) {
            if(this.single()) {
                this.value(null);
            } else {
                var currentValues = this.value();
                if(index < currentValues.length) {
                    this.value(_.union(
                        _.first(currentValues, index),
                        _.rest(currentValues, index + 1)
                    ));
                }
            }
        })
        
        .computed('changed', function() {
            return this.newValue.loaded();
        })
        .method('reset', function(full) {
            this.newValue(null);
            this.newValue.reload();
            if(full) {
                this.persisted.reload();
                this.persistedValue.reload();
            }
        })
        
        .computed('textValue', {
            read: function() {
                return this.getValueText(this.value());
            },
            write: function(value) {
                if(value == null || value == "") {
                    return this.value(null);
                } else {
                    return this.value(value);
                }
            }
        })
        
        .method('getValueText', function(value) {
            if(value == null) return null;
            if(_.isArray(value)) return _.map(value, this.getValueText, this);
            
            var valueClass = this.valueClass();
            if(valueClass == null) return "" + value;
            if(valueClass == o) return value.toString();
            if(valueClass == s) return "" + value;
            if(valueClass == b) return value ? "true" : "false";
            if(valueClass == Node) return value.nodeRef;
            if(valueClass == QName) return value.shortQName();
            if(valueClass == Content) return value.content;
            
            var datatype = this.datatype();
            if(valueClass == n) {
                if(datatype == 'd:int' || datatype == 'd:long') {
                    return "" + Math.floor(value);
                }
                return "" + value;
            }
            if(valueClass == d) {
                if(datatype == 'd:date') {
                    var year = value.getFullYear(),
                        month = value.getMonth() + 1,
                        date = value.getDate();
                    return (year > 1000 ? "" : year > 100 ? "0" : year > 10 ? "00" : "000") + year 
                            + (month < 10 ? "-0" : "-") + month 
                            + (date  < 10 ? "-0" : "-") + date;
                }

                return Alfresco.util.toISO8601(value);
            }
            
            throw { 
                message: "Value class is not supported", 
                valueClass: valueClass,
                datatype: datatype
            };
        })
        
        .computed('jsonValue', {
            read: function() {
                return this.getValueJSON(this.value());
            },
            write: function(value) {
                if(value == null || value == "") {
                    return this.value(null);
                } else {
                    return this.value(value);
                }
            }
        })
        
        .method('getValueJSON', function(value) {
            if(value == null) return null;
            if(_.isArray(value)) return _.map(value, this.getValueJSON, this);
            
            var valueClass = this.valueClass();
            if(valueClass == null) return null;
            if(valueClass == o) return value.toString();
            if(valueClass == s) return "" + value;
            if(valueClass == n) return value.toString();
            if(valueClass == b) return value ? true : false;
            if(valueClass == Node) return value.nodeRef;
            if(valueClass == QName) return value.shortQName();
            if(valueClass == Content) return value.impl().jsonValue();
            
            var datatype = this.datatype();
            if(valueClass == n) {
                if(datatype == 'd:int' || datatype == 'd:long') {
                    return Math.floor(value);
                }
                return value;
            }
            if(valueClass == d) {
                if(datatype == 'd:date') {
                    var year = value.getFullYear(),
                        month = value.getMonth() + 1,
                        date = value.getDate();
                    return (year > 1000 ? "" : year > 100 ? "0" : year > 10 ? "00" : "000") + year 
                            + (month < 10 ? "-0" : "-") + month 
                            + (date  < 10 ? "-0" : "-") + date;
                }
                return Alfresco.util.toISO8601(value);
            }
            
            throw { 
                message: "Value class is not supported", 
                valueClass: valueClass,
                datatype: datatype
            };
        })
        
        // options properties
        .computed('invariantOptions', featuredProperty('options', o, null, notNull))
        .computed('options', function() {
            var options = this.invariantOptions();
            return options ? this.convertValue(options, true) : null;
        })
        
        .method('filterOptions', function(criteria, pagination) {
            // find invariant with correct query
            var model = this.getInvariantsModel(this.value, criteria.cache = criteria.cache || {}),
                optionsInvariant = _.find(this.optionsInvariants(), function(invariant) {
                    if(invariant.language() == "criteria") {
                        var query = evalCriteriaQuery(_.union(invariant.expression(), criteria), model);
                        return query != null;
                    } else { return true }
                });

            if(optionsInvariant == null) return [];
            if(optionsInvariant.language() != "criteria") return this.options();

            var options = evalCriteria(_.union(optionsInvariant.expression(), criteria), model, pagination);
            if (options != null) {
                var optionsWithConvertedValues = this.convertValue(options, true);
                if (options.pagination) optionsWithConvertedValues.pagination = options.pagination;
                return optionsWithConvertedValues;
            }

            return null
        })
        
        .method('getValueTitle', function(value) {
            return evalInvariants(this.valueTitleInvariants(), 
                    this.getInvariantsModel(value), String, '', notNull).value;
        })
        .computed('valueTitle', function() {
            return this.getValueTitle(this.value());
        })
        .shortcut('value-title', 'valueTitle')

        .method('getValueDescription', function(value) {
            return evalInvariants(_.union(this.valueDescriptionInvariants(), this.valueTitleInvariants()),
                    this.getInvariantsModel(value), String, '', notNull).value;
        })
        .computed('valueDescription', function() {
            return this.getValueDescription(this.value());
        })
        .shortcut('value-description', 'valueDescription')

        .computed('title', featuredProperty('title', s, '', notNull))
        .computed('description', featuredProperty('description', s, '', notNull))
        .computed('multiple', featuredProperty('multiple', b, false, notNull))
        .computed('mandatory', featuredProperty('mandatory', b, false, notNull))

        .computed('invariantRelevant', featuredProperty('relevant', b, true, notNull))
        .computed('relevant', function() {
            var exposedAttributes = this.invariants().attributes();
            if(!_.isEmpty(exposedAttributes) // is a view attribute
            && !_.contains(exposedAttributes, this.name())) // is not exposed
                return false; // non-exposed attributes are always irrelevant
            return this.invariantRelevant();
        })
        
        .computed('invariantProtected', featuredProperty('protected', b, false, notNull))
        .computed('protected', function() {
            var invariantValue = this.invariantValue();
            if(invariantValue != null && (!_.isArray(invariantValue) || invariantValue.length > 0)) return true;
            return this.invariantProtected();
        })
        
        .computed('empty', function() {
            return this.value() == null 
                || this.multiple() && this.value().length == 0 
                || this.valueClass() == String && this.value().length == 0;
        })
        
        .computed('evaluatedValid', function() {
            return evalInvariants(this.validInvariants(), this.invariantsModel(), b, true, isFalse);
        })
        .computed('invariantValid', function() {
            return this.evaluatedValid().value;
        })
        .computed('valid', function() {
            // irrelevant is always valid
            if(this.irrelevant()) return true;
            // empty values: valid, if optional or protected
            if(this.empty()) {
                return this.optional() || this['protected']();
            }
            // non-empty values: valid invariants should all be valid
            return this.invariantValid();
        })
        
        .computed('validationMessage', function() {
            // mimic validation behaviour:
            if(this.irrelevant()) return "";
            
            if(this.empty()) {
                return this.optional() || this['protected']() ? "" : Alfresco.util.message("validation-hint.mandatory");
            }
            
            var invariant = this.evaluatedValid().invariant;
            return invariant != null ? invariant.description() : "";
        })
        
        .computed('single', function() {
            return !this.multiple();
        })
        .computed('optional', function() {
            return !this.mandatory();
        })
        .computed('irrelevant', function() {
            return !this.relevant();
        })
        .computed('invalid', function() {
            return !this.valid();
        })
        .computed('unchanged', function() {
            return !this.changed();
        })
        
        // persisted value loading
        .load(['persisted', 'persistedValue'], function(attr) {
            if(!attr.node().impl().isPersisted()) {
                attr.persisted(false);
                return;
            }
            Citeck.utils.attributeValueLoader.loadValue(attr.node().nodeRef, attr.name(), function(key, response) {
                attr.persisted(response.persisted);
                if(response.persisted) {
                    attr.persistedValue(response.value);
                }
            });
        })
        ;
    
    NodeImpl
        .constructor([Node, Object], function(node, model) {
            var that = NodeImpl.call(this, node.key());
            that.node(node);
            that.updateModel(model);
            return that;
        })
        
        .key('key', s)
        .property('nodeRef', s)
        .computed('isPersisted', function() {
            return this.nodeRef() != null;
        })
        .property('node', Node)
        .property('type', s)
        .shortcut('typeShort', 'type')
        .computed('typeFull', function() {
            if(this.type() == null) return null;
            var qnameType = new QName(this.type());
            return qnameType.fullQName();
        })
        
        .property('_attributes', o)
        .computed('attributes', function() {
            var node = this.node(),
                attributes = [],
                createdNames = {},
                persisted = this.isPersisted();
            _.each(this._attributes(), function(value, name) {
                createdNames[name] = true;
                if(_.isUndefined(value)) {
                    attributes.push(new Attribute(node, name));
                } else {
                    attributes.push(new Attribute(node, name, true, value));
                }
            });
            var processAttributeName = function(name) {
                if(!createdNames[name]) {
                    createdNames[name] = true;
                    attributes.push(persisted 
                            ? new Attribute(node, name) 
                            : new Attribute(node, name, false));
                }
            };
            _.each(this.definedAttributeNames(), processAttributeName);
            _.each(this.forcedAttributes(), processAttributeName);
            return attributes;
        })
        .property('definedAttributeNames', [s])
        .property('invariants', InvariantSet)
        .property('permissions', o)
        
        .computed('inViewMode', function() {
            return this.resolve('invariants.defaultModel.view.mode') == "view";
        })
        
        .computed('parent', function() {
            var parent = this.attribute('attr:parent');
            return parent ? parent.value() : null;
        })
        .computed('types', function() {
            var types = this.attribute('attr:types');
            return types ? types.value() : [];
        })
        .computed('aspects', function() {
            var aspects = this.attribute('attr:aspects');
            return aspects ? aspects.value() : [];
        })
        
        .load('invariants', function(node) {
            var updateInvariants = function() {
                // note: aspects are necessary to load invariants
                // and invariants are necessary to display aspects correctly
                // so we need to fetch aspects by hand
                var aspects = _.pluck(node.attribute('attr:aspects').rawValue(), 'shortQName');
                node.invariants(new InvariantSet({
                    key: node.key(),
                    type: node.type(),
                    aspects: aspects
                }));
            };
            var checkPrerequisites = function() {
                if(node.type() == null) {
                    koutils.subscribeOnce(node.type, checkPrerequisites);
                } else if(_.isEmpty(node.aspects())) {
                    koutils.subscribeOnce(node.aspects, checkPrerequisites);
                } else {
                    updateInvariants();
                }
            };
            checkPrerequisites();
        })
        
        .shortcut('forcedAttributes', 'invariants.attributes', [])
        
        .method('updateModel', function(model) {
            this.model(_.omit(model, 'attributes'));
            if(model.attributes) {
                this.model({ _attributes: model.attributes })
            }
        })
        
        .load('definedAttributeNames', koutils.simpleLoad({
            url: function(impl) {
                return Alfresco.constants.PROXY_URI + "citeck/attributes/defined?" + (impl.isPersisted() ? "nodeRef=" + impl.nodeRef() : "type=" + impl.type());
            },
            resultsMap: { definedAttributeNames : 'attributes' }
        }))
        
        .load('*', function(impl) {
            // existing nodes:
            if(impl.isPersisted()) {
                Citeck.utils.nodeInfoLoader.load(impl.nodeRef(), function(nodeRef, model) {
                    impl.updateModel(model);
                });
            }
        })
        .method('attribute', function(name) {
            return _.find(this.attributes() || [], function(attr) {
                return attr.name() == name;
            });
        })
        
        .method('reset', function(full) {
            _.invoke(this.attributes(), 'reset', full);
            if(full) this._attributes.reload();
        })
        
        .computed('valid', function() {
            return _.all(this.attributes(), function(attr) {
                return attr.valid();
            });
        })
        .computed('changed', function() {
            return _.any(this.attributes(), function(attr) {
                return attr.changed();
            });
        })
        
        .computed('invalid', function() {
            return !this.valid();
        })
        .computed('unchanged', function() {
            return !this.changed();
        })

        .computed('data', function() {
            var attributes = {};
            _.each(this.attributes(), function(attr) {
                if(attr.relevant() && !attr['protected']()) {
                    attributes[attr.name()] = attr.jsonValue();
                }
            });
            return {
                nodeRef: this.nodeRef(),
                attributes: attributes
            };
        })

        .computed('allData', function() {
            var attributes = {};
            _.each(this.attributes(), function(attr) {
                if(attr.relevant()) {
                    attributes[attr.name()] = attr.jsonValue();
                }
            });
            return {
                nodeRef: this.nodeRef(),
                attributes: attributes
            };
        })
        
        .property('inSubmitProcess', b)
        .init(function() {
        	this.inSubmitProcess(false);
        })
        ;
    
    var assocsComputed = function(type) {
        return function() {
            var assocs = {};
            _.each(this.impl().attributes(), function(attr) {
                if(attr.type() != type) return;
                var config = {
                    configurable: false,
                    enumerable: true,
                    get: function() {
                        var value = attr.value();
                        if(value == null) return [];
                        if(_.isArray(value)) return value;
                        return [ value ];
                    }
                };
                Object.defineProperty(assocs, attr.name(), config);
            }, this);
            return assocs;
        }
    };
    
    Node
        .key('key', s)
        .property('impl', NodeImpl)
        
        .constructor([String], function(key) {
        }, true)
        .init(function() {
            if(this.impl() == null) {
                this.impl(new NodeImpl(this, {
                    nodeRef: this.key()
                }));
            }
        })
        .constructor([Object], function(model) {
            var that = Node.call(this, model.key || model.nodeRef);
            that.impl(new NodeImpl(that, model));
            return that;
        })
        
        .nativeProperty('nodeRef', function() {
            return this.impl().nodeRef();
        })
        .nativeProperty('storeType', function() {
            return this.nodeRef.replace(/^(.+):\/\/(.+)\/(.+)$/, '$1');
        })
        .nativeProperty('storeId', function() {
            return this.nodeRef.replace(/^(.+):\/\/(.+)\/(.+)$/, '$2');
        })
        .nativeProperty('id', function() {
            return this.nodeRef.replace(/^(.+):\/\/(.+)\/(.+)$/, '$3');
        })
        
        .nativeProperty('type', function() {
            return this.resolve('impl.typeFull', null);
        })
        .nativeProperty('typeShort', function() {
            return this.resolve('impl.typeShort', null);
        })
        .method('isSubType', function(name) {
            return _.any(this.resolve('impl.types', []), function(type) {
                return type.shortQName() == name || type.fullQName() == name;
            });
        })
        .nativeProperty('isCategory', function() {
            this.isSubType('cm:category');
        }, true)
        .nativeProperty('isContainer', function() {
            this.isSubType('cm:folder');
        }, true)
        .nativeProperty('isDocument', function() {
            this.isSubType('cm:content');
        }, true)
        
        .nativeProperty('aspects', function() {
            return _.map(this.resolve('impl.aspects'), function(aspect) {
                return aspect.shortQName();
            });
        }, true)
        .method('hasAspect', function(name) {
            return _.find(this.resolve('impl.aspects'), function(aspect) {
                return aspect.shortQName() == name || aspect.fullQName() == name;
            });
        })

        .nativeProperty('parent', function() {
            return this.impl().parent();
        })
        .nativeProperty('properties', function() {
            var properties = {};
            _.each(this.impl().attributes(), function(attr) {
                if(attr.type() != 'property') return;
                var config = {
                    configurable: false,
                    enumerable: true,
                    get: attr.value,
                    set: attr.value,
                };
                Object.defineProperty(properties, attr.name(), config);
                if(attr.name().match(/^cm:/)) {
                    Object.defineProperty(properties, attr.name().replace(/^cm:/, ''), config);
                }
            });
            return properties;
        }, true)
        .nativeProperty('assocs', assocsComputed('association'), true)
        .nativeProperty('associations', assocsComputed('association'), true)
        .nativeProperty('childAssocs', assocsComputed('child-association'), true)
        .nativeProperty('childAssociations', assocsComputed('child-association'), true)
        .nativeProperty('name', {
            get: function() {
                return this.properties.name;
            },
            set: function(value) {
                this.properties.name = value;
            }
        })
        
        .method('hasPermission', function(permission) {
            return this.resolve('impl.permissions.' + permission, false) == true;
        })
        
        .save(koutils.simpleSave({
            url: function(node) {
                var baseUrl = Alfresco.constants.PROXY_URI + "citeck/invariants/view?";
                if($isNodeRef(node.nodeRef)) {
                    return baseUrl + "nodeRef=" + node.nodeRef;
                } else {
                    return baseUrl + "type=" + node.typeShort;
                }
            },
            toRequest: function(node) {
            	node.impl().inSubmitProcess(true);
                return {
                    view: node.impl().invariants().defaultModel().view(),
                    attributes: node.impl().data().attributes
                };
            },
            toResult: function(response) {
                return new Node(response.result);
            },
            toFailureMessage: function(response) {
            	return response.message;
            },
            onSuccess: function(node, result) {
            	node.impl().inSubmitProcess(false);
            },
            onFailure: function(node, message) {
            	node.impl().inSubmitProcess(false);
            }
        }))
        ;
    
    QName
        .key('key', s)
        
        .property('prefix', s)
        .property('uri', s)
        .property('localName', s)
        
        .computed('shortQName', function() {
            return this.prefix() ? this.prefix() + ":" + this.localName() : this.localName();
        })
        .computed('fullQName', function() {
            return this.uri() ? "{" + this.uri() + "}" + this.localName() : this.localName();
        })
        
        .constructor([String], function(key) {
            var shortQNamePattern = /^(.+)[:](.+)$/;
            var fullQNamePattern = /^[{](.+)[}](.+)$/;
            if(key.match(shortQNamePattern)) {
                this.model({
                    prefix: key.replace(shortQNamePattern, '$1'),
                    localName: key.replace(shortQNamePattern, '$2')
                });
            } else if(key.match(fullQNamePattern)) {
                this.model({
                    uri: key.replace(fullQNamePattern, '$1'),
                    localName: key.replace(fullQNamePattern, '$2')
                });
            } else {
                this.model({
                    prefix: '',
                    uri: '',
                    localName: key
                });
            }
        }, true)
        
        .constructor([QName], function(qname) {
            return qname;
        })
        
        .constructor([Object], function(model) {
            return new QName(model.shortQName || model.fullQName);
        }, false)
        
        .load('prefix', koutils.bulkLoad(Citeck.utils.nsPrefixLoader, 'uri', 'prefix'))
        .load('uri', koutils.bulkLoad(Citeck.utils.nsURILoader, 'prefix', 'uri'))
        
        ;

    //  for file-upload control
    ContentFileImpl
        .key('nodeRef', s)
        .property('filename', s)
        .property('encoding', s)
        .property('content', s)
        .property('mimetype', s)
        .property('size', n)
        
        .computed('jsonValue', function() {
            return {
                url: this.nodeRef()
            }
        })

        // get content
        .load('content', function(content) {
            var nodeRef = content.nodeRef();
            YAHOO.util.Connect.asyncRequest(
                'GET', 
                Alfresco.constants.PROXY_URI + "citeck/print/content?nodeRef=" + content.nodeRef(), 
                {
                    success: function(response) {
                        var result = response.responseText;
                        this.content(result);
                    },

                    failure: function(response) {
                        // error
                    },

                    scope: this
                }
            );
        })

        // get properties
        .load(['filename', 'encoding', 'mimetype', 'size'], koutils.simpleLoad({
            url: Alfresco.constants.PROXY_URI + "citeck/node-content?nodeRef={nodeRef}",
            resultsMap: {
                filename: 'name',
                encoding: 'content.encoding',
                mimetype: 'content.mimetype',
                size: 'content.size'
            }
        }))
        ;

    // for textarea control
    ContentTextImpl
        .constructor([String], function(content) {
            var self = ContentTextImpl.call(this);
            self.model({
                content: content,
                mimetype: "text/plain",
                encoding: "UTF-8"
            })
            return self;
        })

        .property('nodeRef', s)
        .property('encoding', s)
        .property('content', s)
        .property('mimetype', s)
        .computed('size', function() {
            var content = this.content();
            return content ? content.length : 0;
        })

        .computed('jsonValue', function() {
            return {
                mimetype: this.mimetype(),
                encoding: this.encoding(),
                content: this.content(),
                size: this.size()
            }
        })

        ;

    ContentFakeImpl
        .property('filename', s)
        .property('encoding', s)
        .property('mimetype', s)
        .property('size', n)
        
        .constant('jsonValue', null)

        ;

    Content
        .property('impl', o)

        .constructor([String], function(string) {
            var self = Content.call(this);
            if ($isNodeRef(string)) {
                self.impl(new ContentFileImpl(string));
            } else {
                self.impl(new ContentTextImpl(string));
            }

            return self;
        })

        .constructor([Object], function(object) {
            var self = Content.call(this);

            if (object) {
                if (object.url) {
                    self.impl(new ContentFileImpl(object.url));
                } else  if (object.content) {
                    self.impl(new ContentTextImpl(string));
                } else  if (object.filename) {
                    self.impl(new ContentFakeImpl(object));
                }
            }

            return self;
        })

        .constructor([Node], function(node) {
            var self = Content.call(this);
            self.impl(new ContentFileImpl(node.nodeRef));
            return self;
        })

        .nativeProperty('filename', {
            get: function() { 
                return this.impl().filename();
            }
        })
        .nativeProperty('nodeRef', {
            get: function() { 
                return this.impl().nodeRef();
            }
        })


        //  content api
        .nativeProperty('content', {
            get: function() {
                return this.impl().content();
            },
            set: function(value) {
                this.impl().content(value);
            }
        })
        .nativeProperty('mimetype', {
            get: function() { 
                return this.impl().mimetype();
            },
            set: function(value) { 
                this.impl().mimetype(value); 
            }
        })
        .nativeProperty('size', {
            get: function() { 
                return this.impl().size();
            }
        })
        .nativeProperty('encoding', {
            get: function() { 
                return this.impl().encoding();
            }
        })

        // download link
        .nativeProperty('downloadURL', {
            get: function() { 
                return Alfresco.constants.PROXY_URI + "citeck/print/content?nodeRef=" + this.nodeRef;
            }
        })
        ;
    
    Runtime
        .key('key', s)
        .property('node', Node)
        .property('invariants', InvariantSet)
        .constant('rootObjects', rootObjects)
        
        .method('submit', function() {
            if(this.node().impl().valid()) {
                this.broadcast('node-view-submit');
            }
        })
        .method('cancel', function() {
            this.broadcast('node-view-cancel');
        })
        .method('broadcast', function(eventName) {
            YAHOO.Bubbling.fire(eventName, {
                key: this.key(),
                runtime: this,
                node: this.node()
            });
        })
        ;
    
    
    // performance tuning
    var rateLimit = { rateLimit: { timeout: 0, method: "notifyWhenChangesStop" } };
    Attribute.extend('*', rateLimit);
    
    var InvariantsRuntime = function(htmlid) {
        InvariantsRuntime.superclass.constructor.call(this, "Citeck.invariants.InvariantsRuntime", htmlid);
        this.runtime = new Runtime({});
    };
    
    YAHOO.extend(InvariantsRuntime, Alfresco.component.Base, {
        
        onReady: function() {
            koutils.enableUserPrompts();
            this.runtime.model(this.options.model);
            ko.applyBindings(this.runtime, Dom.get(this.id));
        }
        
    });
    
    return InvariantsRuntime;
})