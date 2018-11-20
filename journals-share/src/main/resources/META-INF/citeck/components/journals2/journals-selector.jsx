import React, {Fragment, Component} from 'react';

import journals from 'citeck/components/journals2/journals';
import koutils from 'citeck/utils/knockout.utils';
import koyui from 'citeck/utils/knockout.yui';
import kocomponents from 'citeck/utils/knockout.components';
import koic from 'citeck/utils/knockout.invariants-controls';
import ko from 'lib/knockout';

export class JournalsSelector extends Component {
    constructor(props) {
        props.showContextMenu = (props.showContextMenu === undefined) ? true : props.showContextMenu;

        super(props);

        this.currentType = null;
        this.createButtonId = props.id + '-create-button';
        this.controlId = props.id + '-control';

        this.isMultiple = ko.observable(props.isMultiple);
        this.selectedValue = ko.observable();
        this.journalType = ko.observable();
    }

    addAssociation (journalId, journalControlId, selectedType, callback) {
        const CONTROL_ID = this.controlId;

        let Event = YAHOO.util.Event;
        let isMultiple = this.isMultiple;
        let selectedValue = this.selectedValue;

        this.journalType(journalId);
        this.currentType = selectedType;

        $('#' + CONTROL_ID + '-button').click();

        Event.on(CONTROL_ID + '-journalPanel-submitInput', 'click', function () {
            if(typeof callback === 'function'){
                callback(selectedValue(), selectedType);
            }

            if (isMultiple) {
                selectedValue(null);
            }
        });
    };

    componentDidMount() {
        let that = this;

        const CONTROL_ID = that.controlId;

        let props = this.props;
        let selectedValue = that.selectedValue;
        let protect = ko.observable(false);
        let nodeType = ko.observable();
        let Node = koutils.koclass('invariants.Node');

        if(props.showContextMenu){
            that._createContextMenu();
            that._createButton();
        }

        selectedValue.subscribe(function(value) {
            if(typeof props.onSelect === 'function'){
                props.onSelect(value, that.currentType);
            }
        });

        ko.applyBindings(
            {
                'fieldId': CONTROL_ID + '-field',
                'name': ko.observable(CONTROL_ID + '-create-field'),
                'multiple': that.isMultiple,
                'value': selectedValue,
                'protected': protect,
                'journalId': that.journalType,
                'nodetype': nodeType,
                'filterOptions': function(criteria, pagination) {
                    if (!this.cache) this.cache = {};

                    if (!this.cache.result) {
                        this.cache.result = ko.observable([]);
                        this.cache.result.extend({notify: 'always'});
                    }

                    let query = {
                        skipCount: 0,
                        maxItems: 10
                    };

                    if (!_.find(criteria, function (criterion) {
                            return criterion.predicate === 'journal-id';
                        })
                    ){
                        if (!this.nodetype()) {
                            return [];
                        }

                        query['field_1'] = 'type';
                        query['predicate_1'] = 'type-equals';
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
                            return _.some(['field', 'predicate', 'value'], function(ci) {
                                return p.indexOf(ci) !== -1;
                            });
                        })
                    ) {
                        Alfresco.util.Ajax.jsonPost({
                            url: Alfresco.constants.PROXY_URI + 'search/criteria-search',
                            dataObj: query,
                            successCallback: {
                                scope: this.cache,
                                fn: function(response) {
                                    let result = _.map(response.json.results, function(node) {
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
            document.getElementById(CONTROL_ID + '-container')
        );
    }

    _createContextMenu(){
        let that = this;

        const CONTROL_ID = that.controlId;
        const CREATE_BUTTON_ID = that.createButtonId;

        let props = this.props;
        let id = props.id;
        let addable = props.addable || [
            {
                directed: false,
                direction: 'undirected',
                name: 'assoc:associatedWith'
            }
        ];
        let Dom = YAHOO.util.Dom;
        let Event = YAHOO.util.Event;

        let assocTypeMenu = new YAHOO.widget.ContextMenu(
            id + '-contextmenu',
            {
                trigger: CREATE_BUTTON_ID,
                lazyLoad: true
            }
        );

        let getItemsUrl = Alfresco.constants.PROXY_URI + 'citeck/cardlets/sites-and-journals';

        YAHOO.util.Connect.asyncRequest(
            'GET',
            getItemsUrl, {
                success: function (response) {
                    if (response.responseText) {
                        let data = eval('(' + response.responseText + ')');

                        if (data && data.sites && data.sites.length) {
                            let onMenuItemClick = function(journalId, journalControlId, selectedType){
                                this.addAssociation(journalId, journalControlId, selectedType);
                            };

                            let getSubmenu = function (type) {
                                let submenu = {id: type.replace(':', '_')};

                                submenu.itemdata = data.sites.map(function(item) {
                                    let siteId = item.siteId + '-' + type.replace(':', '-');

                                    return {
                                        text: item.siteName,
                                        submenu: {
                                            id: siteId,
                                            itemdata: item.journals.map(function(journal) {
                                                return {
                                                    id: CONTROL_ID + '-' + siteId + '-' + journal.journalId,
                                                    text: journal.journalName,
                                                    onclick: { fn: onMenuItemClick.bind(that, journal.journalId, journal.journalType, type)}
                                                }
                                            })
                                        }
                                    }
                                });
                                return submenu;
                            };

                            let menuItems = [];

                            for (let  j = 0; j < addable.length; j++) {
                                let assoc = addable[j];
                                let type = assoc.name;

                                if(type !== '') {
                                    if(assoc.direction === 'both'
                                        || assoc.direction === 'target'
                                        || assoc.direction === 'undirected') {
                                        menuItems.push({
                                            text: Alfresco.util.message('association.' + type.replace(':', '_') + '.target'),
                                            submenu : getSubmenu(type)
                                        });
                                    }
                                    if(assoc.direction === 'both' || assoc.direction === 'source') {
                                        menuItems.push({
                                            text: Alfresco.util.message('association.' + type.replace(':', '_') + '.source'),
                                            submenu : getSubmenu(type)
                                        });
                                    }
                                }
                            }

                            assocTypeMenu.addItems(menuItems);
                            assocTypeMenu.render(CREATE_BUTTON_ID);
                        }
                    }
                },
                failure: function() {
                    let messageEl = Dom.get(this.id + "-message");
                    messageEl.innerHTML = Alfresco.util.message("journals-selector.assocs-load-error");
                },
                scope: this
            }
        );

        Dom.setStyle(id + '-contextmenu', 'font-size', '13px');

        Event.addListener(
            CREATE_BUTTON_ID,
            'click', function(event) {
                let xy = YAHOO.util.Event.getXY(event);
                assocTypeMenu.cfg.setProperty('xy', xy);
                assocTypeMenu.show();
            }
        );
    }

    _createButton(){
        const CREATE_BUTTON_ID = this.createButtonId;

        let Dom = YAHOO.util.Dom;
        let createButton = Dom.get(CREATE_BUTTON_ID);

        createButton.setAttribute('title', Alfresco.util.message('journals-selector.create-button.label'));
        createButton.setAttribute('class', 'create-link alfresco-twister-actions');
    }

    render() {
        const CREATE_BUTTON_ID = this.createButtonId;

        let props = this.props;
        const id = props.id;
        const createVariantsVisibility = props.createVariantsVisibility;
        const showContextMenu = props.showContextMenu;

        return (
            <Fragment>
                {
                    showContextMenu ? <a id={CREATE_BUTTON_ID}>&nbsp;</a> : null
                }

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