<#include "/org/alfresco/components/component.head.inc">
<#-- 
    Organization Structure Admin Console Component
-->
<@script type="text/javascript" src="${page.url.context}/res/components/console/consoletool.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/yui/resize/resize.js"></@script>

<!-- People Finder Assets -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/people-finder/people-finder.css" />
<@script type="text/javascript" src="${page.url.context}/res/components/people-finder/people-finder.js"></@script>

<!-- Group Finder Assets -->
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/people-finder/group-finder.css" />
<@script type="text/javascript" src="${page.url.context}/res/components/people-finder/group-finder.js"></@script>

<!-- Forms Runtime -->
<#include "/org/alfresco/components/form/form.get.head.ftl" />
<#attempt>
    <#include "/org/alfresco/components/form/form.dependencies.inc" />
<#recover>
</#attempt>
<@script type="text/javascript" src="${page.url.context}/res/modules/simple-dialog.js"></@script>

<@script type="text/javascript" src="${page.url.context}/res/citeck/components/dynamic-tree/dynamic-toolbar.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/citeck/components/orgstruct/picker-dialogs.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/citeck/components/orgstruct/form-dialogs.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/citeck/components/orgstruct/console.js"></@script>
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/citeck/components/orgstruct/console.css" />
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/citeck/components/deputy/deputy.css" />