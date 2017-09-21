<#import "/ru/citeck/components/journals2/journals.lib.ftl" as journals />

<@markup id="css" >
    <@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/dynamic-tree/action-renderer.css" group="document-journal" />
    <@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/journals2/journals.css" group="document-journal" />
    <@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/journals2/journals-page.css" group="document-journal" />
    <@link rel="stylesheet" href="${page.url.context}/res/yui/calendar/assets/calendar.css" group="document-journal" />
    <@link rel="stylesheet" href="${url.context}/res/citeck/utils/citeck.css" group="document-journal" />
    <@link type="text/css" href="${url.context}/res/citeck/components/document-journal/document-journal.css" group="document-journal" />
</@>

<@markup id="js">
    <@script type="text/javascript" src="${url.context}/res/modules/documentlibrary/doclib-actions.js" group="document-journal" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/error-manager.js" group="document-journal" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/hierarchy-model.js" group="document-journal" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/criteria-model.js" group="document-journal" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/cell-formatters.js" group="document-journal" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/has-buttons.js" group="document-journal" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/action-renderer.js" group="document-journal" />
    <@script type="text/javascript" src="${url.context}/res/yui/calendar/calendar.js" group="document-journal" />
    <@script type="text/javascript" src="${url.context}/res/citeck/utils/citeck.js" group="document-journal" />
    <@script type="text/javascript" src="${url.context}/res/citeck/components/document-journal/document-journal.js" group="document-journal" />
</@>

<#assign id = args.htmlid?html />
<#assign toolbarId = id + "-toolbar" />

<#assign pagingOptions = args.pagingOptions!"10,25,50,100" />

<script type="text/javascript">
  //<![CDATA[
    require(['citeck/components/journals2/journals-page', 'citeck/utils/knockout.yui', 'citeck/utils/knockout.components',
                 'citeck/utils/knockout.invariants-controls'], function(JournalsPage, koyui, kocomponents, koic) {

      new JournalsPage("${id}").setOptions({
        model: {
          journalsList: { 
            id: <#if journalsListId??>"${journalsListId}"<#else>null</#if>,
            documentNodeRef: <#if documentNodeRef??>"${documentNodeRef}"<#else>null</#if>
          },
          journal: <#if journalId??>"${journalId}"<#else>null</#if>,
          filter: <#if filterId??>"${filterId}"<#else>null</#if>,
          settings: <#if settingsId??>"${settingsId}"<#else>null</#if>,
          multiActions: <@journals.renderMultiActionsJSON />,
        },
        cache: <@journals.renderCacheJSON />,
        pagingOptions: [${pagingOptions}]
      }).setMessages(${messages});

      Alfresco.util.createTwister("${id}-heading", "Citeck.widget.DocumentJournal", { panel: "${id}-body" });
      // new Citeck.widget.DocumentJournal("${id}-document-journal").setOptions({}).setMessages(${messages});
    });
  //]]>
</script>

<div id="${id}-document-journal" class="document-journal document-details-panel">
  <h2 id="${id}-heading" class="thin dark alfresco-twister">${msg("header.journal")}</h2>
  <div id="alfresco-journals" class="panel-body" data-bind="css: { loading: loading, filtered: filter() != null }">
    
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

    <div id="${toolbarId}" class="toolbar flat-button icon-buttons" data-bind="css: { hidden: journal() == null }">

      <@journals.renderJournalSelectMenu id />

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

          <!-- ko component: { name: "filter-criteria", params: {
              journalType: $root.resolve("journal.type", null),
              filter: _filter,
              applyCriteria: applyCriteria.bind($root),
              id: "${id}"
          }} --><!-- /ko -->                               
            
        </div>
      </form>
    </div>
    
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
      <div id="${id}-content" class="journal-content">
        <@journals.renderJournalTable />

        <#assign pagingTemplate = '{PreviousPageLink} {PageLinks} {NextPageLink} <span class=rows-per-page-label>' + (msg('label.rows-per-page')?html) + '</span> {RowsPerPageDropdown}' />

        <div id="${id}-paging" class="journal-content-pagination"
               data-bind="<@journals.renderPaginatorBinding pagingTemplate pagingOptions />"></div>
      </div>
    <!-- /ko -->

    <!-- ko if: journal() == null && journalsList() != null && journalsList().journals().length > 0 -->
      <@journals.renderJournalsExpressMenu />
    <!-- /ko -->

  </div>
</div>
