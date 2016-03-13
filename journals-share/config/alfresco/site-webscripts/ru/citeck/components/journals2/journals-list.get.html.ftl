<#import "journals.lib.ftl" as journals />
<#assign id = args.htmlid?html />
<#assign toolbarId = id + "-toolbar" />
<#assign pagingOptions = args.pagingOptions!"10,30,50,100" />

<@markup id="css" >
    <#include "/org/alfresco/components/form/form.css.ftl"/>

    <@link rel="stylesheet" href="${url.context}/res/citeck/utils/citeck.css" group="journals-list" />
    <@link rel="stylesheet" href="${url.context}/res/citeck/components/form/select.css" group="journals-list" />
    <@link rel="stylesheet" href="${url.context}/res/citeck/components/dynamic-tree/action-renderer.css" group="journals-list" />

    <@link rel="stylesheet" href="${page.url.context}/res/citeck/components/journals2/journals.css" group="journals-list"/>
    <@link rel="stylesheet" href="${page.url.context}/res/citeck/components/journals2/journals-page.css" group="journals-list" />
</@>

<@markup id="js">
    <#include "/org/alfresco/components/form/form.js.ftl"/>

    <@script type="text/javascript" src="${url.context}/res/lib/underscore.js" group="journals-list"/>

    <@script type="text/javascript" src="${url.context}/res/modules/documentlibrary/doclib-actions.js" group="journals-list" />

    <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/error-manager.js" group="journals-list" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/hierarchy-model.js" group="journals-list" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/criteria-model.js" group="journals-list" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/cell-formatters.js" group="journals-list" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/has-buttons.js" group="journals-list" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/action-renderer.js" group="journals-list" />

    <@script type="text/javascript" src="${url.context}/res/citeck/utils/citeck.js" group="journals-list" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/form/select.js" group="journals-list"/>
</@>

<@markup id="widgets">
    <@inlineScript group="journals-list">
        new Alfresco.widget.Resizer("journals").setOptions({
            initialWidth: 250
        });
        require(['citeck/components/journals2/journals-page', 'citeck/utils/knockout.yui',
                 'citeck/utils/knockout.invariants-controls'], function(JournalsPage, koyui, koic) {

            new JournalsPage("${id}").setOptions({
                model: {
                    <@journals.renderCurrentIds />
                    multiActions: <@journals.renderMultiActionsJSON />,
                },
                cache: <@journals.renderCacheJSON />,
                pagingOptions: [${pagingOptions}]
            }).setMessages(${messages});
        });
    </@>
</@>

