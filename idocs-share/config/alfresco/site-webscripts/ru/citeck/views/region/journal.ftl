<#assign controlId = fieldId + "-journalControl">
<#assign params = viewScope.region.params!{} />

<#assign title = msg("form.select.label")>

<#assign elementsTab = msg("journal.elements")>
<#assign searchTab = msg("journal.search")>
<#assign selectedElements = msg("journal.selected-elements")>
<#assign applyCriteria = msg("journal.apply-criteria")>
<#assign addSearchCriterion = msg("journal.add-search-criterion")>

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
        <#if params.filterMode??>
          filterMode: '${params.filterMode}',
        </#if>

        localization: {
          title: '${title}',
          elementsTab: '${elementsTab}',
          searchTab: '${searchTab}',
          selectedElements: '${selectedElements}',
          applyCriteria: '${applyCriteria}',
          addSearchCriterion: '${addSearchCriterion}',
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