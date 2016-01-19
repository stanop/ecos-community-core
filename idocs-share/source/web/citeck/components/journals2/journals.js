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
		noneActionGroupId = "none",
		buttonsActionGroupId = "buttons",
		defaultActionGroupId = "injournal",
		DELETE_ACTION_ID = "onActionDelete",
		HasButtons = Citeck.widget.HasButtons,
		BulkLoader = Citeck.utils.BulkLoader,
		$buttonSubscribe = HasButtons.subscribe,
		journalsListIdRegexp = new RegExp('^([^-]+)(-(.+))?-([^-]+)$'),
		koclass = koutils.koclass,
		formatters = Citeck.format,
		msg = Alfresco.util.message,
		$isNodeRef = Citeck.utils.isNodeRef;

var defaultFormatters = {
    "qname": formatters.qname(false),
    "date": formatters.date("dd.MM.yyyy"),
    "datetime": formatters.date("dd.MM.yyyy HH:mm"),
    "noderef": formatters.node(),
    "category": formatters.node(),
    "association": formatters.node(),
    "boolean": formatters.bool(msg('label.yes'), msg('label.no')),
    "filesize": formatters.fileSize("attributes['cm:content']"),
    "mimetype": formatters.icon(16, "attributes['cm:name']"),
    "typeName": formatters.typeName()
};

// class declarations:
var criteriaCounter = 0,
	s = String,
	n = Number,
	b = Boolean,
	o = Object,
	JournalsList = koclass('JournalsList'),
	JournalType = koclass('JournalType'),
	Journal = koclass('Journal'),
	Filter = koclass('Filter'),
	Settings = koclass('Settings'),
	Criterion = koclass('Criterion'),
	CreateVariant = koclass('CreateVariant'),
	Attribute = koclass('Attribute'),
	AttributeInfo = koclass('AttributeInfo'),
	Predicate = koclass('Predicate'),
	PredicateList = koclass('PredicateList'),
	Datatype = koclass('Datatype'),
	FormInfo = koclass('FormInfo'),
	Action = koclass('Action'),
	Record = koclass('Record'),
	Column = koclass('Column'),
	ActionsColumn = koclass('ActionsColumn'),
	JournalsWidget = koclass('JournalsWidget'),
	SortBy = koclass('SortBy'),
	last;

// class definitions:
JournalsList
	.key('id', s)
	.property('documentNodeRef', s)
	.property('title', s)
	.property('journals', [ Journal ])
	.property('default', Journal)
	.computed('scope', function() {
		return this.id() ? this.id().replace(journalsListIdRegexp, '$1') : '';
	})
	.computed('scopeId', function() {
		return this.id() ? this.id().replace(journalsListIdRegexp, '$3') : '';
	})
	.computed('listId', function() {
		return this.id() ? this.id().replace(journalsListIdRegexp, '$4') : '';
	})
  .constructor([String, String], function(id, nodeRef) {
    var jl = new JournalsList(id);
    jl.documentNodeRef(nodeRef);
    return jl;
	}, true)
	;

Criterion
	.property('field', AttributeInfo)
	.property('predicate', Predicate)
	.property('value', s)
	.computed('shortModel', function() {
		return {
			field: this.field().name(),
		   		predicate: this.predicate().id(),
			value: this.value()
		};
	})
	.computed('id', function() {
		if(typeof this._id == "undefined") {
			this._id = criteriaCounter++;
		}
		return this._id;
	})
	.computed('query', function() {
		var id = this.id(),
			result = {};
		result['field_' + id] = this.field().name();
		result['predicate_' + id] = this.resolve('predicate.id');
		result['value_' + id] = this.value();
		return result;
	})
	.computed('valueTemplate', {
		read: function() {
				if(!this._valueTemplate) {
					// make this 'private' to suppress cloning
					this._valueTemplate = ko.observable();
				}
				return this._valueTemplate();
		},
		write: function(value) {
			if(!this._valueTemplate) {
				this._valueTemplate = ko.observable();
			}
			this._valueTemplate(value);
		}
	})
	;

CreateVariant
	.property('title', s)
	.property('destination', s)
	.property('type', s)
	.property('formId', s)
	.property('canCreate', b)
	.property('isDefault', b)
	.computed('link', function() {
		var urlTemplate = 'node-create?type={type}&destination={destination}&viewId={formId}';
		return Alfresco.util.siteURL(YAHOO.lang.substitute(urlTemplate, this, function(key, value) {
			if(typeof value == "function") {
				return value();
			}
			return value;	
		}));
	})
	;

