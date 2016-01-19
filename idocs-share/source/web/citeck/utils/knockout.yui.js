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
define(['lib/knockout'], function(ko) {

var koValue = function(value) {
    return typeof value == "function" ? value() : value;
};

var History = YAHOO.util.History,
    Get = YAHOO.util.Get,
    Paginator = YAHOO.widget.Paginator;

ko.bindingHandlers.yuiButton = {
    init: function(element, valueAccessor) {
        var cfg = valueAccessor() || {},
            btn;
        if(cfg.container) {
            btn = new YAHOO.widget.Button(YAHOO.lang.merge(cfg, {
                container: element
            }));
        } else {
            btn = new YAHOO.widget.Button(element, cfg);
        }
        ko.utils.domNodeDisposal.addDisposeCallback(element, function() {
            btn.destroy();
        });
        valueAccessor.btn = btn;
    },
    update: function(element, valueAccessor) {
        var cfg = valueAccessor() || {},
            btn = valueAccessor.btn;
        _.each(cfg, function(value, key) {
            btn.set(key, value);
        });
    }
};

ko.bindingHandlers.yuiPaginator = {
    init: function(element, valueAccessor) {
        var cfg = valueAccessor(),
            pag = new Paginator(YAHOO.lang.merge(cfg, {
                containers: [ element ]
            }));
        valueAccessor.pag = pag;
        pag.render();

        pag.subscribe("changeRequest", function(state) {
            cfg.recordOffset(state.recordOffset);
            cfg.rowsPerPage(state.rowsPerPage);
        });
    },
    update: function(element, valueAccessor) {
        var cfg = valueAccessor() || {},
            pag = valueAccessor.pag;
        pag.setState({
            rowsPerPage:  cfg.rowsPerPage(),
            recordOffset: cfg.recordOffset(),
            totalRecords: cfg.totalRecords()
        });
        pag.render();
    }
};

var DT = {
    getColumns: function(columns, columnTemplate) {
        if(!columnTemplate) return columns;
        return _.map(columns, function(column) {
            column = koValue(column);
            return columnTemplate.call(this, column);
        }, this);
    },
    getFields: function(columns) {
        return _.pluck(columns, 'key');
    },
    update: function(dt, ds, data) {
        ds.liveData = data;
        ds.sendRequest('', {
            success: function(sRequest, oResponse, oPayload) {
                dt.onDataReturnInitializeTable.call(dt, sRequest, oResponse, oPayload);
            }
        });
    },
    getFormatter: function(column) {
        return column.formatter();
    }
};

ko.bindingHandlers.yuiDataTable = {
    init: function(element, valueAccessor) {
        var cfg = valueAccessor(),
            columns = cfg.columns(),
            columnTemplate = cfg.columnTemplate,
            columnDefs = DT.getColumns(columns, columnTemplate),
            fields = koValue(cfg.fields) || [],
            records = cfg.records(),
            ds, dt,
            sortedBy = cfg.sortedBy,
            doubleClickConfig = cfg.doubleClickConfig,
            configs = cfg.set;
        
        // data source
        ds = new YAHOO.util.LocalDataSource({
            records: _.map(records, function(record) {
                return record.model();
            })
        });
        ds.responseType = YAHOO.util.DataSource.TYPE_JSON;
        ds.responseSchema = {
            resultsList: 'records',
            fields: fields
        };
        
        // data table
        dt = new YAHOO.widget.DataTable(element, columnDefs, ds);

        dt.set('formatRow', function(trEl, oRecord) {
            var cfg = valueAccessor(),
                records = cfg.records(),
                nodeRef = oRecord.getData('nodeRef'),
                record = _.find(records, function(record) {
                    return record.nodeRef() == nodeRef;
                });
            if(record == null) return;
            _.defer(ko.applyBindingsToDescendants, record, trEl);
            return true;
        });
        
        // configuration attributes
        if(configs) {
            _.each(configs, function(value, key) {
                dt.set(key, value);
            });
        }


        // highlighting
        dt.subscribe("rowMouseoverEvent", dt.onEventHighlightRow);
        dt.subscribe("rowMouseoutEvent", dt.onEventUnhighlightRow);

        // sortedBy read/write support
        if(sortedBy) {
            dt.set('sortedBy', sortedBy());
            dt.doBeforeSortColumn = function(column, dir) {
                sortedBy({
                    key: column.key,
                    dir: dir
                });
            };
        }
        
        // clicks and touchs
        if(doubleClickConfig) {
            if(Citeck.utils.isMobile()) {
                var longTouchTimer;

                YAHOO.util.Event.addListener(dt.getTableEl(), "touchstart", function(event) {
                    longTouchTimer = setTimeout(function() {
                        var yuiRecord = getYuiRecord(event.target),
                            nodeRef = yuiRecord.getData("nodeRef");

                        if (nodeRef) doubleClickConfig.setter(nodeRef); 
                    }, 500);
                });

                YAHOO.util.Event.addListener(dt.getTableEl(), "touchend", function(event) {
                    if (longTouchTimer) clearTimeout(longTouchTimer);
                });

                YAHOO.util.Event.addListener(dt.getTableEl(), "touchmove", function(event) {
                    if (longTouchTimer) clearTimeout(longTouchTimer);
                });

                YAHOO.util.Event.addListener(dt.getTableEl(), "contextmenu", function(event) {
                    event.preventDefault();
                });

                dt.subscribe("rowClickEvent", function(args) {
                    var cfg = valueAccessor(), 
                        records = cfg.records(),
                        yuiRecord = dt.getRecord(args.target);

                    toggleRow(yuiRecord, getModelRecord(yuiRecord, records));
                });
            } else {
                var simpleClickTimer;

                YAHOO.util.Event.addListener(dt.getTableEl(), "click", function(event) {
                    var cfg = valueAccessor(),
                        records = cfg.records(),
                        yuiRecord = getYuiRecord(event.target);

                    if (simpleClickTimer) {
                        var nodeRef = yuiRecord.getData("nodeRef");
                        clearTimeout(simpleClickTimer);
                        if (nodeRef) doubleClickConfig.setter(nodeRef); 
                    } else {
                        simpleClickTimer = setTimeout(function() {
                            toggleRow(yuiRecord, getModelRecord(yuiRecord, records));
                            simpleClickTimer = null;
                        }, 200)
                    }
                });
            }
        }

        function toggleRow(yuiRecord, modelRecord) {
            if (dt.isSelected(yuiRecord)) {
                modelRecord.selected(false);
                dt.unselectRow(yuiRecord);
            } else {
                modelRecord.selected(true);
                dt.selectRow(yuiRecord);
            }
        }

        function getModelRecord(yuiRecord, modelRecords) {
            for (var i in modelRecords) {
                if (modelRecords[i].nodeRef() == yuiRecord.getData('nodeRef')) {
                    return modelRecords[i];
                }
            }
        }

        function getYuiRecord(eventTarget) {
            while (eventTarget.tagName != "TABLE") {
                if (eventTarget.tagName == 'TR') {
                    return dt.getRecord(eventTarget);
                } else {
                    eventTarget = eventTarget.parentNode;
                }
            }
        }

        // save all necessary
        valueAccessor.ds = ds;
        valueAccessor.dt = dt;

        return { controlsDescendantBindings: true };
    },
    
    update: function(element, valueAccessor) {
        var cfg = valueAccessor(),
            ds = valueAccessor.ds,
            dt = valueAccessor.dt,
            columns = cfg.columns(),
            columnTemplate = cfg.columnTemplate,
            records = cfg.records(),
            columnSet = dt.getColumnSet(),
            columnDefs = columnSet.getDefinitions(),
            newColumnDefs = DT.getColumns(columns, columnTemplate),
            fields = koValue(cfg.fields) || [],
            sortedBy = koValue(cfg.sortedBy);

        
        // remove sortedBy
        dt.set('sortedBy', null);
        
        // remove all columns
        for(var i = columnDefs.length; i--; ) {
            var column = dt.getColumn(columnDefs[i].key);
            if(column) dt.removeColumn(column);
        }
        
        // remove all data
        DT.update(dt, ds, {
            records: []
        });
        
        // add fields
        ds.responseSchema = {
            resultsList: 'records',
            fields: fields
        };
        
        // add columns
        _.each(newColumnDefs, function(def) {
            dt.insertColumn(def);
        });

        // set data
        DT.update(dt, ds, {
            records: _.map(records, function(record) {
                return record.model();
            })
        });
        
        // set sortedBy only if this key is in the columns
        if(sortedBy && _.findWhere(newColumnDefs, { key: sortedBy.key })) {
            dt.set('sortedBy', sortedBy);
        }

        // restore selected rows
        var yuiRecord;
        for (var i in records) {
            yuiRecord = getYuiRecordByNodeRef(records[i].nodeRef());
            if(records[i].selected()) {
                dt.selectRow(yuiRecord);
            } else {
                dt.unselectRow(yuiRecord);
            }
        }

        function getYuiRecordByNodeRef(nodeRef) {
            var recordSet = dt.getRecordSet(),
                yuiRecords = recordSet.getRecords();

            for(var i in yuiRecords) {
                if (yuiRecords[i].getData("nodeRef") == nodeRef) {
                    return yuiRecords[i];
                }
            }
        }
    }
};

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

ko.bindingHandlers.yuiPanel = {
    init: function(element, valueAccessor) {
        var cfg = valueAccessor() || {},
            dlg = new YAHOO.widget.Panel(element);
        valueAccessor.dlg = dlg;
        dlg.render(document.body);
        _.each(cfg, function(value, key) {
            dlg.cfg.setProperty(key, koValue(value));
        });
        if(typeof cfg.visible == "function") {
            dlg.hideEvent.subscribe(function() {
                cfg.visible(false);
            });
            dlg.showEvent.subscribe(function() {
                cfg.visible(true);
            });
        }
    },
    update: function(element, valueAccessor) {
        var cfg = valueAccessor() || {},
            dlg = valueAccessor.dlg;
        _.each(cfg, function(value, key) {
            dlg.cfg.setProperty(key, koValue(value));
        });
    }
};

ko.bindingHandlers.yuiHistory = (function() {
    var initialized = false;
    return {
        init: function(element, valueAccessor) {
            var cfg = valueAccessor(),
                iframe = cfg.iframe,
                states = cfg.states;

            _.each(states, function(variable, module) {
                History.register(module, History.getBookmarkedState(module) || "", function(value) {
                    variable(value);
                });
            });
            History.onReady(function() {
                initialized = true;
                _.each(states, function(variable, module) {
                    variable(History.getCurrentState(module));
                });
            });
        
            History.initialize(element.id, iframe);
        },
    update: function(element, valueAccessor) {
            var cfg = valueAccessor(),
                states = koValue(cfg.states),
                currentStates = {};
            if(!initialized) return;
            _.each(states, function(variable, module) {
                currentStates[module] = variable();
            });
            History.multiNavigate(currentStates);    
    }
    };
})();
ko.virtualElements.allowedBindings.yuiHistory = true;

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

ko.extenders.logChange = function(target, option) {
    var level = typeof option == "string" ? option : option.level,
        logger = Alfresco.logger,
        log = logger[level];
    target.subscribe(function(newValue) {
        log.call(logger, "Observable " + target + " changed value to " + newValue);
    });
    return target;
};

})
