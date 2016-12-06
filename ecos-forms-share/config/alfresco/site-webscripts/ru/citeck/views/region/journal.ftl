<#assign controlId = fieldId + "-journalControl">
<#assign params = viewScope.region.params!{} />

<#assign createVariantsVisibility = params.createVariantsVisibility!"true" />

<#assign createVariantsSource = params.createVariantsSource!"create-views" />
<#assign virtualParent = params.virtualParent!"false" />

<#assign hightlightSelection>
    <#if params.hightlightSelection??>
        ${params.hightlightSelection}
    <#elseif config.scoped["InvariantControlsConfiguration"]?? && 
             config.scoped["InvariantControlsConfiguration"].journal?? &&
             config.scoped["InvariantControlsConfiguration"]["journal"].attributes["hightlightSelection"]??>
        ${config.scoped["InvariantControlsConfiguration"]["journal"].attributes["hightlightSelection"]}
    <#else>false</#if>
</#assign>

<#assign dock>
    <#if params.dock??>
        ${params.dock}
    <#elseif config.scoped["InvariantControlsConfiguration"]?? &&
             config.scoped["InvariantControlsConfiguration"].journal?? &&
             config.scoped["InvariantControlsConfiguration"]["journal"].attributes["dock"]??>
        ${config.scoped["InvariantControlsConfiguration"]["journal"].attributes["dock"]}
    <#else>true</#if>
</#assign>

<#assign mode>
    <#if params.mode??>
        ${params.mode}
    <#elseif config.scoped["InvariantControlsConfiguration"]?? && 
             config.scoped["InvariantControlsConfiguration"].journal?? &&
             config.scoped["InvariantControlsConfiguration"]["journal"].attributes["mode"]??>
        ${config.scoped["InvariantControlsConfiguration"]["journal"].attributes["mode"]}
    <#else>collapse</#if>
</#assign>

<div id="${controlId}" class="journal-control" 
    data-bind="journalControl: { value: value, multiple: multiple, options: options }, params: function() {
      return {
        <#if params.journalType??>journalType: '${params.journalType}',</#if>
        <#if params.searchBar??>searchBar: '${params.searchBar}',</#if>
        <#if params.defaultVisibleAttributes??>defaultVisibleAttributes: '${params.defaultVisibleAttributes}',</#if>
        <#if params.defaultSearchableAttributes??>defaultSearchableAttributes: '${params.defaultSearchableAttributes}',</#if>
        
        mode: '${mode?trim}',
        dock: ${dock?trim},
        hightlightSelection: ${hightlightSelection?trim},

        <#if params.sortBy??>sortBy: ${params.sortBy},</#if>

        <#if params.defaultHiddenByType??>defaultHiddenByType: '${params.defaultHiddenByType}',</#if>

        <#if params.searchMinQueryLength??>searchMinQueryLength: '${params.searchMinQueryLength}',</#if>
        <#if params.searchScript??>searchScript: '${params.searchScript}',</#if>
        <#if params.searchCriteria??>searchCriteria: ${params.searchCriteria},</#if>
        <#if params.defaultCriteria??>defaultCriteria: ${params.defaultCriteria},</#if>

        createVariantsVisibility: ${createVariantsVisibility},

        <#-- Create Object Transition -->
        createVariantsSource: '${createVariantsSource}',
        virtualParent: ${virtualParent}
      }
    }">

    <button id="${controlId}-button" 
            class="journal-control-button" 
            data-bind="disable: protected">${msg(params.buttonTitle!"button.select")}</button>
</div>