JournalType
	.key('id', s)
	.property('options', o)
	.property('formInfo', FormInfo)
	.property('attributes', [ Attribute ])
	.computed('visibleAttributes', function() {
		return _.invoke(_.filter(this.attributes(), function(attr) {
			return attr.visible();
		}), '_info');
	})
	.computed('searchableAttributes', function() {
		return _.invoke(_.filter(this.attributes(), function(attr) {
			return attr.searchable();
		}), '_info');
	})
	.computed('sortableAttributes', function() {
		return _.invoke(_.filter(this.attributes(), function(attr) {
			return attr.sortable();
		}), '_info');
	})
	.computed('groupableAttributes', function() {
		return _.invoke(_.filter(this.attributes(), function(attr) {
			return attr.groupable();
		}), '_info');
	})
	.computed('defaultAttributes', function() {
		return _.invoke(_.filter(this.attributes(), function(attr) {
			return attr.isDefault();
		}), '_info');
	})
	.computed('defaultFilter', function() {
		return new Filter({
			nodeRef: null,
			title: null,
			permissions: { Write: false, Delete: false},
			journalTypes: [ this.id() ],
			criteria: []
		});
	})
	.computed('defaultSettings', function() {
		return new Settings({
			nodeRef: null,
			title: null,
			permissions: { Write: false, Delete: false},
			journalTypes: [ this.id() ],
			visibleAttributes: _.invoke(this.defaultAttributes(), 'name')
		});
	})
	.property('filters', [ Filter ])
	.property('settings', [ Settings ])
	.method('attribute', function(name) {
		return _.find(this.attributes(), function(attr) {
			return attr.name() == name;
		});
	})

	
	;

Journal
	.key('nodeRef', s)
	.property('title', s)
	.property('type', JournalType)
	.property('criteria', [ Criterion ])
	.property('createVariants', [ CreateVariant ])
	.computed('availableCreateVariants', function() {
		return _.filter(this.createVariants(), function(variant) {
			return variant.canCreate();
		});
	})
	.init(function() {
		this.criteria.extend({ rateLimit: 0 });
	})
	;

Filter
	.key('nodeRef', s)
	.property('title', s)
	.property('permissions', o)
	.property('journalTypes', [ JournalType ])
	.property('criteria', [ Criterion ])
	.computed('valid', function() {
		return this.criteria().length > 0;
	})
	.computed('shortModel', function() {
		return {
			criteria: _.invoke(this.criteria(), 'shortModel')
		};
	})
	.computed('saveModel', function() {
		return {
			criteria: _.invoke(this.criteria(), 'shortModel'),
			title: this.title(),
			journalTypes: _.invoke(this.journalTypes(), 'id')
		};
	})
	.init(function() {
		this.criteria.extend({ rateLimit: 0 });
	})
	;

Settings
	.key('nodeRef', s)
	.property('title', s)
	.property('permissions', o)
	.property('journalTypes', [ JournalType ])
	.property('visibleAttributes', [ AttributeInfo ])
	.computed('valid', function() {
		return this.visibleAttributes().length > 0;
	})
	.computed('shortModel', function() {
		return {
			visibleAttributes: _.invoke(this.visibleAttributes(), 'name')
		};
	})
	.computed('saveModel', function() {
		return {
			visibleAttributes: _.invoke(this.visibleAttributes(), 'name'),
			title: this.title(),
			journalTypes: _.invoke(this.journalTypes(), 'id')
		};
	})
	;
	
Attribute
	.property('name', s)
	.property('_info', AttributeInfo)
	.init(function() {
		this.model({ _info: this.name() });
	})
	.shortcut('type', '_info.type')
	.shortcut('displayName', '_info.displayName')
	.shortcut('datatype', '_info.datatype')
	.shortcut('labels', '_info.labels', {})
	.property('visible', b)
	.property('searchable', b)
	.property('sortable', b)
	.property('groupable', b)
	.property('isDefault', b)
	.property('settings', o)
	;

AttributeInfo
	.key('name', s)
	.property('type', s)
	.property('displayName', s)
	.property('datatype', Datatype)
	.property('labels', o)
	;

