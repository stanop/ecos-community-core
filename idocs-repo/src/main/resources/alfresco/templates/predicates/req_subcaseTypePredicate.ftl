<#assign requiredSubcaseType = predicate.properties['req:requiredSubcaseType']! />
<#assign requiredElementType = predicate.properties['req:requiredElementType']! />
(function(){
    <#if requiredSubcaseType?has_content>
    if (!document.isSubType("${shortQName(requiredSubcaseType)}")) return false;
    </#if>
    var elements = document.assocs['icase:subcaseElement'] || []; 
    if (elements.length == 0) return false;
    var element = elements[0];
    <#if requiredElementType?has_content>
    if (!element.isSubType("${shortQName(requiredElementType)}")) return false;
    </#if>
    return true;
})()