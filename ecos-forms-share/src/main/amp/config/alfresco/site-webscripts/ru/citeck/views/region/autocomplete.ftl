<#assign controlId = fieldId + "-autocompleteControl">
<#assign controlParams = viewScope.region.params>

<#assign helpMessage  = msg("autocomplete.help-message")>
<#assign emptyMessage = msg("autocomplete.empty-message")>
<#assign labelMessage = msg("autocomplete.label-message")>

<div id="${controlId}" class="autocomplete-control"
    data-bind='component: { name: "autocomplete",
        params: {
            <#if controlParams.minQueryLength??>
                minQueryLength: "${controlParams.minQueryLength}",
            </#if>
            <#if controlParams.searchScript??>
                searchScript: "${controlParams.searchScript}",
            </#if>
            <#if controlParams.criteria??>
                criteria: ${controlParams.criteria},
            <#elseif controlParams.attribute?? && controlParams.predicate??>
                criteria: [{
                    attribute: "${controlParams.attribute}",
                    predicate: "${controlParams.predicate}"
                }],
            </#if>

            protected: protected,
            helpMessage: "${helpMessage}",
            labelMessage: "${labelMessage}",

            value: singleValue,
            data: $data  
        }
    }'>
</div>

<#-- TODO:
    - remove deprecated params attribute and predicate
 -->