Datatype
	.key('name', s)
	.property('predicateList', PredicateList)
	.shortcut('predicates', 'predicateList.predicates', [])

	.load('predicateList', function(datatype) {
        YAHOO.util.Connect.asyncRequest(
            'GET', 
            Alfresco.constants.URL_PAGECONTEXT + "search/search-predicates?datatype=" + datatype.name(), 
            {
                success: function(response) {
                    var result = JSON.parse(response.responseText),
                    	predicates = [];

                    for (var i in result.predicates) {
                    	predicates.push(new Predicate(result.predicates[i]))
                    };

                    this.predicateList(new PredicateList({
                    	id: result.datatype, 
                    	predicates: predicates
                    }));
                },

                failure: function(response) {
                    // error
                },

                scope: this
            }
        );
    })

	;

Predicate
	.key('id', s)
	.property('label', s)
	.property('needsValue', b)
	;

PredicateList
	.key('id', s)
	.property('predicates', [ Predicate ])
	;

FormInfo
	.property('type', s)
	.property('formId', s)
	;

Record
	.key('nodeRef', s)
	.property('attributes', o)
	.property('permissions', o)
	.property('aspects', [s])
	.property('isDocument', b)
	.property('isContainer', b)
	.computed('isDoclibNode', function() {
		if(this.isDocument() === true || this.isContainer() === true) {
			return true;
		}
		if(this.isDocument() === false && this.isContainer() === false) {
			return false;
		}
		return null;
	})
	.property('doclib', o) // document library record data
	.computed('selected', {
		read: function() {
			if(!this._selected) {
				this._selected = ko.observable(false);
			}
			return this._selected();
		},
		write: function(selected) {
			this._selected(selected);
		}
	})
    .method('hasAspect', function(aspect) {
        return _.contains(this.aspects(), aspect);
    })
    .method('hasPermission', function(permission) {
        return this.permissions()[permission] === true;
    })
	;
	
Column
	.property('id', s)
	.computed('key', function() {
		var id = this.id();
		return id.match(':') ? 'attributes[\'' + id + '\']' : id;
	})
	.property('_info', AttributeInfo)
	.init(function() {
		this.model({ _info: this.id() });
	})
	.property('formatter', o)
	.property('sortable', b)
	.shortcut('label', '_info.displayName')
	.shortcut('datatype', '_info.datatype.name')
	.shortcut('labels', '_info.labels')
	;
	
Action
	.key('id', s)
	.property('label', s)
	.property('permission', s)
	.property('requiredAspect', s)
	.property('forbiddenAspect', s)
	.property('syncMode', s)
	;

ActionsColumn
	.property('id', s)
	.shortcut('key', 'id')
	.property('formatter', o)
	.constant('sortable', false)
	.property('label', s)
	;
	

SortBy
	.property('id', s)
	.property('order', s)
	.computed('query', function() {
		return {
			attribute: this.id(),
			order: this.order()
		};
	})
	;

