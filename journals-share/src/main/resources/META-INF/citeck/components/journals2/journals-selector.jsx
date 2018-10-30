import React, {Fragment, Component} from 'react';

import journals from 'citeck/components/journals2/journals';
import koutils from 'citeck/utils/knockout.utils';
import koyui from 'citeck/utils/knockout.yui';
import kocomponents from 'citeck/utils/knockout.components';
import koic from 'citeck/utils/knockout.invariants-controls';
import ko from 'lib/knockout';

export class JournalsSelector extends Component {
    componentDidMount() {
        let props = this.props;
        let Dom = YAHOO.util.Dom;
        let Event = YAHOO.util.Event;
        let Element = YAHOO.util.Element;
        let id = props.id;
        let controlId = id + "-control";
        let createButton = Dom.get(id + '-create-button');
        let isMultiple = ko.observable(props.isMultiple);
        let selectedValue = ko.observable();
        let protect = ko.observable(false);
        let journalType = ko.observable();
        let nodeType = ko.observable();
        let Node = koutils.koclass('invariants.Node');
        let addable = props.addable || [
            // {
            //     directed: false,
            //     direction: 'undirected',
            //     name: 'assoc:associatedWith'
            // }
        ];
        let assocTypeMenu = new YAHOO.widget.ContextMenu(
            id + "-contextmenu",
            {
                trigger: id + '-create-button',
                lazyLoad: true
            }
        );
        let currentType;

        createButton.setAttribute('title', Alfresco.util.message('create-button.label'));
        createButton.setAttribute('class', 'create-link alfresco-twister-actions');

        selectedValue.subscribe(function(newValue) {
            props.onSelect(newValue, currentType);
        });

        ko.applyBindings(
            {
                "fieldId": controlId + "-field",
                "name": ko.observable(controlId + "-create-field"),
                "multiple": isMultiple,
                "value": selectedValue,
                "protected": protect,
                "journalId": journalType,
                "nodetype": nodeType,
                "filterOptions":function(criteria, pagination) {

                    if (!this.cache) this.cache = {};
                    if (!this.cache.result) {
                        this.cache.result = ko.observable([]);
                        this.cache.result.extend({notify: 'always'});
                    }

                    var query = {
                        skipCount: 0,
                        maxItems: 10
                    };
                    if (!_.find(criteria, function (criterion) {
                        return criterion.predicate == 'journal-id';
                    })) {
                        if (!this.nodetype()) {
                            return [];
                        }

                        query['field_1'] = "type";
                        query['predicate_1'] = "type-equals";
                        query['value_1'] = this.nodetype();
                    }

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
                }
            },
            document.getElementById(controlId + "-container")
        );

        var addAssociation = function (journalId, journalControlId, selectedType) {
            journalType(journalId);
            currentType = selectedType;

            $("#" + controlId + "-button").click();
            Event.on(controlId + "-journalPanel-submitInput", "click", function (event) {
                if (isMultiple) {
                    selectedValue(null);
                }
            });
        };

        var getItemsUrl = Alfresco.constants.PROXY_URI + 'citeck/cardlets/sites-and-journals';

        YAHOO.util.Connect.asyncRequest(
            'GET',
            getItemsUrl, {
                success: function (response) {
                    if (response.responseText) {
                        var data = eval('(' + response.responseText + ')');
                        if (data && data.sites && data.sites.length) {
                            var getSubmenu = function (type) {
                                var submenu = {id: type.replace(":", "_")};
                                submenu.itemdata = data.sites.map(function(item) {
                                    var siteId = item.siteId + "-" + type.replace(":", "-");
                                    return {
                                        text: item.siteName,
                                        submenu: {
                                            id: siteId,
                                            itemdata: item.journals.map(function(journal) {
                                                return {
                                                    id: controlId + "-" + siteId + "-" + journal.journalId,
                                                    text: journal.journalName,
                                                    onclick: { fn: addAssociation.bind(null, journal.journalId, journal.journalType, type)}
                                                }
                                            })
                                        }
                                    }
                                });
                                return submenu;
                            };
                            var menuItems = [];
                            for (var  j = 0; j < addable.length; j++) {
                                var assoc = addable[j],
                                    type = assoc.name;
                                if(type != '') {
                                    if(assoc.direction == "both"
                                        || assoc.direction == "target"
                                        || assoc.direction == "undirected") {
                                        menuItems.push({
                                            text: Alfresco.util.message("association." + type.replace(":", "_") + ".target"),
                                            submenu : getSubmenu(type)
                                        });
                                    }
                                    if(assoc.direction == "both" || assoc.direction == "source") {
                                        menuItems.push({
                                            text: Alfresco.util.message("association." + type.replace(":", "_") + ".source"),
                                            submenu : getSubmenu(type)
                                        });
                                    }
                                }
                            }

                            assocTypeMenu.addItems(menuItems);
                            assocTypeMenu.render(id + '-create-button');
                        }
                    }
                },
                failure: function() {
                    var messageEl = Dom.get(this.id + "-message");
                    messageEl.innerHTML = Alfresco.util.message("assocs-load-error");
                },
                scope: this
            }
        );

        Dom.setStyle(id + '-contextmenu', "font-size", "13px");

        Event.addListener(
            id + '-create-button',
            "click", function(event) {
                var xy = YAHOO.util.Event.getXY(event);
                assocTypeMenu.cfg.setProperty("xy", xy);
                assocTypeMenu.show();
            }
        );
    }

    render() {
        let props = this.props;
        const id = props.id;
        const createVariantsVisibility = props.createVariantsVisibility;

        return (
            <Fragment>
                <a id={`${id}-create-button`}>&nbsp;</a>

                <div id={`${id}-control-container`} >
                    <div id={`${id}-control`} data-bind={
                        `journalControl: { value: value, multiple: multiple }, params: function() {
                           return {
                                journalType: journalId,
                                mode: 'collapse',
                                hightlightSelection: true,
                                removeSelection: true,
                                createVariantsSource: 'journal-create-variants',
                                createVariantsVisibility: ${createVariantsVisibility}
                            }}`
                        }
                    />
                </div>

                <div id={`${id}-control-button`} />
            </Fragment>
        );
    }
}