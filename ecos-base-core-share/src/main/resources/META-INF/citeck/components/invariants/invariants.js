/*
 * Copyright (C) 2008-2017 Citeck LLC.
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
define(['lib/knockout', 'citeck/utils/knockout.utils', 'lib/moment'], function(ko, koutils, moment) {
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
        ExplicitInvariantSet = koclass('invariants.ExplicitInvariantSet', InvariantSet),
        GroupedInvariantSet = koclass('invariants.GroupedInvariantSet', InvariantSet),
        ClassInvariantSet = koclass('invariants.ClassInvariantSet', InvariantSet),
        MultiClassInvariantSet = koclass('invariants.MultiClassInvariantSet', InvariantSet),
        SingleClassInvariantSet = koclass('invariants.SingleClassInvariantSet', InvariantSet),
        DefaultModel = koclass('invariants.DefaultModel'),
        Message = koclass('invariants.Message'),
        Feature = koclass('invariants.Feature'),
        AttributeSet = koclass('invariants.AttributeSet'),
        AttributeInfo = koclass('invariants.AttributeInfo'),
        Attribute = koclass('invariants.Attribute'),
        Predicate = koclass('invariants.Predicate'),
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
        DDAssociation = koclass('dictionary.Association'),
        DDProperties = koclass('dictionary.Properties');

    DDProperties
        .key('parentDocType', s)
        .property('properties', [QName])
        .load('properties', function () {
            var load = function () {
                var properties = [];
                var attributes = [];
                Alfresco.util.Ajax.jsonGet({
                    url: Alfresco.constants.PROXY_URI + "/api/fullChildrenClasses",
                    dataObj: {'cf': 'all', 'ctbp': this.parentDocType(), 'wap': 'true'},
                    successCallback: {
                        scope: this,
                        fn: function (response) {
                            var classDefs = response.json;
                            for (var c in classDefs) {
                                properties = _.keys(classDefs[c].properties);
                                break;
                            }
                            for (var p in properties) {
                                attributes.push(new QName(properties[p]));
                            }
                            this.model({
                                properties: attributes
                            });
                            return attributes;
                        }
                    }
                });
            };
            load.call(this);
        })
    ;

    DDClass
        .key('name', s)
        .property('qname', QName)
        .property('title', s)
        .property('isAspect', b)
        .property('attributes', [ QName ])
        .load('attributes', koutils.bulkLoad(Citeck.utils.definedAttributesLoader, 'name', 'attributes'))
        ;

    DDClasses
        .key('filter', s)
        .property('classes', [ DDClass ])
        .property('ctbp', s)
        .init(function() {
            if (this.ctbp.loaded() == false) {
                this.ctbp("idocs:doc");
            }
        })
        .load('classes', koutils.simpleLoad({
            url: Alfresco.constants.PROXY_URI + "api/childrenClassesWithFullQname?cf={filter}&ctbp={ctbp}",
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

        getSubTypes: function(type) {
            var ddClasses = new DDClasses('type');
            ddClasses.ctbp(type);
            return _.invoke(ddClasses.classes(), 'name');
        },

        getTitle: function(name) {
            new DDClasses('all').ctbp("").classes();
            return new DDClass(name.key()).title();
        },

        getProperties: function (parentDocType) {
            return DDProperties(parentDocType).properties();
        }

    };


    var JournalService = koutils.koclass('journals.JournalsService')
        .property('journalTypes', [o])
        .load('journalTypes', koutils.simpleLoad({
            url: Alfresco.constants.PROXY_URI + "api/journals/maptypes",
            resultsMap: function(response) {
                return {
                    journalTypes: _.map(response, function(item) {
                        return {
                            journalType: item.journalType,
                            type: item.type
                        }
                    })
                }
            }
        }))
        .method('getAllJournalTypes', function() {
            var allJournalTypes = [];
            _.each(this.journalTypes(), function(value) {
                allJournalTypes.push(value.journalType)
            });
            return allJournalTypes;
            })
        .method('getJournalType', function(journalTypeId) {
            var journalType;
            _.each(this.journalTypes(), function(value) {
                if (value.type == QName(journalTypeId).key()) {
                    journalType = value.journalType;
                }
            })
            if(_.isUndefined(journalType)) {
                return this.getAllJournalTypes();
            } else {
                return journalType;
            }
        });

    var JournalServiceImpl = new JournalService();

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

        searchQuery: function(node, attrName, query, schema, cacheAge, params) {
            var originalFormat = params ? params.originalFormat : false;
            var searchQueryDataName = params && params.invariantName ? "searchQueryData" + params.invariantName : "searchQueryData";

            var attribute = node.impl().attribute(attrName),
                attInfo = attribute.info();

            if (!attInfo[searchQueryDataName] || attInfo[searchQueryDataName].query() != query) {
                attInfo[searchQueryDataName] = {
                    query: ko.observable(""),
                    nodes: ko.observableArray([]),
                    result: ko.observable(),
                    schema: null,
                    cacheAge: null
                };
                attInfo[searchQueryDataName].query.subscribe(function(value) {

                    var dataObj = { query: value };
                    if (this.schema)
                        dataObj.schema = _.isObject(this.schema) ? JSON.stringify(this.schema) : this.schema;
                    if (this.cacheAge) dataObj.cacheAge = this.cacheAge;

                    Alfresco.util.Ajax.jsonGet({
                        url: Alfresco.constants.PROXY_URI + "citeck/search/query",
                        dataObj: dataObj,
                        successCallback: {
                            scope: this,
                            fn: function(response) {
                                var results = response.json.results;

                                if (!originalFormat) {
                                    var nodes = [];
                                    for (var n in results) nodes.push(new Node(results[n]));
                                    this.nodes(_.map(results, function(result) {
                                       return new Node(result);
                                    }));
                                } else { attInfo[searchQueryDataName].result(results); }
                                
                            }
                        }
                    });
                }, attInfo[searchQueryDataName]);
            }

            attInfo[searchQueryDataName].cacheAge = cacheAge;
            attInfo[searchQueryDataName].schema = schema;
            attInfo[searchQueryDataName].query(query);

            if (originalFormat) return attInfo[searchQueryDataName].result();
            return attInfo[searchQueryDataName].nodes();
        },
        ko: ko,
        koutils: koutils,
        utils: UtilsImpl,
        dictionary: DictionaryServiceImpl,
        enumeration: EnumerationServiceImpl,
        journals: JournalServiceImpl,
        moment: moment
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

    function evalFreeMarker(expression, model) {
        try {
            var liveTemplate = _.template(expression, { interpolate: /\$\{(.+?)\}/g });
            return liveTemplate(model);
        } catch(e) {
            return undefined;
        }
    }

    function evalCriteriaQuery(criteria, model, pagination) {
        var query = {
            skipCount: 0,
            sortBy: [{attribute: "sys:node-dbid", order: "asc"}]
        };

        if (pagination) {
            if (pagination.maxItems) query.maxItems = pagination.maxItems;
            if (pagination.skipCount) query.skipCount = pagination.skipCount;
            if (pagination.sortBy) {
                if (_.isArray(pagination.sortBy)) query.sortBy = pagination.sortBy;
                if (_.isObject(pagination.sortBy)) query.sortBy = [ pagination.sortBy ];
            }
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
        if(query == null) return null;

        var previousResult = cache.result(); // always call this to create dependency
        if(cache.query) {
            if(_.isEqual(query, cache.query)) {
                return previousResult;
            }
        }

        // select search script by parameter from pagination
        // criteria-search by default
        var searchScripts = {
                "light-search": "citeck/light-search",
                "criteria-search": "search/criteria-search"
            },
            selectedSearchScript = pagination ? pagination.searchScript : undefined,
            searchScriptUrl = selectedSearchScript ? searchScripts[selectedSearchScript] : searchScripts["criteria-search"];

        cache.query = query;
        Alfresco.util.Ajax.jsonPost({
            url: Alfresco.constants.PROXY_URI + searchScriptUrl,
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

    // TODO:
    // - cache for default invariants

    InvariantScope
        .property('class', s)
        .property('classKind', s)
        .property('attribute', s)
        .property('attributeKind', s)
        ;

    Invariant
        .property('scope', InvariantScope)
        .shortcut('classScope', 'scope.class')
        .shortcut('attributeScope', 'scope.attribute')
        .shortcut('classScopeKind', 'scope.classKind')
        .shortcut('attributeScopeKind', 'scope.attributeKind')
        .property('feature', s)
        .property('final', b)
        .shortcut('isFinal', 'final')
        .property('description', s)
        .property('language', s)
        .property('priority', s)
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

        // TODO: load
        ;

    var COMMON_DEFAULT_MODEL_KEY = "default",
        COMMON_INVARIANTS_KEY = "default";

    var invariantsLoader = new Citeck.utils.BulkLoader({
            url: Alfresco.constants.PROXY_URI + "citeck/invariants",
            method: "GET",
            emptyFn: function() { return { aspects: [] } },
            addFn: function(query, className) {
                if(className != COMMON_INVARIANTS_KEY) query.aspects.push(className);
            },
            getFn: function(response) {
                return _.groupBy(response.json.invariants, function(invariant) {
                    return invariant.scope["class"] || COMMON_INVARIANTS_KEY;
                });
            }
        }),
        viewAttributeNamesByNodeRefLoader = new Citeck.utils.BulkLoader({
            url: Alfresco.constants.PROXY_URI + "citeck/invariants/view-attributes",
            method: "POST",
            emptyFn: function() { return { nodeRefs: [] } },
            addFn: function(query, nodeRef) {
                if (query.nodeRefs.indexOf(nodeRef) == -1) query.nodeRefs.push(nodeRef);
            },
            getFn: function(response) {
                var result = response.json || eval("(" + response.serverResponse.responseText + ")");
                _.each(result, function(value, key, object) {
                    object[key] = _.filter(value, function(attributeName) { return !!attributeName; });
                })
                return result;
            }
        }),
        viewAttributeNamesByTypeLoader = new Citeck.utils.BulkLoader({
            url: Alfresco.constants.PROXY_URI + "citeck/invariants/view-attributes",
            method: "POST",
            emptyFn: function() { return { types: [] } },
            addFn: function(query, type) {
                if (query.types.indexOf(type) == -1) query.types.push(type);
            },
            getFn: function(response) {
                var result = response.json || eval("(" + response.serverResponse.responseText + ")");
                _.each(result, function(value, key, object) {
                    object[key] = _.filter(value, function(attributeName) { return !!attributeName; });
                })
                return result;
            }
        });

    InvariantSet
        /*.property('invariants', [Invariant])*/
        .computed('groupedInvariants', function() {
            return _.groupBy(this.invariants(), this.getInvariantKey, this);
        })
        .method('getInvariantKey', function(invariant) {
            return this.getKey(
                    invariant.scope().attributeKind(),
                    invariant.scope().attribute(),
                    invariant.feature(),
                    invariant.isFinal());
        })
        .method('getKey', function(kind, name, feature, isFinal) {
            var sep = "::";
            return kind + (name ? sep + name : '') + sep + feature + (isFinal ? sep + 'final' : '');
        })
        ;

    ExplicitInvariantSet
        .key('className', s)
        .property('invariants', [ Invariant ])
        ;

    GroupedInvariantSet
        .key('key', s)
        .computed('invariants', {
            read: function() {
                var invariants = [], createdInvariants = [],
                    processInvariant = function(invariant) {
                        if (createdInvariants.indexOf(invariant) == -1) {
                            invariants.push(new Invariant(invariant));
                            createdInvariants.push(invariant);
                        }
                    };

                _.each(this.forcedInvariants(), processInvariant);

                return invariants;
            },
            pure: true,
            deferEvaluation: false
        })

        .property('forcedInvariants', o)
        ;

    ClassInvariantSet
        .key('className', s)
        .property('invariants', [ Invariant ])
        .load('invariants', koutils.bulkLoad(invariantsLoader, "className", "invariants"))
        ;

    MultiClassInvariantSet
        .key('classNames', s)
        .computed('invariants', function() {
            var classNames = this.classNames().split(','),
                invariants = _.flatten(_.map(classNames, function(className) {
                    return new ClassInvariantSet(className).invariants();
                }));
            
            var priorityGroups = _.groupBy(invariants, function(invariant) {
                if (invariant.isFinal()) return 1;

                if (invariant.attributeScopeKind().match(/_type$/)) return 2;

                var priorities = { "common": 10, "module": 11, "extend": 12, "custom": 13, "view-scoped": 14 },
                    priority = priorities[invariant.priority()];
                if (priority) return priority;

                var classScope = invariant.classScope();
                if (classNames.indexOf(classScope) != -1)
                    return 100 + classNames.indexOf(classScope);

                return 1000;
            });

            return _.flatten(_.values(priorityGroups));
        })
        ;

    SingleClassInvariantSet
        .constructor([ Object ], function(model) { this.setModel(model); }, true)

        .key('className', s)
        .property('nodeRef', s)
        .property('type', s)
        .property('attributeNames', [ s ])

        .property('_invariants', o)
        .property('_cache', o)

        .computed('invariants', function() {
            var invariants = [];
            if (this._cache()) {
                var defaultInvariants = this.defaultInvariants(),
                    specifiedInvariants = this.specifiedInvariants();

                if (!defaultInvariants.length || !specifiedInvariants.length) return [];
                invariants = defaultInvariants.concat(specifiedInvariants);
            } else {
                invariants = _.map(this._invariants(), function(invariant) {
                    return new Invariant(invariant);
                })
            }
           
            var priorityGroups = _.groupBy(invariants, function(invariant) {
                    if (invariant.isFinal()) return 1;

                    if (invariant.attributeScopeKind().match(/_type$/)) return 2;

                    var priorities = { "common": 10, "module": 11, "extend": 12, "custom": 13, "view-scoped": 14 },
                        priority = priorities[invariant.priority()];
                    if (priority) return priority;

                    var classScope = invariant.classScope();
                    if (classNames.indexOf(classScope) != -1)
                        return 100 + classNames.indexOf(classScope);

                    return 1000;
                });

            return _.flatten(_.values(priorityGroups));
        })
        .computed('defaultInvariants', {
            read: function() {
                if (!this._invariants()) return [];
                var defaultInvariantGroups = this.attributeNames().concat(["general", "base"]);
                return this._cache().getDefaultInvariants(defaultInvariantGroups); 
            },
            write: function (invariants) { 
                return this._cache().addDefaultInvariants(invariants); 
            },
            scope: this
        })
        .computed('specifiedInvariants', function() {
            return _.map(
                _.filter(this._invariants(), function (invariant) { return !!invariant["scope"]["class"] }),
                function(invariant) { return new Invariant(invariant); }
            );
        })

        .load('_invariants', function(invariantSet) {
            var URLParams = "?attributes=" + invariantSet.attributeNames();
            if (invariantSet.nodeRef()) { URLParams += "&nodeRef=" + invariantSet.nodeRef(); }
            else { URLParams += "&type=" + invariantSet.type(); }

            Alfresco.util.Ajax.jsonGet({
                url: Alfresco.constants.PROXY_URI + "citeck/invariants" + URLParams,
                successCallback: {
                    scope: invariantSet,
                    fn: function (response) {
                        if (this._cache()) this.defaultInvariants(response.json.invariants);
                        this._invariants(response.json.invariants);
                    }
                }
            });
        })
        .load('_cache', function(invariantSet) {
            invariantSet._cache(Alfresco.util.ComponentManager.get("InvariantsRuntimeCache"));
        })
        ;


    var featureInvariants = function(featureName) {
        return function() {
            if (featureName != "relevant") if (this.irrelevant()) return [];

            var invariantSet = this.invariantSet();
            if(!invariantSet) return [];

            var groupedInvariants = invariantSet.groupedInvariants();

            var info = this.info(),
                name = info.name(),
                type = info.type(),
                kind = type == 'property' ? info.datatype() : info.nodetype();

            var keys = [
                invariantSet.getKey(type, name, featureName, true),
                invariantSet.getKey(type + '_type', kind, featureName, true),
                invariantSet.getKey(type + '_type', null, featureName, true),
                invariantSet.getKey(type, name, featureName, false),
                invariantSet.getKey(type + '_type', kind, featureName, false),
                invariantSet.getKey(type + '_type', null, featureName, false)
            ];

            return _.union.apply(_, _.map(keys, function(key) {
                return groupedInvariants[key] || [];
            }, this));
        };
    };

    var featureEvaluator = function(featureName, requiredClass, defaultValue, isTerminate) {
        return function(model) {
            if (featureName != "relevant") if (this.irrelevant()) return {};

            var invariantSet = this.invariantSet(),
                invariant = null,
                invariantValue = null,
                invariants = featureInvariants(featureName).call(this);

            invariant = _.find(invariants, function(invariant) {
                invariantValue = invariant.evaluate(model);
                return isTerminate(invariantValue, invariant);
            });

            return {
                invariant: invariant,
                value: koutils.instantiate(invariant != null ? invariantValue : defaultValue, requiredClass)
            }
        };
    };

    var featuredProperty = function(featureName) {
        return function() {
            if (featureName != "relevant") if (this.irrelevant()) return null;
            return this[featureName + 'Evaluator'](this.invariantsModel()).value;
        }
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

    var datatypeClassMapping = {
        'd:any': 'java.lang.Object',
        'd:text': 'java.lang.String',
        'd:date': 'java.util.Date',
        'd:datetime': 'java.util.Date',
        'd:int': 'java.lang.Integer',
        'd:long': 'java.lang.Long',
        'd:float': 'java.lang.Float',
        'd:double': 'java.lang.Double',
        'd:boolean': 'java.lang.Boolean',
        'd:locale': 'java.util.Locale',
        'd:mltext': 'org.alfresco.service.cmr.repository.MLText',
        'd:noderef': 'org.alfresco.service.cmr.repository.NodeRef',
        'd:category': 'org.alfresco.service.cmr.repository.NodeRef',
        'd:qname': 'org.alfresco.service.namespace.QName',
        'd:content': 'org.alfresco.service.cmr.repository.ContentData',
        'd:version': 'org.alfresco.util.VersionNumber',
        'd:period': 'org.alfresco.service.cmr.repository.Period'
    };

    var datatypeNodetypeMapping = {
        'd:noderef': 'sys:base',
        'd:category': 'cm:category'
    };

    var attributeLoader = new Citeck.utils.BulkLoader({
        url: Alfresco.constants.PROXY_URI + "citeck/invariants/attributes",
        method: "POST",
        emptyFn: function() { return { names:[] } },
        addFn: function(query, name) {
            if(query.names.indexOf(name) == -1) {
                query.names.push(name);
                return true;
            }

            return false;
        },
        getFn: function(response) {
            var attributes = response.json.attributes;
            return _.object(_.pluck(attributes, 'name'), attributes);
        }
    });

    Predicate
        .key('id', s)
        .property('label', s)
        .property('needsValue', b)
        ;

    var featureParameter = function(name, defaultValue) {
        return function() {
            var params = this.params();
            if (params[name]) {
                try {
                    return Citeck.utils.eval(this, params[name]);
                } catch (e) { 
                    console.error("featureParameter " + name + " have an error calculating value of evaluatedExpression for AttributeSet.id:" + this.id());
                    console.error(e);
                }
            }

            return defaultValue || null;
        }
    }

    AttributeSet
        .constructor([ Object, Node], function(data, node) {
            if (!data || !Node) return null;    
            return new AttributeSet({
                id: data.id,
                template: data.template,
                params: data.params,
                _invariants: data.invariants,
                _attributes: data.attributes,
                _sets: data.sets,
                node: node
            });
        }, true)
        .constructor([ Object, Node, AttributeSet, n], function(data, node, parentSet, index) {
            if (!data || !Node || !AttributeSet) return null;
            return new AttributeSet({
                id: data.id,
                index: index,
                template: data.template,
                params: data.params,
                _invariants: data.invariants,
                _attributes: data.attributes,
                _sets: data.sets,
                node: node,
                parentSet: parentSet
            });
        }, true)

        .key('id', s)
        .property('index', n)
        .property('node', Node)
        .property('parentSet', AttributeSet)
        .property('template', s)
        .property('params', o)
       
        .property('_visibility', b)
        .property('_activity', b)
        .property('_attributes', o)
        .property('_sets', o)
        .property('_rendered', b)

        .computed('paramRelevant', featureParameter("relevant"))
        .computed('paramProtected', featureParameter("protected"))

        .computed('irrelevant', function() {
            if (this.paramRelevant() != null) return !this.paramRelevant();
            var a_irrelevant = _.every(this.attributes(), function(attr) { 
                    return attr.irrelevant() || this.getAttributeTemplate(attr.name()) == "none"; 
                }, this),
                s_irrelevant = _.every(this.sets(), function(set) { return set.irrelevant(); });
            return a_irrelevant && s_irrelevant;
        })
        .computed('relevant', function() { return !this.irrelevant(); })
        .computed('protected', function() {
            if (this.paramProtected() != null && this.paramProtected()) return true;
            return false;
        })
        .computed('invalid', function() {
            return _.any(this.attributes(), function(attribute) {
                return attribute.relevant() && attribute.invalid();
            });
        })
        .computed('hidden', function() {
            return this.irrelevant() || !this._visibility();
        })
        .computed('disabled', function() {
            return this["protected"]() || !this._activity();
        })
        .computed('selected', function() { return !this.hidden() && !this.disabled(); })

        .computed('attributes', function() {
            var attributes = this.resolve('node.impl.attributes', []);
            return _.filter(attributes, function(attribute) {
                return !!_.find(this._attributes(), function(attr) { return attr.name == attribute.name(); });
            }, this);
        })
        .computed('sets', function() {
            return _.map(this._sets(), function(as, index) {
                return new AttributeSet(as, this.node(), this, index);
            }, this);
        })

        .method('visible', function(newValue) {
            if (_.isUndefined(newValue)) return this._visibility();
            this._visibility(newValue);
        })
        .method('enable', function(newValue) {
            if (_.isUndefined(newValue)) return this._activity();
            this._activity(newValue);
        })
        .method('getAttributeTemplate', function(name) {
            var attribute = _.find(this._attributes(), function(attr) { return attr.name == name; });
            if (attribute) return attribute.template;
            return null;
        })

        .load('_visibility', function() {
            var parent = this.parentSet();
            if (parent && parent.template() == "tabs") {
                var defaultAttributeSet = parent.params().defaultAttributeSet || null; 
                if (defaultAttributeSet) {
                    this._visibility(defaultAttributeSet == this.id());
                    return;
                } else if (this.index() == 0) {
                    this._visibility(true);
                    return;
                }

                this._visibility(false);
                return;
            }

            this._visibility(true);
        })
        .load('_activity', function() { this._activity(true) })
        ;

    AttributeInfo
        .key('name', s)
        .property('type', s) // one of: property, association, child-association, ...
        .property('nodetype', s)
        .property('datatype', s)
        .property('javaclass', s)
        .property('predicates', [ Predicate ])

        .load('predicates', function(attributeInfo) {
            var datatype = attributeInfo.datatype();
            if (datatype.indexOf(":") != -1) { datatype = datatype.split(":")[1]; }

            YAHOO.util.Connect.asyncRequest('GET',
                Alfresco.constants.URL_PAGECONTEXT + "search/search-predicates?datatype=" + datatype, {
                    success: function(response) {
                        var result = JSON.parse(response.responseText),
                            predicates = [];

                        for (var i in result.predicates) {
                            predicates.push(new Predicate(result.predicates[i]))
                        };

                        this.predicates(predicates);
                    },

                    failure: function(response) { /* error */ },

                    scope: attributeInfo
                }
            );
        })
        .load('*', koutils.bulkLoad(attributeLoader, 'name'))

        // auxiliary variables
        .property('_options', [ Node ])
        .property('_dependencies', o)
        ;

    Attribute
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

        .key('key', s)
        .property('info', AttributeInfo)
        .property('node', Node)
        .property('persisted', b)

        .property('inlineEditVisibility', b)

        .shortcut('name', 'info.name')
        .shortcut('type', 'info.type')
        .shortcut('nodetype', 'info.nodetype')
        .shortcut('datatype', 'info.datatype')
        .shortcut('predicates', 'info.predicates')
        .shortcut('javaclass', 'info.javaclass')
        .shortcut('default', 'defaultValue', null)

        .computed('valueClass', function() { return classMapping[this.javaclass()] || null; })
        .computed('invariantSet', function() { return this.node().impl().invariantSet(); })
        .computed('invariantsModel', function() { return this.getInvariantsModel(this.value, this.cache = this.cache || {}); })
        .computed('title', featuredProperty('title'))
        .computed('description', featuredProperty('description'))
        .computed('multiple', featuredProperty('multiple'))
        .computed('mandatory', featuredProperty('mandatory'))
        .computed('invariantRelevant', featuredProperty('relevant'))
        .computed('relevant', function() {
            var allAttributeNames = this.node().impl().allAttributeNames();
            if(!_.isEmpty(allAttributeNames) && !_.contains(allAttributeNames, this.name())) return false;
            return this.invariantRelevant();
        })
        .computed('invariantProtected', featuredProperty('protected'))
        .computed('protected', function() {
            if(this.irrelevant()) return true;

            // first value invariant
            var invariantValue = this.invariantValue();
            if(invariantValue != null && (!_.isArray(invariantValue) || invariantValue.length > 0)) return true;

            // second own invariant
            return this.invariantProtected();
        })
        .computed('empty', function() {
            return this.value() == null
                || this.multiple() && this.value().length == 0
                || this.valueClass() == String && this.value().length == 0;
        })
        .computed('evaluatedValid', function() {
            return this.validEvaluator(this.invariantsModel());
        })
        .computed('invariantValid', function() {
            return this.evaluatedValid().value; 
        })
        .computed('valid', function() {
            if(this.irrelevant()) return true;
            if(this.empty()) return this.optional() || this['protected']();
            return this.invariantValid();
        })
        .computed('validDraft', function() {
            if(this.irrelevant() || this.empty()) return true;
            return this.invariantValid();
        })
        .computed('validationMessage', function() {
            if(this.irrelevant()) return "";

            if(this.empty())
                return this.optional() || this['protected']() ? "" : Alfresco.util.message("validation-hint.mandatory");

            var invariant = this.evaluatedValid().invariant;
            return invariant != null ? Alfresco.util.message(invariant.description()) : "";
        })
        .computed('changed', function() { return this.newValue.loaded(); })
        .computed('changedByInvariant', function() {
            return (this.invariantValue.loaded() && this.invariantValue() != null) || 
                   (this.invariantNonblockingValue.loaded() && this.invariantNonblockingValue() != null);
        })
        .computed('textValue', {
            read: function() { return this.getValueText(this.value()); },
            write: function(value) {
                if(value == null || value == "") {
                    return this.value(null);
                } else {
                    return this.value(value);
                }
            }
        })
        .computed('textValidationValue', {
            read: function() {
                return this.getValueText(this.value());
            },
            write: function(value) {
                if(value == null || value == "") {
                    return this.value(null);
                } else {
                    var newValue = (value.replace(/<(?:[^"'>]+|(["'])(?:\\[\s\S]|(?!\1)[\s\S])*\1)*>/g, ""));
                    return this.value(this.value() == newValue ? null : newValue);
                }
            }
        })
        .computed('single', function() { return !this.multiple(); })
        .computed('optional', function() { return !this.mandatory(); })
        .computed('irrelevant', function() { return !this.relevant(); })
        .computed('invalid', function() { return !this.valid(); })
        .computed('unchanged', function() { return !this.changed(); })
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
        .computed('inlineViewAttention', function() {
            return !this.inlineEditVisibility() && this.changedByInvariant();
        })

        .load('inlineEditVisibility', function() { this.inlineEditVisibility(false) })
        
        .method('showEditableField', function(data, event) {
            if (!this.inlineEditVisibility()) this.inlineEditVisibility(true);
        })
        .method('inlineEditChanger', function(data, event) {
            var isDraft = this.node().properties["invariants:isDraft"];

            // save node if it valid
            if (this.inlineEditVisibility()) {
                if (this.changed() || this.changedByInvariant()) {
                    if (isDraft || this.resolve("node.impl.valid")) {
                        this.node().thisclass.save(this.node(), { });

                        if (isDraft) {
                            data.inlineEditVisibility(false);
                        } else {
                            // hide inline edit form for all attributes after save node
                            _.each(this.node().impl().attributes(), function(attr) {
                                if (attr.inlineEditVisibility()) attr.inlineEditVisibility(false);
                            });
                        }
                    }
                    
                    return;
                }
            }
            

            // change visibility mode
            this.inlineEditVisibility(!this.inlineEditVisibility());
        })
        .method('convertValue', function(value, multiple) {
            if(value == null) return multiple ? [] : null;

            var instantiate = _.partial(koutils.instantiate, _, this.valueClass());
            if(_.isArray(value)) {
                return multiple ? _.map(value, instantiate) : instantiate(value[0]);
            } else {
                return multiple ? [ instantiate(value) ] : instantiate(value) ;
            }
        })
        .method('getInvariantsModel', function(value, cache) {
            var model = {};

            _.each(this.node().impl().defaultModel(), function(property, name) {
                Object.defineProperty(model, name, _.isFunction(property) ? { get: property } : { value: property });
            });
            Object.defineProperty(model, 'node', { get: this.node });
            Object.defineProperty(model, 'value', typeof value == "function" ? { get: value } : { value: value });
            Object.defineProperty(model, 'cache', { value: cache });
            return model;
        })
                .method('href', function(data) {
            if (data && data.toString().indexOf("invariants.Node") != -1) {
                if (data.typeShort == "cm:person") {
                    if (Alfresco.constants.URI_TEMPLATES["userprofilepage"]) {
                        return Alfresco.util.uriTemplate("userprofilepage", { userid: data.properties.userName });
                    }
                } 
                return Alfresco.util.siteURL('card-details?nodeRef=' + data.nodeRef);
            }
            return null;
        })
        .method('reset', function(full) {
            this.newValue(null);
            this.newValue.reload();

            this.invariantValue.reload();
            this.invariantNonblockingValue.reload();

            this.hybridValue(null);
            this.hybridValue.reload();

            if(full) {
                this.persisted.reload();
                this.persistedValue.reload();
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
        .method('destroy', function(index, nodeRef) {
            this.resolve('node.impl.runtime').deleteNode(nodeRef, {
                success: function(oResponse) {
                    var result = YAHOO.lang.JSON.parse(oResponse.responseText)
                    if (result.success) this.remove(index);
                },
                scope: this
            })
        })
        .method('push', function(data) {
            if (this.single()) { 
                this.value(data); 
            } else {
                var currentValues = this.value();
                currentValues.push(data);
                this.value(currentValues);
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
        .method('getAttributeSet', function() {
            var map = this.node().impl().attributeSetMap(),
                mapObject = map.attributes[this.name()];
            return mapObject ? mapObject.set : null;
        })
        .method('getFirstLevelAttributeSet', function() {
            var map = this.node().impl().attributeSetMap(),
                rootSet = this.node().impl().attributeSet(),
                set = map.attributes[this.name()] ? map.attributes[this.name()].set : null;

            if (set) {
                if (set == rootSet) return set;
                while (set.parentSet() != rootSet) set = set.parentSet();
                return set;
            }

            return null;
        })

        // feature evaluators
        .method('valueEvaluator', featureEvaluator('value', o, null, notNull))
        .method('nonblockingValueEvaluator', featureEvaluator('nonblocking-value', o, null, notNull))
        .method('defaultEvaluator', featureEvaluator('default', o, null, notNull))
        .method('optionsEvaluator', featureEvaluator('options', o, null, notNull))
        .method('titleEvaluator', featureEvaluator('title', s, '', notNull))
        .method('descriptionEvaluator', featureEvaluator('description', s, '', notNull))
        .method('valueTitleEvaluator', featureEvaluator('value-title', s, '', notNull))
        .method('valueDescriptionEvaluator', featureEvaluator('value-description', s, '', notNull))
        .method('relevantEvaluator', featureEvaluator('relevant', b, true, notNull))
        .method('multipleEvaluator', featureEvaluator('multiple', b, false, notNull))
        .method('mandatoryEvaluator', featureEvaluator('mandatory', b, false, notNull))
        .method('protectedEvaluator', featureEvaluator('protected', b, false, notNull))
        .method('validEvaluator', featureEvaluator('valid', b, true, isFalse))

        // value properties:
        .property('newValue', o) // value, set by user
        .property('persistedValue', o) // value, persisted in repository
        .property('hybridValue', o)
        .computed('invariantValue', featuredProperty('value'))
        .computed('invariantNonblockingValue', featuredProperty('nonblockingValue'))
        .computed('invariantDefault', featuredProperty('default'))
        .computed('defaultValue', function() {
            return this.convertValue(this.invariantDefault(), this.multiple()); 
        })

        .subscribe('invariantNonblockingValue', function(newValue) {
            if (this.invariantNonblockingValue.loaded() && newValue) this.hybridValue(newValue);
        })
        .subscribe('newValue', function(newValue) {
            if (this.changed()) this.hybridValue(newValue);
        })

        .computed('rawValue', function() {
            // if (this.irrelevant()) return null;

            var invariantValue = this.invariantValue(), invariantNonblockingValue = this.invariantNonblockingValue(),
                hybridValue = this.hybridValue(),
                invariantDefault = this.invariantDefault(),
                isViewMode = this.resolve("node.impl.inViewMode", false),
                isInlineEditMode = this.resolve("node.impl.runtime.inlineEdit", false),
                inSubmitProcess = this.resolve("node.impl.inSubmitProcess", false),
                inEditing = this.inlineEditVisibility() || inSubmitProcess;

            if (!isViewMode || inEditing) {
                if(invariantValue != null) return invariantValue;
                if(this.hybridValue.loaded()) return hybridValue;
            }           

            if(this.persisted()) return this.persistedValue();            

            if ((!isViewMode || inEditing) && invariantDefault != null) {
                return invariantDefault;
            }

            return null;
        })
        .computed('value', {
            read: function() { return this.convertValue(this.rawValue(), this.multiple()); },
            write: function(value) { this.newValue(this.convertValue(value, true)); }
        })
        .computed('singleValue', {
            read: function() { return this.convertValue(this.rawValue(), false); },
            write: function(value) { this.value(value); }
        })
        .computed('multipleValues', {
            read: function() { return this.convertValue(this.rawValue(), true); },
            write: function(value) { this.value(value); }
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

        // options properties
        .computed('invariantOptions', featuredProperty('options'))
        .computed('optionsInvariants', function() {
            return featureInvariants('options').call(this);
        })
        .computed('options', {
            read: function() { return this.convertValue(this.invariantOptions(), true); },
            pure: true
        })
        .method('filterOptions', function(criteria, pagination) {
            if (this.irrelevant()) return [];

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

            return [];
        })

        // value title
        .method('getValueTitle', function(value, postprocessing) {
            var value = this.valueTitleEvaluator(this.getInvariantsModel(value)).value;
            if (postprocessing) { return postprocessing(value);  }
            return value; 
        })
        .computed('valueTitle', function() { return this.getValueTitle(this.singleValue()); })
        .shortcut('value-title', 'valueTitle')

        // value description
        .method('getValueDescription', function(value) {
            var model = this.getInvariantsModel(value);
            return this.valueDescriptionEvaluator(model).value || this.getValueTitle(value);
        })
        .computed('valueDescription', function() { return this.getValueDescription(this.singleValue()); })
        .shortcut('value-description', 'valueDescription')

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
        .property('isDraft', b)
        .property('node', Node)
        .property('type', s)
        .property('classNames', [ s ])
        .property('permissions', o)
        .property('inSubmitProcess', b)
        .property('viewAttributeNames', [ s ])
        .property('defaultAttributeNames', [ s ])
        .property('virtualParent', Node)
        .property('defaultModel', DefaultModel)
        .property('runtime', Runtime)

        .property('_invariants', o)
        .property('_withoutView', b)
        .property('_definedAttributeNamesLoaded', b)
        .property('_viewAttributeNamesLoaded', b)
        .property('_attributes', o)
        .property('_set', o)
        .property('_cache', o)

        .shortcut('typeShort', 'type')

        .computed('isPersisted', function() { return this.nodeRef() != null; })
        .computed('typeFull', function() {
            if(this.type() == null) return null;
            var qnameType = new QName(this.type());
            return qnameType.fullQName();
        })
        .computed('inViewMode', function() {
            return this.resolve('defaultModel.view.mode') == "view";
        })
        .computed('types', function() {
            var types = this.getAttribute('attr:types');
            return types ? types.multipleValues() : [];
        })
        .computed('aspects', function() {
            var aspects = this.getAttribute('attr:aspects');
            return aspects ? aspects.multipleValues() : [];
        })
        .computed('definedAttributeNames', function() {
            var attributeNames =_.uniq(_.flatten(_.map(this.classNames(), function(className) {
                return _.invoke(new DDClass(className).attributes(), 'shortQName');
            })));

            if (attributeNames.length > 0) this._definedAttributeNamesLoaded(true);
            return attributeNames;
        })
        .computed('allAttributeNames', function() {
            return _.union(this.defaultAttributeNames(), this.viewAttributeNames(), this.definedAttributeNames());
        })
        .computed('valid', function() {
            return _.all(this.attributes(), function(attr) { return attr.valid(); });
        })
        .computed('validDraft', function() {
            return _.all(this.attributes(), function(attr) { return attr.validDraft(); });
        })
        .computed('changed', function() {
            return _.any(this.attributes(), function(attr) { return attr.changed(); });
        })
        .computed('invalid', function() {
            return !this.valid();
        })
        .computed('unchanged', function() {
            return !this.changed();
        })
        .computed('changedData', function() {
            var attributes = {};

            _.each(this.getChangedAttributes(), function(attr) {
                if(attr.relevant()) { attributes[attr.name()] = attr.jsonValue(); }
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
        .computed('attributeSet', function() {
            if (this._set()) return new AttributeSet(this._set(), this.node());
            return null;
        })
        .computed('attributes', {
            read: function() {
                var node = this.node(),
                    attributes = [],
                    createdNames = {};

                var validAttributeNames = !this._withoutView() ? 
                    this.viewAttributeNames().concat(this.defaultAttributeNames()) : this.definedAttributeNames();
                validAttributeNames = validAttributeNames;

                if(this.isPersisted() && this._attributes() != null && this._attributes().length >= validAttributeNames.length) {
                    var filteredAttributes = _.filter(this._attributes(), function(value, name) {
                        return _.contains(validAttributeNames, name);
                    });

                    _.each(filteredAttributes, function(value, name) {
                        createdNames[name] = true;
                        attributes.push(new Attribute(node, name, true, value));
                    });

                    return attributes;
                }

                var processAttributeName = function(name) {
                    if(!createdNames[name]) {
                        createdNames[name] = true;
                        // we can't be sure, whether this attribute is persisted or not
                        // because not all persisted attributes are in the default attributes list
                        attributes.push(new Attribute(node, name));
                    }
                };

                _.each(validAttributeNames, processAttributeName);

                return attributes;
            },
            pure: true
        })
        .computed('parent', function() {
            var virtualParent = this.virtualParent();
            if(virtualParent) return virtualParent;
            var parent = this.attribute('attr:parent');
            return parent ? parent.value() : null;
        })
        .computed('attributeSetMap', function() {
            var buildMapOfAttributes = function(set, attributes, nMap) {
                    attributes.forEach(function(attribute) {
                        if (!nMap[attribute.name()]) {
                            nMap.attributes[attribute.name()] = {
                                attribute: attribute,
                                set: set
                            };
                        }
                    });
                },
                buildMapOfSets = function(sets, parent, nMap) {
                    sets.forEach(function(set) {
                        if (!nMap[set.id()]) {
                            nMap.attributeSets[set.id()] = { 
                                set: set,
                                parent: parent, 
                                children: _.map(set.sets(), function(s) { return s.id(); })
                            };
                            
                            if (set.sets().length)
                                buildMapOfSets(set.sets(), set.id(), nMap)

                            if (set.attributes().length)
                                buildMapOfAttributes(set, set.attributes(), nMap);
                        }
                    });
                };


            var rootSet = this.attributeSet(),
                map = {
                    attributes: {},
                    attributeSets: {}
                };

            if (rootSet) {
                buildMapOfAttributes(rootSet, _.map(this.defaultAttributeNames(), function(attributeName) {
                    return this.attribute(attributeName);
                }, this), map);

                map.attributeSets[rootSet.id()] = {
                    set: rootSet,
                    parent: null,
                    children: _.map(rootSet.sets(), function(s) { return s.id(); })
                };

                if (rootSet.sets().length)
                    buildMapOfSets(rootSet.sets(), rootSet.id(), map);

                if (rootSet.attributes().length)
                    buildMapOfAttributes(rootSet, rootSet.attributes(), map);

                return map;
            }

            return null;
        })
        .computed('invariantSet', function() {
            if (!_.isNull(this._invariants()) && this._invariants().length > 0) {
                return new ExplicitInvariantSet({ 
                    className: this.type(), 
                    invariants: this._invariants() 
                });
            } else if (this.type.loaded()) {
                var validAttributeNames = !this._withoutView() ? 
                    this.defaultAttributeNames().concat(this.viewAttributeNames()) : this.definedAttributeNames();
                if (validAttributeNames.length > this.defaultAttributeNames().length) {
                    return new SingleClassInvariantSet({ 
                        className: this.type(),
                        nodeRef: this.nodeRef(),
                        type: this.type(),
                        attributeNames: validAttributeNames 
                    });
                }
            }

            return null;
        })

        .method('_filterAttributes', function(filterBy) {
            return _.filter(this.attributes() || [], function(attr) {
                return attr[filterBy]();
            })
        })
        
        // Deprecated. Slow. Use 'getAttribute'
        .method('attribute', function(name) {
            return _.find(this.attributes() || [], function(attr) {
                return attr.name() == name;
            });
        })
        .method('reset', function(full) {
            _.invoke(this.attributes(), 'reset', full);
            if(full) this._attributes.reload();
        })
        .method('updateModel', function(model) {
            if (model) {
                this.model(_.omit(model, 'attributes'));

                if(model.attributes && model.attributes.length) {
                    this.model({ _attributes: model.attributes })
                }
            }
        })
        .method('getAttributeSet', function(id) {
            var map = this.attributeSetMap(),
                object =  map ? map.attributeSets[id] || map.attributes[id] : null;
            return object ? object.set : null;
        })
        .method('getFirstLevelAttributeSet', function(id) {
            var map = this.attributeSetMap(),
                object =  map ? map.attributeSets[id] || map.attributes[id] : null,
                rootSet = this.attributeSet(),
                set = object.set;

            if (set) {
                if (set == rootSet) return set;
                while (set.parentSet() != rootSet) set = set.parentSet();
                return set;
            }

            return null;
        })
        .method('getAttribute', function(name) {
            var attributeObject = this.attributeSetMap() ? this.attributeSetMap().attributes[name] : null;
            return attributeObject ? attributeObject.attribute : undefined;
        })
        .method('getChangedAttributes', function() {
            return _.filter(this.attributes() || [], function(attr) {
                return attr.relevant() && (attr.changed() || attr.changedByInvariant() || (!attr.persisted() && attr.invariantDefault() != null));
            })
        })
        .method('getInvalidAttributes', function() {
            return this._filterAttributes("invalid");
        })


        .load('_cache', function(impl) {
            impl._cache(Alfresco.util.ComponentManager.get("InvariantsRuntimeCache"));
        })
        .load('_invariants', function(impl) { impl._invariants([]); })
        .load('_set', function(impl) { impl._set(null); })
        .load([ '_withoutView', '_viewAttributeNamesLoaded' ], function(impl) { 
            impl.setModel({ _withoutView: false, _viewAttributeNamesLoaded: false });
        })
        .load('_definedAttributeNamesLoaded', function(impl) { impl._definedAttributeNamesLoaded(false); })
        .load('isDraft', function(impl) {
            var draftAttribute = impl.getAttribute("invariants:isDraft");
            impl.isDraft(draftAttribute ? draftAttribute.value() : false);
        })
        .load('inSubmitProcess', function(impl) { impl.inSubmitProcess(false); })
        .load('defaultModel', function(impl) { impl.defaultModel(new DefaultModel(COMMON_DEFAULT_MODEL_KEY)) })
        .load('runtime', function(impl) { impl.runtime(null); })
        .load('defaultAttributeNames', function(impl) {
            var defaultAttributeNames = [
                "attr:aspects", "attr:noderef", "attr:types", 
                "attr:parent", "attr:parentassoc"
            ];

            impl.defaultAttributeNames(defaultAttributeNames);
        })
        .load('viewAttributeNames', function(impl) {
            if (!impl.viewAttributeNames.loaded()) {
                if (impl._cache()) {
                    var cachedViewAttributeNames = impl._cache().get(["ViewAttributeNames", impl.type()]);
                    if (!_.isEmpty(cachedViewAttributeNames)) {
                        impl.viewAttributeNames(cachedViewAttributeNames);
                        return;
                    }
                }

                var loader = function(nodeRef, attributes) {
                    var attributeCount = attributes ? _.keys(attributes).length : -1;
                    if (attributeCount <= 0) impl._withoutView(true);
                    else if (impl._cache()) impl._cache().insert(["ViewAttributeNames"], impl.type(), attributes);
                    impl.setModel({ viewAttributeNames: attributes, _viewAttributeNamesLoaded: true })
                }

                if (impl.nodeRef()) {
                    viewAttributeNamesByNodeRefLoader.load(impl.nodeRef(), loader);
                } else if (impl.type()) {
                    viewAttributeNamesByTypeLoader.load(impl.type(), loader);
                }
            }
        })


        .load([ 'classNames', 'type' ], function(impl) {
            if(impl.isPersisted()) {
                Citeck.utils.classNamesLoader.load(impl.nodeRef(), function(nodeRef, model) {
                    if (model) impl.updateModel(model);
                });
            }
        })
        .load('permissions', function(impl) {
            if(impl.isPersisted()) {
                Citeck.utils.permissionsLoader.load(impl.nodeRef(), function(nodeRef, model) {
                    if (model) impl.updateModel(model);
                });
            }
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
            if(this.impl.loaded() == false) {
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
            return this.impl().nodeRef() || '';
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
            return this.impl().typeFull();
        })
        .nativeProperty('typeShort', function() {
            return this.impl().typeShort();
        })
        .method('isSubType', function(name) {
            if(Citeck.utils.isShortQName(name)) {
                return this.impl().typeShort() == name || _.contains(_.invoke(this.impl().types(), 'shortQName'), name);
            } else {
                return this.impl().typeFull() == name || _.contains(_.invoke(this.impl().types(), 'fullQName'), name);
            }
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
            return _.invoke(this.impl().aspects(), 'shortQName');
        }, true)
        .method('hasAspect', function(name) {
            if(Citeck.utils.isShortQName(name)) {
                return _.contains(_.invoke(this.impl().aspects(), 'shortQName'), name);
            } else {
                return _.contains(_.invoke(this.impl().aspects(), 'fullQName'), name);
            }
        })

        .method('hasClassName', function(name) {
            return _.contains(this.impl().classNames(), name);
        })

        .nativeProperty('parent', function() {
            return this.impl().parent();
        })
        .nativeProperty('properties', function() {
            logger.debug('properties recalculated for node ' + this.key() + ' (nodeRef ' + this.impl().nodeRef() + ')');
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

                var data = {
                    view: node.resolve('impl.defaultModel.view'),
                    attributes: node.resolve('impl.changedData.attributes')
                };
                    
                var isDraft = node.resolve('impl.isDraft');
                if (_.isBoolean(isDraft)) data['isDraft'] = isDraft;

                return data;
            },
            toResult: function(response) {
                return new Node(response.result);
            },
            toFailureMessage: function(response) {
                return response.message;
            },
            onSuccess: function(node, result) {
                if (node.impl().runtime() && node.impl().runtime().inlineEdit()) {
                    node.impl().getChangedAttributes().forEach(function(attribute) {
                        // value as persistedValue
                        // persisited by default as 'true' after save
                        attribute.persistedValue(attribute.rawValue());
                        attribute.persisted(true);

                        attribute.reset();
                    });
                }

                node.impl().inSubmitProcess(false);
            },
            onFailure: function(node, message) {
                if (node.impl().runtime() && node.impl().runtime().inlineEdit()) {
                    node.impl().getChangedAttributes().forEach(function(attribute) {
                        attribute.reset(true);
                    });
                }

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
        .computed('longQName', function() {
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
        .property('parent', Runtime)
        .property('inlineEdit', b)

        .property('_loading', b)

        .constant('rootObjects', rootObjects)

        .shortcut('inSubmitProcess', 'node.impl.inSubmitProcess')

        .computed('loaded', function() {
            if (!this._loading()) return true;

            if (this.node.loaded() && this.node().impl.loaded() && 
                this.node().impl().attributes.loaded() && this.node().impl().invariantSet.loaded()) {

                if (!this.node().impl()._withoutView()) {
                    if (!this.node().impl()._viewAttributeNamesLoaded()) return false;
                } else if (!this.node().impl()._definedAttributeNamesLoaded()) return false;

                var attributes = this.node().impl().attributes(), loadedAttributes = [];
                _.each(attributes, function(attribute) {
                    if (_.every([ "name", "valid", "relevant", "protected", "mandatory" ], function(p) {
                        return attribute[p].loaded();
                    })) loadedAttributes.push(true);
                });
                if (loadedAttributes.length != attributes.length) return false;

                // delay while controls are loading (0.5 seconds enough)
                if (this._loading())
                    setTimeout(function(scope) {  scope._loading(false); }, 500, this);
            }

            return false;
        })

        .computed('invalid', function() {
            return !this.node().properties["invariants:isDraft"] && this.resolve('node.impl.invalid');
        })

        .computed('isSubmitReady', function() {
            var isValid = !this.invalid(),
                isLoaded = this.loaded(),
                isNotInSubmitProcess = !this.inSubmitProcess();

            return isNotInSubmitProcess && isLoaded && isValid;
        })


        // PUBLIC METHODS
        // --------------
        .method('scrollToFormField', function(data, event) {
            var field = $(".form-field[data-attribute-name='" + data.name() + "']");
            if (!data.inlineEditVisibility()) data.inlineEditVisibility(true);
            if (field) $('html,body').animate({ scrollTop: field.offset().top - 10 }, 500 );
        })

        .method('deleteNode', function(nodeRef, callback) {
            YAHOO.util.Connect.asyncRequest('DELETE', Alfresco.constants.PROXY_URI + "citeck/node?nodeRef=" + nodeRef, callback);
        })

        .method('submit', function() {
            if(this.node().impl().valid()) {
                if (this.node().hasAspect("invariants:draftAspect")) {
                    this.node().impl().isDraft(false);
                }

                this.broadcast('node-view-submit');
            }
        })
        .method('submitDraft', function() {
            if (this.node().hasAspect("invariants:draftAspect")) {
                this.node().impl().isDraft(true);
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

        .method('terminate', function() {
            var form = Alfresco.util.ComponentManager.get(this.key() + "-form");
            if (form) form._terminate();
            delete form;
        })


        // for tabs template
        // -----------------

        .method('selectAttributeSet', function(attributeSet, event) {
            if (!attributeSet.disabled()) {
                _.each(attributeSet.parentSet().sets(), function(set) { set.visible(false); })
                attributeSet.visible(true);
            }
        })

        .method('scroll', function(data, event) {
            var scrollArrow = $(event.target),
                direction = (function() {
                    var matches = scrollArrow.attr("class").match(/scroll-(left|right)/);
                    return matches.length > 0 ? matches[1] : undefined;
                })();

            if (direction) {
                var list = $("ul", scrollArrow.parent());
                if (direction == "right") 
                    list.animate({ scrollLeft: list.scrollLeft() + 100 }, 300);
                if (direction == "left") 
                    list.animate({ scrollLeft: list.scrollLeft() - 100 }, 300);
            }

            return false;
        })

        .init(function() {
            this._loading(true);
        })
        ;


    // performance tuning
    var rateLimit0 = { rateLimit: { timeout: 0, method: "notifyWhenChangesStop" } },
        rateLimit250 = { rateLimit: { timeout: 250, method: "notifyWhenChangesStop" } };

    AttributeSet.extend('irrelevant', rateLimit0);
    AttributeSet.extend('attributes', rateLimit0);
    AttributeSet.extend('sets', rateLimit0);

    Attribute.extend('*', rateLimit0);
    Attribute.extend('invariantNonblockingValue', rateLimit0);
    
    AttributeInfo.extend('*', rateLimit0);
    
    DDClass.extend('attributes', rateLimit0);
    
    NodeImpl.extend('type', rateLimit0);
    NodeImpl.extend('_attributes', rateLimit0);
    NodeImpl.extend('attributes', rateLimit0);
    NodeImpl.extend('invariantSet', rateLimit250);

    GroupedInvariantSet.extend('invariants', rateLimit250);
    MultiClassInvariantSet.extend('invariants', rateLimit250)
    SingleClassInvariantSet.extend('invariants', rateLimit0)

    Runtime.extend('loaded', rateLimit250)

    // create common attributes statically:
    // _.each({
    //     "attr:aspects": "d:qname",
    //     "attr:noderef": "d:noderef",
    //     "attr:types": "d:qname",
    //     "attr:parent": "d:noderef",
    //     "attr:parentassoc": "d:qname",
    //     "cm:name": "d:text",
    //     "cm:created": "d:datetime",
    //     "cm:creator": "d:text",
    //     "cm:modified": "d:datetime",
    //     "cm:modifier": "d:text",
    //     "cm:accessed": "d:datetime",
    //     "cm:title": "d:mltext",
    //     "cm:description": "d:mltext",
    //     "cm:content": "d:content",
    //     "cm:owner": "d:text",
    //     "sys:store-protocol": "d:text",
    //     "sys:store-identifier": "d:text",
    //     "sys:node-uuid": "d:text",
    //     "sys:node-dbid": "d:long",
    //     "sys:locale": "d:locale",
    // }, function(type, name) {
    //     new AttributeInfo({
    //             "name": name,
    //             "type": "property",
    //             "datatype": type,
    //             "nodetype": datatypeNodetypeMapping[type] || null,
    //             "javaclass": datatypeClassMapping[type]
    //         });
    // });

    // create common aspects statically
    // _.each({
    //     "cm:auditable": [ "cm:created", "cm:creator", "cm:modified", "cm:modifier", "cm:accessed" ],
    //     "sys:localized": [ "sys:locale" ],
    //     "sys:referenceable": [ "sys:store-protocol", "sys:store-identifier", "sys:node-uuid", "sys:node-dbid", "attr:noderef" ],
    //     "sys:incomplete": [],
    //     "cm:ownable": ["cm:owner"],
    // }, function(attributes, name) {
    //     new DDClass({
    //         name: name,
    //         qname: name,
    //         isAspect: true,
    //         attributes: attributes
    //     })
    // });

    // create common types statically
    // _.each({
    //     "sys:base": [ "attr:types", "attr:aspects", "attr:parent", "attr:parentassoc" ],
    //     "cm:cmobject": ["cm:name"],
    //     "cm:content": ["cm:content"],
    //     "cm:folder": ["cm:contains"],
    //     "cm:authority": [],
    //     "cm:authorityContainer": ["cm:authorityName", "cm:authorityDisplayName", "cm:member"],
    //     "cm:category": ["cm:subcategories"],
    //     "dl:dataListItem": []
    // }, function(attributes, name) {
    //     new DDClass({
    //         name: name,
    //         qname: name,
    //         isAspect: false,
    //         attributes: attributes
    //     })
    // });

    var InvariantsRuntimeCache = function() {
        InvariantsRuntimeCache.superclass.constructor.call(this, "Citeck.invariants.InvariantsRuntimeCache", "InvariantsRuntimeCache");
        
        this.cacheObject = {
            "DefaultInvariants": {},
            "ViewAttributeNames": {}
        };
    };

    YAHOO.extend(InvariantsRuntimeCache, Alfresco.component.Base, {
        _isInvariantClass: function(object) {
            return object.toString().indexOf("invariants.Invariant") != -1;
        },

        insert: function(keys, keyName, object, mode) {
            if (!_.isArray(keys)) { keys = [ keys ] }
            var targetObject = this.get(keys);
            if (targetObject) {
                try {
                    if (_.isArray(targetObject)) { targetObject.push(object); }
                    else if (_.isObject(targetObject)) {
                        if (keyName) targetObject[keyName] = object;
                        else targetObject = object;
                    }
                } catch (e) { console.error(e); }
            }
        },
        get: function(keys) {
            if (!_.isArray(keys)) { keys = [ keys ] }
            var targetObject = this.cacheObject;
            for (var i = 0; i < keys.length; i++) {
                if (!targetObject[keys[i]]) return null;
                targetObject = targetObject[keys[i]];
            }
            return targetObject;
        },
        has: function(keys) { return !!this.get(keys); },
        remove: function(key) { delete this.cacheObject[key]; },


        // An extended set of methods for DefaultInvariants
        // ------------------------------------------------

        addDefaultInvariants: function(invariants) {
            var defaultInvariants = this.cacheObject["DefaultInvariants"];

            // finally, add groups to 'DefaultInvaraints'
            _.each(
                // thirdly, group by attribute class name
                _.groupBy(
                    // secondly, instantiate invariant
                    _.map(
                        // firstly, get all default invariants
                        _.filter(
                            invariants, 
                            function (invariant) { 
                                return this._isInvariantClass(invariant) ? 
                                    !invariant.classScope() : !invariant["scope"]["class"];
                            }, this
                        ),
                        function(invariant) { 
                            return !this._isInvariantClass(invariant) ? new Invariant(invariant) : invariant;
                        }, this
                    ), 
                    function(invariant) {
                        var attributeName = invariant.attributeScope(),
                            isTypeInvariant = invariant.attributeScopeKind().indexOf("_type") != -1;
                        
                        if (isTypeInvariant) {
                            if (attributeName) return "general";
                            return "base";
                        }

                        return attributeName;
                    }, this
                ), 
                function(invGroup, invGroupKey) {
                    if (!defaultInvariants[invGroupKey]) defaultInvariants[invGroupKey] = [];
                    defaultInvariants[invGroupKey] = _.uniq(
                        defaultInvariants[invGroupKey].concat(invGroup), 
                        function(inv, index) { return JSON.stringify(_.values(inv.model())); }
                    );
                }, this
            );
        },

        addDefaultInvariant: function(invariant) {
            var defaultInvariants = this.cacheObject["DefaultInvariants"],
                invariantObject = !this._isInvariantClass(invariant) ? new Invariant(invariant) : invariant,
                invariantKey = invariantObject.attributeScope();
            
            if (!defaultInvariants[invariantKey]) defaultInvariants[invariantKey] = [];
            if (!_.contains(defaultInvariants[invariantKey], invariantObject))
                defaultInvariants[invariantKey].push(invariantObject);
        },

        getDefaultInvariants: function(attributeNames) {
            if (attributeNames) {
                return _.flatten(_.filter(this.cacheObject["DefaultInvariants"], function(invArray, invKey) {
                    return _.contains(attributeNames, invKey);
                }));
            }
            return _.flatten(_.values(this.cacheObject["DefaultInvariants"]));
        },

        getDefaultInvariantsGroup: function(attributeName) {
            if (attributeName) return this.cacheObject["DefaultInvariants"][attributeName];
            return this.cacheObject["DefaultInvariants"];
        }

    });


    var InvariantsRuntime = function(htmlid, runtimeKey) {
        InvariantsRuntime.superclass.constructor.call(this, "Citeck.invariants.InvariantsRuntime", htmlid);
        this.runtime = new Runtime(runtimeKey);
    };

    YAHOO.extend(InvariantsRuntime, Alfresco.component.Base, {

        _terminate: function() {
            // unregister component
            Alfresco.util.ComponentManager.unregister(this);

            // destory knockout runtime
            ko.cleanNode(this.id);
            this.runtime = null;

            this.destroy();
        },

        onReady: function() {
            var self = this;

            if (this.options.model.node.viewAttributeNames.length) {
                this.options.model.node._viewAttributeNamesLoaded = true;
                this.options.model.node._withoutView = false;
            }

            if (this.options.invariantsRuntimeCache) this.initRuntimeCache();
            this.initRuntime();

            if (this.options.model.inlineEdit) {
                $("body").mousedown(function(e, a) {
                    var node = self.runtime.node(),
                        isDraft = node.properties["invariants:isDraft"],
                        form = $("#" + self.options.model.key),
                        inlineEditingAttributes = node.impl()._filterAttributes("inlineEditVisibility");

                    var targetOutForm = !form.is(e.target) && form.has(e.target).length == 0,
                        isNotAPanel = $(e.target).closest(".yui-panel-container").length == 0,
                        isNotALink = e.target.tagName != "A";

                    // target not: from form, a link, a panel
                    if (targetOutForm && isNotAPanel && isNotALink && inlineEditingAttributes.length) {
                        if (isDraft || node.resolve("impl.valid")) {
                            // save node if it valid
                            if (_.any(inlineEditingAttributes, function(attr) {
                                return attr.newValue() != null && attr.newValue() != attr.persistedValue();
                            })) node.thisclass.save(node, { });
                            
                            // close all valid inline editing attributes
                            _.each(node.impl().attributes(), function(attr) {
                                if (attr.inlineEditVisibility() && (isDraft || attr.valid())) attr.inlineEditVisibility(false);
                            });
                        } else {
                            Alfresco.util.PopupManager.displayMessage({
                                text: Alfresco.util.message("message.invalid-node.form")
                            });   
                        }
                    }
                });
            }
        },

        initRuntime: function() {
            koutils.enableUserPrompts();
            this.runtime.model(this.options.model);
            ko.applyBindings(this.runtime, Dom.get(this.id));
        },

        initRuntimeCache: function() {
            var runtimeCache = Alfresco.util.ComponentManager.get("InvariantsRuntimeCache");
            if (!runtimeCache) Alfresco.util.ComponentManager.register(new InvariantsRuntimeCache());
        }

    });

    return InvariantsRuntime;
})