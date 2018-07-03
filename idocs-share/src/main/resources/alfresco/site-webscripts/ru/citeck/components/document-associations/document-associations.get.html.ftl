<@markup id="css" >
    <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/citeck/components/document-associations/document-associations.css" group="document-associations"/>
</@>

<@markup id="js">
    <@script type="text/javascript" src="${page.url.context}/res/citeck/components/document-associations/document-associations.js" group="document-associations" />
</@>

<#if nodeRef?? && assocs??>
    <#assign el=args.htmlid?js_string>

    <div id="${el}-control-container" >
        <div id="${el}-control" data-bind="journalControl: { value: value, multiple: multiple }, params: function() {
           return {
                journalType: journalId,
                mode: 'collapse',
                hightlightSelection: true,
                removeSelection: true,
                createVariantsSource: 'journal-create-variants',
                <#if createVariantsVisibility??>createVariantsVisibility: ${createVariantsVisibility?string}</#if>
            }}">
        </div>
    </div>
    <div id="${el}-control-button" ></div>

    <div id="${el}-panel" class="document-associations document-details-panel">
        <h2 id="${el}-heading" class="thin dark">
            ${msg("header.assocs")}
            <span id="${el}-heading-actions" class="alfresco-twister-actions">
                <a id="${el}-create-button">&nbsp;</a>
            </span>
        </h2>
        <div id="${el}-body" class="panel-body">
            <div id="${el}-message"></div>
        </div>
    </div>

    <#macro renderAssocList list>
        [
            <#list list as item>
            {
                "name": "${item.name?js_string}",
                "direction": "${item.direction?js_string}",
                "directed": ${item.directed?string}
            }<#if item_has_next>,</#if>
            </#list>
        ]
    </#macro>

    <#macro renderCells cells>
        [ <#list cells as cell>"${cell}"<#if cell_has_next>,</#if></#list> ]
    </#macro>

    <script type="text/javascript">//<![CDATA[
    require(['citeck/components/dynamic-tree/cell-formatters'], function() {
        YAHOO.util.Event.onContentReady("${el}", function() {
            var component = new Citeck.widget.DocumentAssociations("${el}").setOptions({
                nodeRef: "${nodeRef?js_string}",

                    <#if columns??>columns: ${columns},</#if>
                    <#if isMultiple??>isMultiple: ${isMultiple?string},</#if>
                visible: <@renderAssocList assocs.visible />,
                addable: <@renderAssocList assocs.addable />,
                removeable: <@renderAssocList assocs.removeable />,
                dependencies: ${jsonUtils.toJSONString(dependencies)}
            }).setMessages(${messages});

            Alfresco.util.createTwister("${el}-heading", "Citeck.widget.DocumentAssociations", { panel: "${el}-body" });
        });
    });
    //]]></script>

</#if>
