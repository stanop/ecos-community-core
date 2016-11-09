<#assign controlId = fieldId + "-journalControl">
<#assign params = viewScope.region.params!{} />

<#assign createVariantsSource = params.createVariantsSource!"create-views" />
<#assign virtualParent = params.virtualParent!"false" />

<div id="${controlId}" class="journal-control" 
    data-bind="journalControl: { value: value, multiple: multiple, options: options }, params: function() {
      return {
        <#if params.journalType??>journalType: '${params.journalType}',</#if>
        <#if params.searchBar??>searchBar: '${params.searchBar}',</#if>
        <#if params.defaultVisibleAttributes??>defaultVisibleAttributes: '${params.defaultVisibleAttributes}',</#if>
        <#if params.defaultSearchableAttributes??>defaultSearchableAttributes: '${params.defaultSearchableAttributes}',</#if>
        <#if params.mode??>mode: '${params.mode}',</#if>

        <#if params.sortBy??>sortBy: ${params.sortBy},</#if>

        <#if params.defaultHiddenByType??>defaultHiddenByType: '${params.defaultHiddenByType}',</#if>

        <#if params.searchMinQueryLength??>searchMinQueryLength: '${params.searchMinQueryLength}',</#if>
        <#if params.searchScript??>searchScript: '${params.searchScript}',</#if>
        <#if params.searchCriteria??>searchCriteria: ${params.searchCriteria},</#if>

        <#if params.defaultCriteria??>defaultCriteria: ${params.defaultCriteria},</#if>

        <#-- Create Object Transition -->
        createVariantsSource: '${createVariantsSource}',
        virtualParent: ${virtualParent}
      }
    }">

    <button id="${controlId}-button" 
            class="journal-control-button" 
            data-bind="disable: protected">${msg(params.buttonTitle!"button.select")}</button>
</div>