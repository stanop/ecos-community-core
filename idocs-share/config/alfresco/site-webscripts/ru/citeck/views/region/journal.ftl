<#assign controlId = fieldId + "-journalControl">
<#assign params = viewScope.region.params!{} />

<#assign title = msg("form.select.label")>
<#assign search = msg("journal.search")>

<#assign elementsTab = msg("journal.elements")>
<#assign filterTab = msg("journal.filter")>
<#assign createTab = msg("journal.create")>
<#assign selectedElements = msg("journal.selected-elements")>
<#assign applyCriteria = msg("journal.apply-criteria")>
<#assign addFilterCriterion = msg("journal.add-filter-criterion")>

<#assign nextPageLabel = msg("journal.pagination.next-page-label")>
<#assign nextPageTitle = msg("journal.pagination.next-page-title")>
<#assign previousPageLabel = msg("journal.pagination.previous-page-label")>
<#assign previousPageTitle = msg("journal.pagination.previous-page-title")>

<#assign submitButtonTitle = msg("button.ok")>
<#assign cancelButtonTitle = msg("button.cancel")>

<div id="${controlId}" class="journal-control" 
    data-bind="journalControl: { value: value, multiple: multiple, options: options }, params: function() {
      return {
        <#if params.journalType??>
          journalType: '${params.journalType}',
        </#if>
        <#if params.searchBar??>
          searchBar: '${params.searchBar}',
        </#if>
        <#if params.defaultVisibleAttributes??>
          defaultVisibleAttributes: '${params.defaultVisibleAttributes}',
        </#if>
        <#if params.defaultSearchableAttributes??>
          defaultSearchableAttributes: '${params.defaultSearchableAttributes}',
        </#if>
        <#if params.mode??>
          mode: '${params.mode}',
        </#if>

        <#if params.sortBy??>
          sortBy: '${params.sortBy}',
        </#if>
        <#if params.orderBy??>
          orderBy: '${params.orderBy}',
        </#if>

        localization: {
          title: '${title}',
          search: '${search}',
          elementsTab: '${elementsTab}',
          filterTab: '${filterTab}',
          createTab: '${createTab}',
          selectedElements: '${selectedElements}',
          applyCriteria: '${applyCriteria}',
          addFilterCriterion: '${addFilterCriterion}',
          submitButton: '${submitButtonTitle}',
          cancelButton: '${cancelButtonTitle}',
          nextPageLabel: '${nextPageLabel}',
          nextPageTitle: '${nextPageTitle}',
          previousPageLabel: '${previousPageLabel}',
          previousPageTitle: '${previousPageTitle}'
        }
      }
    }">

    <button id="${controlId}-button" 
            class="journal-control-button" 
            data-bind="disable: protected">${msg("button.select")}</button>
</div>