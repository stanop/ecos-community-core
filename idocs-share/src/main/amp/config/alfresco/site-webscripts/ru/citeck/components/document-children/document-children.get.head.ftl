<#include "/org/alfresco/components/component.head.inc">

<@script type="text/javascript" src="${url.context}/res/lib/grouped-datatable.js"></@script>

<@script type="text/javascript" src="${url.context}/res/modules/documentlibrary/global-folder.js"></@script>
<@script type="text/javascript" src="${url.context}/res/modules/documentlibrary/copy-move-to.js"></@script>
<@script type="text/javascript" src="${url.context}/res/modules/documentlibrary/doclib-actions.js"></@script>
<@script type="text/javascript" src="${url.context}/res/components/documentlibrary/actions.js"></@script>
<@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/error-manager.js"></@script>
<@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/hierarchy-model.js"></@script>
<@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/cell-formatters.js"></@script>
<@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/dynamic-tree/cell-formatters.css"></@link>
<@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/dynamic-table.js"></@script>
<@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/dynamic-tree/dynamic-table.css" />
<@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/action-renderer.js"></@script>
<@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/dynamic-tree/action-renderer.css" />

<@script type="text/javascript" src="${url.context}/res/citeck/components/document-children/document-children.js"></@script>
<@script type="text/javascript" src="${url.context}/res/citeck/components/document-children/button-panel.js"></@script>
<@script type="text/javascript" src="${url.context}/res/citeck/components/document-children/button-commands.js"></@script>
<@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/document-children/document-children.css" />

<#-- dependencies for create button -->
<@script type="text/javascript" src="${url.context}/res/citeck/components/orgstruct/form-dialogs.js"></@script>
<#include "/org/alfresco/components/form/form.dependencies.inc" />