<@markup id="html">
    <script type="html/template" id="hidden-criterion">
        <input type="hidden" data-bind="attr: { name: 'field_' + id() }, value: field().name" />
        <input type="hidden" data-bind="attr: { name: 'predicate_' + id() }, value: predicate().id" />
        <input type="hidden" data-bind="attr: { name: 'value_' + id() }, value: value" />
    </script>

    <script type="html/template" id="visible-criterion">
        <div class="criterion">
                            <span class="criterion-actions">
                                <a class="criterion-remove" title="${msg("button.remove-criterion")}" data-bind="click: $root._filter().criteria.remove.bind($root._filter().criteria, $data)"></a>
                            </span>
                            <span class="criterion-field">
                                <input type="hidden" data-bind="attr: { name: 'field_' + id() }, value: field().name" />
                                <label data-bind="text: field().displayName"></label>
                            </span>
                            <span class="criterion-predicate">
                                <!-- ko if: resolve('field.datatype.predicates.length', 0) == 0 && predicate() != null -->
                                <input type="hidden" data-bind="attr: { name: 'predicate_' + id() }, value: predicate().id" />
                                <!-- /ko -->
                                <!-- ko if: resolve('field.datatype.predicates.length', 0) != 0 -->
                                <select data-bind="attr: { name: 'predicate_' + id() }, value: predicate, options: field().datatype().predicates, optionsText: 'label'"></select>
                                <!-- /ko -->
                            </span>
                            <span class="criterion-value" data-bind="visible: resolve('predicate.needsValue', false)">
                                <!-- ko template: { name: valueTemplate() || 'hidden-value' } -->
                                <!-- /ko -->
                            </span>
            <!-- ko if: $root.resolve('journal.type.formInfo') != null -->
            <div class="hidden" data-bind="
                                templateSetter: {
                                    name: valueTemplate,
                                    field: 'value_' + id(),
                                    url: '${url.context}/page/citeck/components/form-control?htmlid=${id}-criterion-' + id() + '&itemKind=type&itemId=' + $root.journal().type().formInfo().type() + '&formId=' + ($root.journal().type().formInfo().formId()||'') + '&field=' + field().name() + '&name=value_' + id() + '&value=' + encodeURIComponent(value()) + '&disabled=false&mode=create'
                                }">
            </div>
            <!-- /ko -->
        </div>
    </script>

    <script type="html/template" id="hidden-value">
        <input type="hidden" data-bind="attr: { name: 'value_' + id() }, value: value" />
    </script>

    <@uniqueIdDiv>
        <div class="yui-t1" id="alfresco-journals" data-bind="css: { loading: loading, filtered: filter() != null }">
            <div id="yui-main">
                <div class="yui-b" id="alf-content">
                    <div id="${toolbarId}" class="toolbar flat-button icon-buttons" data-bind="css: { hidden: journal() == null }">

                        <@journals.renderCreateVariantsMenu id />

                        <span class="filter" title="${msg("button.filter.tip")}" data-bind="yuiButton: { type: 'checkbox', checked: currentMenu() == 'filter' }">
                            <span class="first-child">
                                <button data-bind="click: toggleToolbarMenu.bind($data, 'filter')">${msg('button.filter')}</button>
                            </span>
                        </span>

                        <span class="settings" title="${msg("button.settings.tip")}" data-bind="yuiButton: { type: 'checkbox', checked: currentMenu() == 'settings' }">
                            <span class="first-child">
                                <button data-bind="click: toggleToolbarMenu.bind($data, 'settings')">${msg("button.settings")}</button>
                            </span>
                        </span>

                        <span class="update" title="${msg("button.update.tip")}" data-bind="yuiButton: { type: 'push' }">
                            <span class="first-child">
                                <button data-bind="click: performSearch">${msg('button.update')}</button>
                            </span>
                        </span>

                        <span class="selected-menu" data-bind="yuiButton: { type: 'menu', menu: '${toolbarId}-selected-menu', disabled: allowedMultiActions().length == 0 }">
                            <span class="first-child">
                                <button>${msg("menu.selected-items")}</button>
                            </span>
                        </span>

                        <@journals.renderCreateReportMenu id />

                        <div class="align-right">
                        <div id="${id}-paging" class="toolbar-paging"
                            data-bind="<@journals.renderPaginatorBinding '{PreviousPageLink} {CurrentPageReport} {NextPageLink}' pagingOptions />">
                        </div>
                        </div>

                        <div id="${toolbarId}-selected-menu" class="yui-overlay yuimenu button-menu">
                            <div class="bd">
                                <ul data-bind="foreach: allowedMultiActions" class="first-of-type">
                                    <li class="yuimenuitem">
                                        <a class="yuimenuitemlabel" data-bind="click: $root.executeAction.bind($root, $data)">
                                            <span data-bind="text: label, css: id"></span>
                                        </a>
                                    </li>
                                </ul>
                                <ul>
                                    <li class="yuimenuitem">
                                        <a class="yuimenuitemlabel" data-bind="click: deselectAllRecords">
                                            <span class="onActionDeselectAll">${msg("menu.selected-items.deselect-all")}</span>
                                        </a>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>
                    <div id="${id}-search" class="form-container">
                        <form id="${id}-search-form" data-bind="submit: applyCriteria">
                            <!-- search criteria -->
                            <div id="${toolbarId}-filter" class="toolbar-menu" data-bind="if: journal() && _filter(), visible: currentMenu() == 'filter'">
                                <div id="${id}-criteria-buttons" class="criteria-buttons flat-button icon-buttons" data-bind="if: journal() != null">

                                    <span class="apply" title="${msg("button.apply-criteria")}" data-bind="yuiButton: { type: 'push', disabled: !_filter().valid() }">
                                        <span class="first-child">
                                            <button data-bind="click: applyCriteria"></button>
                                        </span>
                                    </span>
                                    <span class="reset" title="${msg("button.reset-criteria")}" data-bind="yuiButton: { type: 'push' }">
                                        <span class="first-child">
                                            <button data-bind="click: clearCriteria"></button>
                                        </span>
                                    </span>
                                    <span class="save" title="${msg("button.save-filter")}" data-bind="yuiButton: { type: 'push', disabled: !_filter().valid() }">
                                        <span class="first-child">
                                            <button data-bind="click: saveFilter"></button>
                                        </span>
                                    </span>

                                    <span class="add" data-bind="yuiButton: { type: 'menu', menu: '${id}-add-criterion-menu' }, css: { hidden: resolve('journal.type.searchableAttributes.length', 0) == 0 }">
                                        <span class="first-child">
                                            <button>${msg("button.add-criterion")}</button>
                                        </span>
                                    </span>
                                    <div id="${id}-add-criterion-menu" class="yui-overlay yuimenu button-menu">
                                        <div class="bd">
                                            <ul data-bind="foreach: resolve('journal.type.searchableAttributes', [])" class="first-of-type">
                                                <li class="yuimenuitem">
                                                    <span class="yuimenuitemlabel" data-bind="text: displayName, click: $root.addCriterion.bind($root,name(),'','')"></span>
                                                </li>
                                            </ul>
                                        </div>
                                    </div>
                                </div>
                                <div id="${id}-filter-criteria" class="filter-criteria">
                                <!-- ko template: { name: 'visible-criterion', foreach: _filter().criteria() } -->
                                <!-- /ko -->
                                </div>
                            </div>
                        </form>
                    </div>
                    <!-- other settings -->
                    <div id="${toolbarId}-settings" class="toolbar-menu" data-bind="if: journal() && _settings(), visible: currentMenu() == 'settings'">

                        <div id="${id}-settings-buttons" class="settings-buttons flat-button icon-buttons">
                            <span class="apply" title="${msg("button.apply-settings")}" data-bind="yuiButton: { type: 'push', disabled: !_settings().valid() }">
                                <span class="first-child">
                                    <button data-bind="click: applySettings"></button>
                                </span>
                            </span>
                            <span class="reset" title="${msg("button.reset-settings")}" data-bind="yuiButton: { type: 'push' }">
                                <span class="first-child">
                                    <button data-bind="click: resetSettings"></button>
                                </span>
                            </span>
                            <span class="save" title="${msg("button.save-settings")}" data-bind="yuiButton: { type: 'push', disabled: !_settings().valid() }">
                                <span class="first-child">
                                    <button data-bind="click: saveSettings"></button>
                                </span>
                            </span>
                        </div>

                        <!-- ko if: resolve('journal.type.visibleAttributes.length', 0) > 0 -->
                            <label for="${id}-columns-select" class="columns-select">${msg("label.columns-select")}</label>

                            <#if settingsControlMode??>
                                <#if settingsControlMode == "checkbox">
                                    <!-- ko component: { name: "checkbox-radio", params: {
                                        options: journal().type().visibleAttributes,
                                        value: _settings().visibleAttributes,
                                        optionText: function(option) { return option.displayName },
                                        multiple: true
                                    }} --><!-- /ko -->
                                </#if>
                            <#elseif !settingsControlMode?? || settingsControlMode == "select">
                                <select id="${id}-columns-select" class="columns-select" multiple="true" data-bind="options: journal().type().visibleAttributes, selectedOptions: _settings().visibleAttributes, optionsText: 'displayName'" size="10"></select>
                            </#if>
                        <!-- /ko -->

                    </div>
                    <!-- ko if: journal() != null -->
                    <div id="${id}-content">
                        <@journals.renderJournalTable />
                        <#assign pagingTemplate = '{PreviousPageLink} {PageLinks} {NextPageLink} <span class=rows-per-page-label>' + (msg('label.rows-per-page')?html) + '</span> {RowsPerPageDropdown}' />
                        <div id="${id}-paging" data-bind="<@journals.renderPaginatorBinding pagingTemplate pagingOptions />">
                        </div>
                    </div>
                    <!-- /ko -->

                    <!-- ko if: journal() == null && journalsList() != null && journalsList().journals().length > 0 -->
                    <@journals.renderJournalsExpressMenu />
                    <!-- /ko -->
                </div>
            </div>
            <div class="yui-b" id="alf-filters">
                <!-- ko if: journalsList() != null -->
                <div id="${id}-journals-list-header" class="toolbar" data-bind="with: journalsList">
                    <h2 data-bind="text: title"></h2>
                </div>
                <div class="items-list" data-bind="with: journalsList">
                    <ul data-bind="foreach: journals">
                        <li data-bind="
                            click: $root.journal.bind($root, $data),
                            css: { selected: $root.journal() == $data }
                        ">
                            <span data-bind="text: title"></span>
                        </li>
                    </ul>
                </div>
                <!-- /ko -->

                <div class="toolbar" data-bind="css: { hidden: journal() == null }">
                    <!-- ko if: journalsList() != null -->
                    <h2>${msg("header.filters")}</h2>
                    <!-- /ko -->
                    <!-- ko if: journalsList() == null && journal() != null -->
                    <h2 data-bind="text: journal().title"></h2>
                    <!-- /ko -->
                </div>
                <div class="items-list" data-bind="css: { hidden: journal() == null }">
                    <ul>
                        <li data-bind="
                            click: $root.selectFilter.bind($root, null),
                            css: { selected: $root.filter() == null }
                        ">${msg('filter.default')}</li>
                        <!-- ko foreach: filters -->
                        <li data-bind="
                            click: $root.filter.bind($root, $data),
                            css: { selected: $root.filter() == $data }
                        ">
                            <span data-bind="text: title"></span>
                            <a class="remove" data-bind="
                                click: $root.removeFilter.bind($root, $data),
                                css: { hidden: !permissions().Delete }
                            "></a>
                        </li>
                        <!-- /ko -->
                    </ul>
                </div>

                <div class="toolbar" data-bind="css: { hidden: journal() == null }">
                    <h2>${msg("header.settings")}</h2>
                </div>
                <div class="items-list" data-bind="css: { hidden: journal() == null }">
                    <ul>
                        <li data-bind="
                            click: $root.selectSettings.bind($root, null),
                            css: { selected: $root.settings() == null }
                        ">${msg('settings.default')}</li>
                        <!-- ko foreach: settingsList -->
                        <li data-bind="
                            click: $root.settings.bind($root, $data),
                            css: { selected: $root.settings() == $data }
                        ">
                            <span data-bind="text: title"></span>
                            <a class="remove" data-bind="
                                click: $root.removeSettings.bind($root, $data),
                                css: { hidden: !permissions().Delete }
                            "></a>
                        </li>
                        <!-- /ko -->
                    </ul>
                </div>
            </div>
            <div id="${id}-report-form-container" style="display:none;">
                <form id="${id}-report-form" data-bind="attr: {action: createReportLink, target: createReportTarget}, event: {load: createReportFormInit('${id}-report-form')}" enctype="multipart/form-data" method="post">
                    <input type="hidden" name="jsondata" data-bind="attr: { value: createReportQuery }" />
                </form>
            </div>
        </div>

        <!-- ko gotoAddress: gotoAddress -->
        <!-- /ko -->

        <iframe id="${id}-history-iframe" src="${url.context}/res/favicon.ico" style="position: absolute;top: 0;left: 0;width: 1px;height: 1px;visibility: hidden;"></iframe>
        <input id="${id}-history-field" type="hidden" data-bind="yuiHistory: {
            iframe: '${id}-history-iframe',
            states: {
                journal: journalId,
                filter: filterId,
                settings: settingsId,
                skipCount: skipCountId,
                maxItems: maxItemsId
            }
        } "/>

        <!-- ko dependencies: dependencies --><!-- /ko -->
        <!-- ko dependencies: dependencies --><!-- /ko -->
    </@>
</@>
