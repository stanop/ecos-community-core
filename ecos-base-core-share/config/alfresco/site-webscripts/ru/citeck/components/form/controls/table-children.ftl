<#--
    This control represents node children of specified association type.
    Required parameters:
        field.control.params.columns - this string represents column definition in the format of
                                        YAHOO.widget.DataTable see: http://yui.github.io/yui2/docs/yui_2.9.0_full/datatable/index.html#instantiating
                                        For example: [{key: "nodeRef", label: "Id", resizeable: true}, {key: "displayName", label: "Name", resizeable: true}]
        field.control.params.responseSchema - this string represents column definition in the format of
                                        YAHOO.util.LocalDataSource see: http://yui.github.io/yui2/docs/yui_2.9.0_full/datasource/index.html#instantiating
                                        For example: {resultsList: "items", fields: [{key: "nodeRef"}, {key: "displayName"}]}
        field.control.params.destNode - temporary destination folder, which will be used for created items
        field.control.params.errors - a map of error descriptions
        field.control.params.childrenUrl - it is non mandatory parameter, it is a URL which returns info about displayed nodes in JSON format
        field.control.params.rootNode - root node, it is non mandatory parameter, it only used when the specified parameter 'childrenUrl' contains
                                        specific parameters like {nodeRefForURL}, which is used for substituting parameters in the URL
        field.control.params.showAddButton - if it is 'true', form shows add button.
-->

<@script type="text/javascript" src="${url.context}/res/citeck/components/orgstruct/form-dialogs.js"></@script>
<@script type="text/javascript" src="${url.context}/res/citeck/components/dynamic-tree/cell-formatters.js"></@script>
<@script type="text/javascript" src="${url.context}/res/citeck/components/document-children/button-commands.js"></@script>
<@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/dynamic-tree/action-renderer.css" />

<#assign controlId = fieldHtmlId + "-tc-cntrl">
<#assign childrenUrl = field.control.params.childrenUrl!"">
<#assign is_property = field.type == "property" />

<script type="text/javascript">//<![CDATA[
(function() {

    var table = new Citeck.widget.DynamicTableControl("${controlId}", "${fieldHtmlId}").setOptions({
        model: {
            formats: {
                "item": {
                    name: "{nodeRef}",
                    keys: [ "selected-{selected}", "item",  ],
                },
                "selected-items": {
                    name: "selected-items",
                    keys: [ "selected-items" ]
                }
            },
            item: {
                "": {
                    "format": "item",
                    "get": "${url.context}/proxy/alfresco/citeck/node?nodeRef={nodeRef}&replaceColon=_&includeNodeRef=nodeRef",
                    "resultsList": "props"
                }
            },
            children: {
                "selected-items": {
                    "format": "item",
                },
                "root": {
                    "format": "item",
                    "get": "${childrenUrl}",
                    "resultsList": "props"
                },
            },
            <#if field.control.params.errors??>
            errors: ${field.control.params.errors},
            </#if>
        },
        columns: ${field.control.params.columns},
        <#if field.control.params.selection??>
            selection: "${field.control.params.selection}",
        </#if>
        <#if field.control.params.beforeRender??>
            beforeRender: function (event) {
                var dataTable = this;
                return ${field.control.params.beforeRender};
            },
        </#if>
        <#if field.control.params.preview??>
            preview: ${field.control.params.preview},
        </#if>
        <#if field.control.params.previewByClickOnCell??>
            previewByClickOnCell: "${field.control.params.previewByClickOnCell}",
        </#if>
        responseSchema: ${field.control.params.responseSchema},
        field: "${field.name?js_string}",
        forms: {
            nodeId: "nodeRef"
        },
        <#if field.control.params.rootNode??>
            rootNode: ${field.control.params.rootNode},
        </#if>
        <#if field.control.params.errors??>
            errors: ${field.control.params.errors},
        </#if>
        <#if field.control.params.destNode??>
            destFolder: "${field.control.params.destNode}",
        </#if>
        itemId: "${field.endpointType}",
        <#if field.control.params.assocType??>
            assocType: "${field.control.params.assocType}",
        </#if>
        btnAddTitle: "${msg("form.button.add.element.title")}",
        <#if (field.control.params.showAddButton!'false') == "true" >
        allowSelectAction: true,
        <#else>
        allowSelectAction: false,
        </#if>
        <#if field.control.params.showCancelButton??>
            showCancelButton: "${field.control.params.showCancelButton}",
        </#if>
        <#if (field.control.params.showSearchButton!'false') == "true" >
        allowSearchAction: true,
        search: {
            itemType: "${field.control.params.itemType!'item'}",
            itemKey: "${field.control.params.itemKey!'id'}",
            itemTitle: "${field.control.params.itemTitle!'{name}'}",
            itemURL: "${field.control.params.itemURL!}",
            itemURLresults: "${field.control.params.itemURLresults!}",
            searchURL: "${field.control.params.searchURL!}",
            searchURLresults: "${field.control.params.searchURLresults!}",
            rootURL: "${field.control.params.rootURL!}",
            rootURLresults: "${field.control.params.rootURLresults!}",
            btnSearchTitle: "${field.control.params.btnSearchTitle!}"
        }
        <#else>
        allowSearchAction: false,
        </#if>
    }).setMessages(${messages});

})();
//]]></script>

<#if field.value?is_number>
    <#assign fieldValue=field.value?c>
<#else>
    <#assign fieldValue=field.value?html>
</#if>

<div class="form-field">
    <#if form.mode == "view">
        <span class="viewmode-label">${field.label?html}:</span>
    <#else>
        <label for="${controlId}">${field.label?html}:<#if field.endpointMandatory!false || field.mandatory!false><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
    </#if>

    <div id="${controlId}" class="table-children">
        <div id="${controlId}-itemGroupActions" class="show-picker"></div>

        <div id="${controlId}-view" class="table-children-view hide-buttons"></div>
        <input type="hidden" id="${fieldHtmlId}" name="<#if is_property>${field.name}<#else>-</#if>" value="${fieldValue}" />
        <input type="hidden" id="${controlId}-added" name="<#if !is_property>${field.name}_added<#else>-</#if>" />
        <input type="hidden" id="${controlId}-removed" name="<#if !is_property>${field.name}_removed<#else>-</#if>" />
    </div>
</div>