JournalsWidget
  .property('documentNodeRef', s)
	.property('journalsLists', [JournalsList])
	.property('journals', [Journal])
	.property('journalsList', JournalsList)
	.shortcut('filters', 'journal.type.filters', [])
	.shortcut('settingsList', 'journal.type.settings', [])
	.property('journal', Journal)
	.property('filter', Filter)
	.property('settings', Settings)
	.property('_filter', Filter)
	.property('_settings', Settings)
	.shortcut('currentFilter', 'filter', 'journal.type.defaultFilter', null)
	.shortcut('currentSettings', 'settings', 'journal.type.defaultSettings', null)
	.computed('journalsListId', {
		read: function() {
			return this.resolve('journalsList.id', '');
		},
		write: function(value) {
			value ? this.journalsList(new JournalsList(value, this.documentNodeRef())) : this.journalsList(null);
		}
	})
	.computed('journalId', {
		read: function() {
			return this.resolve('journal.nodeRef', '');
		},
		write: function(value) {
			value ? this.journal(new Journal(value)) : this.journal(this.resolve('journalsList.default'));
		}
	})
	.computed('filterId', {
		read: function() {
			var filter = this.filter();
			if(!filter) return "";
			if(filter.nodeRef()) return filter.nodeRef();
			return JSON.stringify(filter.shortModel());
		},
		write: function(value) {
			if(!value) {
				this.filter(null);
				return;
			} else if(value.match('^workspace')) {
				this.filter(new Filter(value));
			} else {
				this.filter(new Filter(_.defaults(JSON.parse(value), {
					nodeRef: null,
					title: "",
					criteria: [],
					journalTypes: [],
					permissions: { Write: true, Delete: true },
				})));
			}
		}
	})
	.computed('settingsId', {
		read: function() {
			var settings = this.settings();
			if(!settings) return "";
			if(settings.nodeRef()) return settings.nodeRef();
			return JSON.stringify(settings.shortModel());
		},
		write: function(value) {
			if(!value) {
				this.settings(null);
				return;
			} else if(value.match('^workspace')) {
				this.settings(new Settings(value));
			} else {
				this.settings(new Settings(_.defaults(JSON.parse(value), {
					nodeRef: null,
					title: "",
					visibleAttributes: [],
					journalTypes: [],
					permissions: { Write: true, Delete: true },
				})));
			}
		}
	})
	
	
	// paging
	.property('skipCount', n)
	.property('maxItems', n)
	.property('totalItems', n)
	.property('hasMore', b)
	.computed('totalEstimate', function() {
		var total = this.totalItems();
		if(typeof total != "undefined" && total !== null) {
			return total;
		} else {
			// allow one page only
			return this.skipCount() + this.maxItems() + (this.hasMore() ? 1 : 0);
		}
	})
	.computed('skipCountId', koutils.numberSerializer('skipCount'))
	.computed('maxItemsId', koutils.numberSerializer('maxItems'))
	.property('records', [ Record ])

	// selected records
	.computed('selectedRecords', function() {
		return _.filter(this.records(), function(record) {
			return record.selected();
		});
	})
	.computed('selectedRecordsAreDoclib', function() {
		return _.all(this.selectedRecords(), function(record) {
			return record.isDoclibNode();
		});
	})
	.computed('selectedRecordsAllowedPermissions', function() {
		var records = this.selectedRecords(),
			recordsPermissions = _.invoke(records, 'permissions'),
			allPermissions = _.flatten(_.map(recordsPermissions, _.pairs), true);
		return _.reduce(allPermissions, function(permissions, permission) {
			var name = permission[0], allowed = permission[1];
			if(!_.has(permissions, name) || !allowed) {
				   permissions[name] = allowed;
			}
			return permissions;
		}, {});
	})

	// datatable interface: fields, columns, records
	.shortcut('actionGroupId', 'journal.type.options.actionGroupId', defaultActionGroupId)
	.computed('columns', function() {
		var visibleAttributes = this.resolve('currentSettings.visibleAttributes', []),
			journalType = this.resolve('journal.type'),
			linkFormatterName = 'doubleClickLink',
			recordUrl = this.recordUrl(),
			linkSupplied = recordUrl == null;
		
		// init columns
		var columns = _.map(visibleAttributes, function(attr) {
			var options = journalType ? journalType.attribute(attr.name()) : null,
			    formatter = options ? options.settings().formatter : null,
			    includeLink = false;
			
			if(formatter) {
			    formatter = formatters.loadedFormatter(formatter);
			} else if(attr.labels()) {
			    var classPrefix = attr.name().replace(/\W/g, '_') + "-";
			    formatter = formatters.code(attr.labels(), classPrefix, classPrefix);
			    includeLink = !linkSupplied;
			} else if(attr.datatype()) {
			    formatter = defaultFormatters[attr.datatype().name()];
			    if(!formatter) includeLink = !linkSupplied;
			} else {
			    formatter = formatters.loading();
			}
			
			if(includeLink) {
			    formatter = formatters.doubleClickLink(recordUrl, this.recordIdField(), formatter);
			    linkSupplied = true;
			}
			if(formatter) formatter = formatters.multiple(formatter);
			
			return {
				id: attr.name(),
				sortable: options ? options.sortable() : false,
				formatter: formatter
			};
		}, this);
		
		columns = _.map(columns, Column);
		
		// init action column
		var actionGroupId = this.actionGroupId();
		if(actionGroupId == buttonsActionGroupId) {
			columns.push(new ActionsColumn({
				id: 'actions',
				label: this.msg("column.actions"),
				formatter: 'buttons'
			}));
		} else if(actionGroupId != noneActionGroupId) {
			columns.push(new ActionsColumn({
				id: 'actions',
				label: this.msg("column.actions"),
				formatter: 'actions("' + actionGroupId + '")'
			}));
		}
		// init selected column
		columns.unshift(new ActionsColumn({
			id: 'selected',
			label: '',
			formatter: formatters.checkbox('selected')
		}));
		return columns;
	})
	.computed('fields', function() {
		var defaultFields = [
			{ key: 'nodeRef' },
			{ key: 'type' }
		];
		var attributes = this.resolve('journal.type.attributes', []);
		return _.map(attributes, function(attr) {
			var id = attr.name();
			return {
				key: id.match(':') ? 'attributes[\'' + id + '\']' : id
			};
		}).concat(defaultFields);
	})
	.property('sortBy', [ SortBy ])
	.computed('sortByQuery', function() {
		return _.invoke(this.sortBy() || [], 'query');
	})
	.computed('sortByFirst', {
		read: function() {
			if(this.sortBy().length == 0) {
				return null;
			}
			var sortBy = this.sortBy()[0];
			return {
				key: 'attributes[\'' + sortBy.id() + '\']',
				dir: sortBy.order() == 'asc' ? 'yui-dt-asc' : 'yui-dt-desc' 
			};
		},
		write: function(value) {
			this.sortBy([ 
				new SortBy({
					id: value.key.replace(/^attributes\[\'(.*)\'\]$/, '$1'),
					order: value.dir == 'yui-dt-asc' ? 'asc' : 'desc'
				})
			]);
		}
	})
	.computed('loading', function() {
		return !this.records.loaded();
	})
	.property('selectedId', s)
	.shortcut('recordIdField', 'journal.type.options.doubleClickId', 'nodeRef')
	.shortcut('recordUrl', 'journal.type.options.doubleClickLink', null)
	.computed('gotoAddress', function() {
		var id = this.selectedId(),
			url = this.recordUrl();
		if(!id || !url) return null;
		return YAHOO.lang.substitute(url, {
			id: id
		});
	})
	.computed('dependencies', function() {
		var journalOptions = this.resolve('journal.type.options', {});
		return _.compact([ journalOptions.js, journalOptions.css ]);
	})
	.property('multiActions', [ Action ])
    .computed('allowedMultiActions', function() {
        var records = this.selectedRecords(),
            doclibMode = _.all(records, function(record) {
                return record.isDoclibNode();
            }),
            hasPermission = function(record, permission) {
                return record.hasPermission(permission);
            },
            hasAspect = function(record, aspect) {
                return record.hasAspect(aspect);
            };
        if(records.length == 0) return [];
        return _.filter(this.multiActions(), function(action) {
            // sync mode check:
            if(action.syncMode() != null) return false;
            // doclib mode check:
            if(!doclibMode && action.id() != DELETE_ACTION_ID) return false;
            // permission check
            var permission = action.permission();
            if(permission && !_.all(records, _.partial(hasPermission, _, permission))) {
                return false;
            }
            // required aspect check
            var requiredAspect = action.requiredAspect();
            if(requiredAspect && !_.all(records, _.partial(hasAspect, _, requiredAspect))) {
                return false;
            }
            // forbidden aspect check
            var forbiddenAspect = action.forbiddenAspect();
            if(forbiddenAspect && _.any(records, _.partial(hasAspect, _, forbiddenAspect))) {
                return false;
            }
            
            return true;
        });
    })
    
	.computed('fullscreenLink', function() {
		var journalsList = this.journalsList(),
			journalId = this.journalId(),
			filterId = this.filterId(),
			settingsId = this.settingsId(),
			prefix = '',
			postfix = '',
			tokens = {
				journal: this.journalId(),
				filter: this.filterId(),
				settings: this.settingsId(),
				skipCount: this.skipCountId(),
				maxItems: this.maxItemsId()
			},
			hash = _.map(tokens, function(value, key) {
				return key + '=' + value;
			}).join('&');
		if(journalsList != null) {
			if(journalsList.scope() != 'global') {
				prefix = journalsList.scope() + '/' + journalsList.scopeId() + '/';
			}
			postfix = '/list/' + journalsList.listId();
		}
		return YAHOO.lang.substitute('{context}{prefix}journals2{postfix}#{hash}', {
			context: Alfresco.constants.URL_PAGECONTEXT,
			prefix: prefix,
			postfix: postfix,
			hash: hash
		});
	})

	.init(function() {
		this.columns.extend({ rateLimit: 100 });
		this.records.extend({ rateLimit: 100 });
		this.journal.subscribe(function() {
			// reset filter and settings
			this.filter(null);
			this.settings(null);

			this.sortBy([]);
			this.skipCount(0);
			this.selectedId(null);

			this.performSearch();
		}, this);
		this.currentFilter.subscribe(function() {
			this._filter(this.resolve('currentFilter.clone'));
			this.performSearch();
		}, this);
		this.currentSettings.subscribe(function() {
			this._settings(this.resolve('currentSettings.clone'));
		}, this);
		
		this.skipCount.subscribe(this.performSearch, this);
		this.maxItems.subscribe(this.performSearch, this);
		this.sortByQuery.subscribe(this.performSearch, this);
	})

	.method('performSearch', function() {
		this.records.reload();
	})
	
	.property('createReportType', s)
	.property('createReportDownload', b)
	.property('createReportFormId', s)
	.computed('createReportTarget', function() {
		if (this.createReportDownload() == true)
			return "_self";
		else	
			return '_blank';
	})
	.computed('createReportLink', function() {
		var isDownload = (this.createReportDownload() == true);
		return Alfresco.constants.PROXY_URI + "report/criteria-report?download=" + isDownload;
	})
	.computed('createReportQuery', function() {
		var journal = this.journal();
		if (journal) {
			var journalCriteria = journal.criteria();
			if (journal.criteria.loaded()) {
				var filter = this.currentFilter();
				if (filter) {
					var filterCriteria = filter.criteria();
					if (filter.criteria.loaded()) {
						var query = _.reduce(_.flatten([
							journalCriteria, 
							filterCriteria
						]), function(query, criterion) {
							return _.extend(query, criterion.query());
						}, {});
					
						query.sortBy = this.sortByQuery();
						query.reportType = this.createReportType();
						query.reportTitle = journal.title();
						
						var reportColumns = [];
						var visibleAttributes = this.resolve('currentSettings.visibleAttributes', []);
						
						if (visibleAttributes) {
							reportColumns.push({
								attribute: "rowNum",
								title: "№"
							});
							
							for (var i = 0; i < visibleAttributes.length; i++) {
								reportColumns.push({
									attribute: visibleAttributes[i].name._value(),
									title: visibleAttributes[i].displayName._value()
								});
							}
						}
						
						query.reportColumns = reportColumns;
						query.reportFilename = query.reportTitle + "." + query.reportType;
						
						return JSON.stringify(query);
					}
				}
			}
		}
		
		return "{}";
	})
	.method('createReport', function(reportType, isDownload) {
		this.createReportType(reportType);
		this.createReportDownload(isDownload);
		var reportForm = Dom.get(this.createReportFormId());
		
		if (this.createReportQuery() != "{}")
			reportForm.submit();
	})
	.method('createReportFormInit', function(reportFormId) {
		this.createReportFormId(reportFormId);
	})
	.computed('reportButtonDisabled', function() {
		var records = this.records();
		if (typeof records != "undefined" && records !== null)
			return (records.length == 0);
		else
			return true;
	})

	.method('deselectAllRecords', function() {
		_.each(this.records(), function(record) {
			record.selected(false);
		});
	})
	
	/*********************************************************/
	/*             FILTERS AND SETTINGS FUNCTIONS            */
	/*********************************************************/
	
	.methods({
		addCriterion: function(field, predicate, value) {
			// TODO add default predicate according to journal field settings
			this._filter().criteria.push(new Criterion({
				field: field,
				predicate: predicate || null,
				value: value || ""
			}));
		},
	
		applyCriteria: function() {
			this.filter(this._filter().clone());
		},
	
		clearCriteria: function() {
			this.filter(null);
			this._filter(this.currentFilter().clone());
		},
		
		applySettings: function() {
			this.settings(this._settings().clone());
		},
	
		resetSettings: function() {
			this.settings(null);
			this._settings(this.currentSettings().clone());
		},
	})

	/*********************************************************/
	/*            SELECT, SAVE AND REMOVE METHODS            */
	/*********************************************************/

	.methods({

		selectJournalsList: function(id) {
			this.journalsList(id ? new JournalsList(id) : null);
		},
	
		selectJournal: function(journalId) {
			this.journal(journalId ? new Journal(journalId) : null);
		},

		selectFilter: function(filterId) {
			this.filter(filterId ? new Filter(filterId) : null);
		},

		selectSettings: function(settingsId) {
			this.settings(settingsId ? new Settings(settingsId) : null);
		},

		saveFilter: function() {
			if(!this.resolve('_filter.valid', false)) return;
			this._filter().journalTypes.push(this.journal().type());
			Filter.save(this._filter(), {
				scope: this,
				fn: function(newFilter) {
					this.filter(newFilter);
					this.journal().type().filters.push(newFilter);
				}
			});
		},

		removeFilter: function(filter) {
			if(!filter.nodeRef()) return;
			Filter.remove(filter, {
				scope: this, 
				fn: function() {
					var journalType = this.resolve('journal.type');
					if(journalType) {
						journalType.filters.remove(filter);
					}
					if(this.filter() == filter) {
						this.filter(null);
					}
				}
			});
		},

		saveSettings: function() {
			if(!this.resolve('_settings.valid', false)) return;
			this._settings().journalTypes.push(this.journal().type());
			Settings.save(this._settings(), {
				scope: this,
				fn: function(newSettings) {
					this.settings(newSettings);
					this.journal().type().settings.push(newSettings);
				}
			});
		},

		removeSettings: function(settings) {
			if(!settings.nodeRef()) return;
			Settings.remove(settings, {
				scope: this, 
				fn: function() {
					var journalType = this.resolve('journal.type');
					if(journalType) {
						journalType.settings.remove(settings);
					}
					if(this.settings() == settings) {
						this.settings(null);
					}
				}
			});
		},

		_removeRecord: function(record) {
			Record.remove(record, {
				scope: this,
				fn: function() {
					this.records.remove(record);
				}
			});
		},

		removeRecord: function(record) {
			if(!record.nodeRef()) return;
			this._removeRecord(record);
		},

		removeRecords: function(records) {
			_.each(records, this._removeRecord, this);
		},
	})

	;

/*********************************************************/
/*                        REST API                       */
/*********************************************************/
	
JournalsList
	.load('*', koutils.simpleLoad({
		url: Alfresco.constants.PROXY_URI + "api/journals/list?journalsList={id}&nodeRef={documentNodeRef}"
	}))
	;

JournalType
	.load('filters', koutils.simpleLoad({
		url: Alfresco.constants.PROXY_URI + "api/journals/filters?journalType={id}",
		resultsMap: { filters: 'filters' }
	}))
	.load('settings', koutils.simpleLoad({
		url: Alfresco.constants.PROXY_URI + "api/journals/settings?journalType={id}",
		resultsMap: { settings: 'settings' }
	}))
	.load('*', koutils.simpleLoad({
		url: Alfresco.constants.PROXY_URI + "api/journals/types/{id}",
		resultsMap: function(data) {
			return {
				attributes: data.attributes,
				options: data.settings,
				formInfo: {
					type: data.settings.type,
					formId: data.settings.formId
				},
			}
		}
	}))
	;

Journal
	.load('*', koutils.simpleLoad({
		url: Alfresco.constants.PROXY_URI + "api/journals/journals-config?nodeRef={nodeRef}"
	}))
	;

Filter
	.load('*', koutils.simpleLoad({
		url: Alfresco.constants.PROXY_URI + "api/journals/filter?nodeRef={nodeRef}"
	}))
	.save(koutils.simpleSave({
		url: Alfresco.constants.PROXY_URI + "api/journals/filter",
		toRequest: function(filter) {
			return filter.saveModel();
		},
		toResult: function(model) {
			return new Filter(model);
		}
	}))
	.remove(koutils.simpleSave({
		method: "DELETE",
		url: Alfresco.constants.PROXY_URI + "api/journals/filter?nodeRef={nodeRef}"
	}))
	;

Settings
	.load('*', koutils.simpleLoad({
		url: Alfresco.constants.PROXY_URI + "api/journals/selected-attributes?nodeRef={nodeRef}"
	}))
	.save(koutils.simpleSave({
		url: Alfresco.constants.PROXY_URI + "api/journals/settings-save",
		toRequest: function(settings) {
			return settings.saveModel();
		},
		toResult: function(model) {
			return new Settings(model);
		}
	}))
	.remove(koutils.simpleSave({
		method: "DELETE",
		url: Alfresco.constants.PROXY_URI + "api/journals/settings?nodeRef={nodeRef}"
	}))
	;

AttributeInfo
	.load('*', koutils.bulkLoad(new BulkLoader({
		url: Alfresco.constants.PROXY_URI + "components/journals/journals-metadata", 
		method: "GET",
		emptyFn: function() { return { attributes: [] }; },
		addFn: function(query, id) { 
			if(id) {
				query.attributes.push(id);
				return true;
			} else {
				return false;
			}
		},
		getFn: function(response) { return response.json.attributes; }
	}), 'name'))
	;

JournalsWidget
	.load('journalsLists', koutils.simpleLoad({
		url: Alfresco.constants.PROXY_URI + "api/journals/lists",
		resultsMap: { journalsLists: 'journalsLists' }
	}))
	.load('journals', koutils.simpleLoad({
		url: Alfresco.constants.PROXY_URI + "api/journals/all",
		resultsMap: { journals: 'journals' }
	}))
	.load('records', function() { 
		var load = function() {
			if(this.records.loaded()) {
				logger.debug("Records are already loaded, skipping");
				return;
			}
			
			var journal = this.journal();
			if(!journal) {
				logger.debug("Journal is not loaded, deferring search");
				koutils.subscribeOnce(this.journal, load, this);
				return;
			}

			var journalCriteria = journal.criteria();
			if(!journal.criteria.loaded()) {
				logger.debug("Journal criteria are not loaded, deferring search");
				koutils.subscribeOnce(journal.criteria, load, this);
				return;
			}

			var filter = this.currentFilter();
			if(!filter) {
				logger.debug("Filter is not loaded, deferring search");
				koutils.subscribeOnce(this.currentFilter, load, this);
				return;
			}

			var filterCriteria = filter.criteria();
			if(!filter.criteria.loaded()) {
				logger.debug("Filter criteria are not loaded, deferring search");
				koutils.subscribeOnce(filter.criteria, load, this);
				return;
			}

			var query = _.reduce(_.flatten([
				journalCriteria, 
				filterCriteria
			]), function(query, criterion) {
				return _.extend(query, criterion.query());
			}, {});
		
			query.sortBy = this.sortByQuery();
			query.skipCount = this.skipCount() || 0;
			query.maxItems = this.maxItems() || 10;
		
			logger.info("Loading records with query: " + JSON.stringify(query));

			Alfresco.util.Ajax.jsonPost({
				url: Alfresco.constants.PROXY_URI + "search/criteria-search",
				dataObj: query,
				successCallback: {
					scope: this,
					fn: function(response) {
						var data = response.json;
						this.model({
							records: data.results,
							skipCount: data.paging.skipCount,
							maxItems: data.paging.maxItems,
							totalItems: data.paging.totalItems,
							hasMore: data.paging.hasMore
						});
					}
				}
			});
		};
		load.call(this);
	})
	;

var recordLoader = new Citeck.utils.DoclibRecordLoader();
Record
	// TODO define load method - to load selected records
	.load('doclib', function(record) {
		if(record.isDoclibNode() === true) {
			recordLoader.load(record.nodeRef(), function(id, model) {
				record.model({ doclib: model });
			});
		} else if(record.isDoclibNode() === false) {
			record.doclib(null);
		} else {
			// if it is not loaded yet - do not do anything
		}
	})
	.remove(koutils.simpleSave({
		method: "DELETE",
		url: Alfresco.constants.PROXY_URI + "citeck/node?nodeRef={nodeRef}"
	}))
	;


	/*********************************************************/
	/*              KNOCKOUT PERFORMANCE TUNING              */
	/*********************************************************/

JournalsWidget
//	.extend('*', { logChange: true })
//	.extend('columns', { rateLimit: 0 })
//	.extend('records', { rateLimit: 0 })
//	.extend('*', { rateLimit: 0 })
	;

var rateLimit = { rateLimit: { timeout: 5, method: "notifyWhenChangesStop" } };

AttributeInfo
	.extend('*', rateLimit)
	;

Datatype
	.extend('*', rateLimit)
	;

Column
	.extend('*', rateLimit)
	;

// Journals widget class

var Journals = function(name, htmlid, dependencies, ViewModelClass) {
	Journals.superclass.constructor.call(this, name || "Citeck.widgets.Journals", htmlid, dependencies);
	this.viewModel = new ViewModelClass({});

	// inject msg method
	this.viewModel.msg = this.bind(this.msg);
};

YAHOO.extend(Journals, Alfresco.component.Base, {

	options: {
	
		predicateLists: [],
		
	},
	
	onReady: function() {
		// init objects from cache
		this.initCachedObjects();
		
		// init viewmodel
		this.viewModel.model(this.options.model);

		// init views
		ko.applyBindings(this.viewModel, Dom.get(this.id));
	},

	initCachedObjects: function() {
		_.each(this.options.cache, function(models, className) {
			var constructor = koclass(className);
			_.each(models, function(model) {
				if(!model) return;
				new constructor(model);
			});
		});
	},

});

return Journals;

})
