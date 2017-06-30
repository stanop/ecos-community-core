/*
 * Copyright (C) 2008-2016 Citeck LLC.
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
define(['jquery', 'citeck/utils/knockout.utils', 'citeck/components/journals2/journals', 'lib/knockout'],
    function (jq, koutils, Journals, ko) {

        var PopupManager = Alfresco.util.PopupManager,
            koclass = koutils.koclass,
            JournalsWidget = koclass('JournalsWidget'),
            JournalsPage = koclass('JournalsPage', JournalsWidget),
            Record = koclass('Record'),
            msg = Alfresco.util.message,
            Dom = YAHOO.util.Dom;

        JournalsPage
        // load filter method
            .property('loadFilterMethod', String)
            .load('loadFilterMethod', function () {
                this.loadFilterMethod("onclick")
            })
            .computed('filterVisibility', function () {
                switch (this.loadFilterMethod()) {
                    case "onstart":
                    case "loaded":
                        return true;

                    case "onclick":
                    default:
                        return false;
                }
                ;
            })

            // menu
            .property('currentMenu', String)
            .method('toggleToolbarMenu', function (menu) {
                if (menu == "filter") this.loadFilterMethod("loaded");

                if (this.currentMenu() == menu) {
                    this.currentMenu('');
                } else {
                    this.currentMenu(menu);
                }
            })

            // actions support
            .method('executeAction', function (action) {
                var vms = this.selectedRecords(),
                    records = [];
                if (action.isDoclib()) {
                    for (var i in vms) {
                        var vm = vms[i],
                            loaded = vm.doclib.loaded(),
                            record = vm.doclib();
                        if (record) {
                            records.push(record);
                        } else if (!loaded) {
                            koutils.subscribeOnce(vm.doclib, _.partial(this.executeAction, action), this);
                            return;
                        } else {
                            throw new Error("doclib actions can be executed only on doclib nodes");
                        }
                    }
                } else {
                    records = vms;
                }
                this.actionsRuntime[action.func()](records, action);
            })

            // add user interaction for save and remove methods:
            .method('saveFilter', function () {
                this.userInteraction.simulateChange();
                if (!this._filter().valid()) return;
                this.userInteraction.askTitle({
                    callback: {
                        scope: this,
                        fn: function (title) {
                            this._filter().title(title);
                            this.$super.saveFilter();
                        }
                    },
                    title: this.msg("title.save-filter"),
                    text: this.msg("label.save-filter"),
                });
            })
            .method('removeFilter', function (filter) {
                this.userInteraction.askConfirm({
                    callback: {
                        scope: this,
                        fn: function () {
                            this.$super.removeFilter(filter);
                        },
                    },
                    text: this.msg("message.confirm.delete", filter.title()),
                    title: this.msg("message.confirm.delete.1.title"),
                });
            })
            .method('saveSettings', function () {
                if (!this._settings().valid()) return;
                this.userInteraction.askTitle({
                    callback: {
                        scope: this,
                        fn: function (title) {
                            this._settings().title(title);
                            this.$super.saveSettings();
                        }
                    },
                    title: this.msg("title.save-settings"),
                    text: this.msg("label.save-settings"),
                });
            })
            .method('removeSettings', function (settings) {
                this.userInteraction.askConfirm({
                    callback: {
                        scope: this,
                        fn: function () {
                            this.$super.removeSettings(settings);
                        },
                    },
                    text: this.msg("message.confirm.delete", settings.title()),
                    title: this.msg("message.confirm.delete.1.title"),
                });
            })
            .method('removeRecord', function (record) {
                this.userInteraction.askConfirm({
                    callback: {
                        scope: this,
                        fn: function () {
                            this.$super.removeRecord(record);
                        },
                    },
                    text: this.msg("message.confirm.delete", record.attributes()['cm:name']),
                    title: this.msg("message.confirm.delete.1.title"),
                });
            })
            .method('removeRecords', function (records) {
                this.userInteraction.askConfirm({
                    callback: {
                        scope: this,
                        fn: function () {
                            this.$super.removeRecords(records);
                        },
                    },
                    text: this.msg("message.confirm.delete.description", records.length),
                    title: this.msg("message.confirm.delete.title"),
                });
            })
            .method('performSearch', function () {
                this.userInteraction.simulateChange();
                this.$super.performSearch();
            })
            .method('applyCriteria', function () {
                this.userInteraction.simulateChange();
                this.$super.applyCriteria();
            })

            .method('toggleSidebar', function (data, event) {
                $("#alf-filters").toggle();
                $("#alfresco-journals #alf-content .toolbar .sidebar-toggle").toggleClass("yui-button-selected");
            })
        ;

        var JournalsPageWidget = function (htmlid) {
            JournalsPageWidget.superclass.constructor.call(this,
                "Citeck.widgets.JournalsPage",
                htmlid,
                ["button", "menu", "history", "paginator", "dragdrop"],
                JournalsPage);

            // doclib actions parameters:
            this.currentPath = "/";
            this.options.containerId = "documentLibrary",
                this.options.rootNode = "alfresco://company/home";

            this.viewModel.actionsRuntime = this;
            this.viewModel.userInteraction = this;
        };

        YAHOO.extend(JournalsPageWidget, Journals, {

            onReady: function () {
                JournalsPageWidget.superclass.onReady.apply(this, arguments);

                _.reduce(['metadataRefresh', 'fileDeleted', 'folderDeleted', 'filesDeleted'], function (memo, eventName) {
                    YAHOO.Bubbling.on(eventName, function (layer, args) {
                        this.viewModel.performSearch()
                    }, this);
                }, null, this);

                YAHOO.Bubbling.on('removeJournalRecord', function (layer, args) {
                    this.viewModel.removeRecord(new Record(args[1]));
                }, this);
            },

            simulateChange: function () {
                // simulate change on hidden fields,
                // to force view models update

                $('#' + this.id + '-filter-criteria input[type="hidden"]').trigger('change');
            },

            askTitle: function (config) {
                return PopupManager.getUserInput(_.defaults(config, {
                    input: "text",
                    okButtonText: this.msg("button.save")
                }));
            },

            askConfirm: function (config) {
                var callback = config.callback;
                if (!callback) throw new Error("Callback should be specified");
                return PopupManager.displayPrompt(_.defaults(config, {
                    buttons: [
                        {
                            text: this.msg("button.yes"),
                            handler: function () {
                                callback.fn.call(callback.scope);
                                this.destroy();
                            }
                        },
                        {
                            text: this.msg("button.no"),
                            handler: function () {
                                this.destroy();
                            },
                            isDefault: true
                        }
                    ]
                }));
            },

        });

        /*********************************************************/
        /*           DOCUMENT LIBRARY ACTIONS SUPPORT            */
        /*********************************************************/

        _.extend(JournalsPageWidget.prototype, Alfresco.doclib.Actions.prototype, {

            // override for deleting multiple records
            onActionDelete: function (record) {
                if (_.isArray(record)) {
                    this.viewModel.removeRecords(record);
                } else {
                    this.viewModel.removeRecord(record);
                }
            },

            onBatchEdit: function (records, action) {

                var editStatus = {}, ref;

                var defaultValueMapping = {
                    "boolean": false,
                    "text": "",
                    "int": 0,
                    "double": 0,
                    "long": 0,
                    "float": 0,
                    "association": null,
                    "mltext": ""
                };

                var id = Alfresco.util.generateDomId();
                var attribute = action.attribute();
                var datatype = attribute.datatype().name();
                var editValue = ko.observable(defaultValueMapping[datatype]);
                var journalsPage = this;

                var confirmPopup = function (text, onYes, onNo) {
                    Alfresco.util.PopupManager.displayPrompt({
                        title: Alfresco.util.message("message.confirm.title"),
                        text: text,
                        noEscape: true,
                        buttons: [
                            {
                                text: msg("actions.button.ok"),
                                handler: onYes
                            },
                            {
                                text: msg("actions.button.cancel"),
                                handler: onNo,
                                isDefault: true
                            }]
                    });
                };

                var setStatus = function (record, status) {
                    var recordAttributes = record.attributes();
                    editStatus[record.nodeRef()] = {
                        status: status,
                        title: recordAttributes["cm:title"] || recordAttributes["cm:name"]
                    };
                };

                var showResults = function (response) {
                    var panel = new YAHOO.widget.Panel(id + "-results-panel", {
                        width: "40em",
                        fixedcenter: YAHOO.env.ua.mobile === null ? "contained" : false,
                        constraintoviewport: true,
                        underlay: "shadow",
                        close: true,
                        modal: true,
                        visible: true,
                        draggable: true,
                        postmethod: "none", // Will make Dialogs not auto submit <form>s it finds in the dialog
                        hideaftersubmit: false, // Will stop Dialogs from hiding themselves on submits
                        fireHideShowEvents: true
                    });

                    panel.setHeader("Batch edit results");

                    for (ref in editStatus) {
                        if (editStatus[ref].status == "PENDING") {
                            editStatus[ref].status = ((response || {})[ref] || {})[attribute.name()] || "RESPONSE_ERR";
                        }
                    }

                    var body = '<table class="batch-edit-results">';
                    for (ref in editStatus) {
                        body += '<tr><td>' + editStatus[ref].title + '</td><td>' + Alfresco.util.message("batch-edit.message."+editStatus[ref].status) + '</td></tr>';
                    }
                    body += '</table>';
                    body += '<div class="form-buttons batch-edit-results-form-buttons"><input id="' + id + '-close-results-btn" type="button" class="batch-edit-results-button" value="' + msg("button.ok") + '" /></div>';

                    panel.setBody(body);
                    panel.render(document.body);

                    Dom.get(id + '-close-results-btn').onclick = function () {
                        panel.destroy();
                    }
                };

                var processRecords = function (records, options) {

                    var nodes = _.map(records, function (r) {
                        return r.nodeRef();
                    });
                    var attributes = {};
                    attributes[attribute.name()] = editValue();

                    Alfresco.util.Ajax.jsonPost({
                        url: Alfresco.constants.PROXY_URI + "api/journals/batch-edit",
                        dataObj: {
                            "nodes": nodes,
                            "attributes": attributes,
                            "skipInStatuses": options.skipInStatuses
                        },
                        successCallback: {
                            scope: this,
                            fn: function (response) {
                                journalsPage.viewModel.performSearch();
                                panel.destroy();
                                showResults(response.json);
                            }
                        },
                        failureCallback: {
                            scope: this,
                            fn: function (response) {
                                Alfresco.util.PopupManager.displayMessage({
                                    text: msg("message.failure") + ':' + response.json.message,
                                    displayTime: 4
                                });
                            }
                        }
                    });
                };

                var onSubmit = function () {
                    var filterByOptions = function (records, idx, result, callback, options) {
                        if (idx >= records.length) {
                            var repoOptions = {};
                            if (options.skipInStatuses) {
                                repoOptions.skipInStatuses = options.skipInStatuses;
                            }
                            callback(result, repoOptions);
                            return;
                        }
                        var recordAttributes = records[idx].attributes();
                        var value = recordAttributes[attribute.name()];

                        var isEmptyValue = true;
                        if (value && value instanceof Array && value.length > 0) {
                            isEmptyValue = false;
                        } else if (value && !(value instanceof Array)) {
                            isEmptyValue = false;
                        }

                        if (!isEmptyValue) {
                            if (!options.changeExistsValue) {
                                setStatus(records[idx], "SKIPPED");
                                filterByOptions(records, idx + 1, result, callback, options);
                            } else {
                                if (options.confirmChange) {
                                    var fieldTitle = attribute.displayName();
                                    var documentTitle = recordAttributes["cm:title"] || recordAttributes["cm:name"];
                                    confirmPopup("В документе '" + documentTitle + "' значение поля '"
                                        + fieldTitle + "' равно '" + value
                                        + "'. Желаете его заменить?", function () {
                                        this.destroy();
                                        result.push(records[idx]);
                                        filterByOptions(records, idx + 1, result, callback, options);
                                    }, function () {
                                        this.destroy();
                                        setStatus(records[idx], "CANCELLED");
                                        filterByOptions(records, idx + 1, result, callback, options);
                                    })
                                } else {
                                    result.push(records[idx]);
                                    filterByOptions(records, idx + 1, result, callback, options);
                                }
                            }
                        } else {
                            if (options.skipEmptyValues) {
                                setStatus(records[idx], "SKIPPED");
                            } else {
                                result.push(records[idx]);
                            }
                            filterByOptions(records, idx + 1, result, callback, options);
                        }
                        return result;
                    };

                    if (datatype != "association" || editValue() != null) {
                        // Set default values when options are not specified in the configuration
                        var options = {
                            confirmChange: false,
                            skipEmptyValues: false,
                            changeExistsValue: true,
                            skipInStatuses: []
                        };
                        var useFilter = false;
                        var confirmChange = action.settings().confirmChange;
                        if (confirmChange) {
                            if (confirmChange === 'true') {
                                options.confirmChange = true;
                            } else {
                                options.confirmChange = false;
                            }
                            useFilter = true;
                        }
                        var skipEmptyValues = action.settings().skipEmptyValues;
                        if (skipEmptyValues) {
                            if (skipEmptyValues === 'true') {
                                options.skipEmptyValues = true;
                            } else {
                                options.skipEmptyValues = false;
                            }
                            useFilter = true;
                        }
                        var changeExistsValue = action.settings().changeExistsValue;
                        if (changeExistsValue) {
                            if (changeExistsValue === 'true') {
                                options.changeExistsValue = true;
                            } else {
                                options.changeExistsValue = false;
                            }
                            useFilter = true;
                        }
                        var skipInStatusesValue = action.settings().skipInStatuses;
                        if (skipInStatusesValue) {
                            if (skipInStatusesValue.length > 0) {
                                var statusesArray = skipInStatusesValue.split(",");
                                options.skipInStatuses = statusesArray;
                            }
                        }

                        if (useFilter) {
                            filterByOptions(records, 0, [], processRecords, options);
                        } else {
                            processRecords(records, options);
                        }
                    }
                };

                var panel = new YAHOO.widget.Panel(id + "-batch-edit-container", {
                    width: "40em",
                    fixedcenter: YAHOO.env.ua.mobile === null ? "contained" : false,
                    constraintoviewport: true,
                    underlay: "shadow",
                    close: true,
                    modal: true,
                    visible: true,
                    draggable: true,
                    postmethod: "none", // Will make Dialogs not auto submit <form>s it finds in the dialog
                    hideaftersubmit: false, // Will stop Dialogs from hiding themselves on submits
                    fireHideShowEvents: true
                });

                panel.setHeader("Batch edit");

                var body =
                    '<div id="batch-edit-div">' +
                    '<div class="batch-edit-criterion">' +
                    '<div class="criterion-label">' +
                    '<span>' + action.attribute().displayName() + '</span>' +
                    '</div>' +
                    '<!-- ko component: { name: "filter-criterion-value", params: {\
                        fieldId: fieldId,\
                        datatype: action.attribute().datatype().name(),\
                        value: value,\
                        attribute: action.attribute\
                    }} --><!-- /ko -->' +
                    '</div>' +
                    '<div class="form-buttons">' +
                    '<input id="' + id + '-form-submit" type="button" value="' + msg("button.send") + '" />' +
                    '<input id="' + id + '-form-cancel" type="button" value="' + msg("button.cancel") + '" />' +
                    '</div>' +
                    '</div>';

                panel.setBody(body);
                panel.render(document.body);
                ko.applyBindings({
                    "action": action,
                    "fieldId": id + "-field",
                    "value": editValue
                }, Dom.get("batch-edit-div"));

                var submitBtn = document.getElementById(id + "-form-submit");
                submitBtn.onclick = onSubmit;

                var cancelBtn = document.getElementById(id + "-form-cancel");
                cancelBtn.onclick = function () {
                    panel.destroy();
                };

                for (var i in records) {
                    setStatus(records[i], "PENDING");
                }
            }

        });

        return JournalsPageWidget;

    });
