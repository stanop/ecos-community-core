<@markup id="css" >
    <@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/dynamic-tree/dynamic-table.css" group="document-children" />
    <@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/document-children/document-children.css" group="document-children" />
    <@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/dynamic-tree/action-renderer.css" group="document-children" />
</@>

<#if nodeRef??>

<#assign dcid=params.htmlid?js_string />

<#include "/org/alfresco/include/alfresco-macros.lib.ftl" />

<div id="${dcid}-panel" class="document-children document-details-panel">
	<h2 id="${dcid}-heading" class="thin dark">
		${msg("${params.header!}")}
		<span id="${dcid}-heading-actions" class="alfresco-twister-actions" style="position:relative;float:right;"></span>
	</h2>
	<div class="panel-body">
		<div id="${dcid}" class="document-view">
			<div id="${dcid}-view" class="document-view"></div>
		</div>
	</div>
	<script type="text/javascript">//<![CDATA[

    require(['citeck/utils/citeck'], function() {

        Citeck.utils.requireInOrder(['lib/grouped-datatable',
            'modules/documentlibrary/global-folder',
            'modules/documentlibrary/copy-move-to',
            'modules/documentlibrary/doclib-actions',
            'components/documentlibrary/actions'], function () {

            require(['citeck/components/dynamic-tree/error-manager',
                'citeck/components/dynamic-tree/hierarchy-model',
                'citeck/components/dynamic-tree/cell-formatters',
                'citeck/components/dynamic-tree/dynamic-table',
                'citeck/components/dynamic-tree/action-renderer',
                'citeck/components/document-children/document-children',
                'citeck/components/document-children/button-panel',
                'citeck/components/document-children/button-commands',
                'citeck/components/orgstruct/form-dialogs'], function() {

                <#if params.buttonsInHeader??>
                    (new Citeck.widget.ButtonPanel("${dcid}-heading-actions")).setOptions({
                        args: {
                            <#list params?keys as key>
                                    <#if params[key]??>"${key}": "${params[key]?js_string}"<#if key_has_next>,</#if></#if>
                            </#list>
                        }
                    }).setMessages(${messages});
                </#if>
                (new Citeck.widget.DocumentChildren("${dcid}")).setOptions({
                    nodeRef: "${nodeRef!}",
                    childrenUrl: ${childrenUrl},
                    columns: <@columns?interpret />,
                    responseSchema: ${responseSchema}
                    <#if responseType??>
                        , responseType: ${responseType}
                    </#if>
                    <#if hideEmpty??>
                        , hideEmpty: ${hideEmpty?string}
                    </#if>
                    <#if groupBy??>
                        , groupBy: "${groupBy?js_string}"
                    </#if>
                    <#if groupTitle??>
                        , groupTitle: "${groupTitle?js_string}"
                    </#if>
                    <#if childrenFormat??>
                        , childrenFormat: "${childrenFormat?js_string}"
                    </#if>
                }).setMessages(${messages});
                <#if (args.hideTwister!'false') == 'false'>
                    Alfresco.util.createTwister("${dcid}-heading", "${args.twisterKey!'dc'}");
                </#if>
            });
		})
	});
	//]]></script>
</div>
</#if>
