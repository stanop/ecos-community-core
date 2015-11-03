<#include "/org/alfresco/include/alfresco-template.ftl" />
<@templateHeader>
<!-- File-Upload -->
    <@script type="text/javascript" src="${page.url.context}/res/components/upload/file-upload.js"></@script>
    <@script type="text/javascript" src="${page.url.context}/res/components/upload/html-upload.js"></@script>
    <@script type="text/javascript" src="${page.url.context}/res/components/upload/flash-upload.js"></@script>
    <@script type="text/javascript" src="${url.context}/res/components/documentlibrary/actions.js"></@script>
    <@script type="text/javascript" src="${url.context}/res/components/documentlibrary/actions-util.js"></@script>
    <@script type="text/javascript" src="${url.context}/res/modules/documentlibrary/doclib-actions.js"></@script>
    <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/components/document-details/document-details-panel.css" />
    <@templateHtmlEditorAssets />
</@>

<@templateBody>
<div id="alf-hd">
    <#include "/ru/citeck/include/header.ftl" />
    <#if page.url.args.nodeRef??>
    <@region id="path" scope="template"/>
	</#if>
</div>
<div id="bd">
    <div class="share-form">
        <@region id="data-header" scope="page" />
        <@region id="data-form" scope="page" />
        <@region id="data-actions" scope="page" />
    </div>
    <#if page.url.args.nodeRef??>
        <h1 style="padding: 15px; margin-top: 1em; border-top: 1px solid;">${msg("title.documentInfo")}</h1>
        <@region id="actions-common" scope="page"/>
        <@region id="actions" scope="page"/>
        <div class="yui-gc">
            <div class="yui-u first">
                <#if (config.scoped['DocumentDetails']['document-details'].getChildValue('display-web-preview') == "true")>
                        <@region id="web-preview" scope="page"/>
                    </#if>
            </div>
            <div class="yui-u">
                <@region id="node-actions" scope="page"/>
                <@region id="document-metadata" scope="page"/>
            </div>
        </div>
    </#if>
    <@region id="html-upload" scope="template"/>
    <@region id="flash-upload" scope="template"/>
    <@region id="file-upload" scope="template" name="file-upload-id"/>
    <@region id="dnd-upload" scope="template"/>
</div>

</@>

<@templateFooter>
<div id="alf-ft">
    <@region id="footer" scope="global"/>
    <@region id="data-loader" scope="page" />
</div>
</@>
