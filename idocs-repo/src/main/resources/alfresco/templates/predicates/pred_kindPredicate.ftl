<#if predicate.properties['pred:requiredKind']??>
document.properties['tk:kind'] != null && document.properties['tk:kind'].nodeRef == "${predicate.properties['pred:requiredKind'].nodeRef}"
<#else>
document.properties['tk:type'] != null && document.properties['tk:type'].nodeRef == "${predicate.properties['pred:requiredType'].nodeRef}"
</#if>