<@markup id="css" >
    <@link rel="stylesheet" type="text/css" href="${url.context}/res/citeck/components/templates/card-templates.css" group="templates" />
</@>

<#assign el=args.htmlid?js_string>
<#include "/org/alfresco/include/alfresco-macros.lib.ftl" />
<#if nodeRefExists??>
    <div id="${el}-body" class="document-details-panel">

        <h2 id="${el}-heading" class="thin dark">
            ${msg("title")}
        </h2>

        <div class="panel-body">
            <#if templates?? && templates?size &gt; 0>
                <div>
                    <#list templates as template>
                        <#assign urlTemplate=url.context + "/proxy/alfresco/citeck/print/metadata-printpdf?nodeRef=" + nodeRef + "&templateType=" + template.type +"&print=true" />
                        <div class="row">
                            <span>${template.typeTitle}</span>
                            <div style="float: right">
                                <span><a href="${urlTemplate}&format=html"><img src="${url.context}/res/citeck/components/templates/images/document-16.png"></a></span>
                                <span><a href="${urlTemplate}&format=docx"><img src="${url.context}/res/citeck/components/templates/images/word-16.png"></a></span>
                                <span><a href="${urlTemplate}&format=pdf"><img src="${url.context}/res/citeck/components/templates/images/pdf-16.png"></a></span>
                            </div>
                        </div>
                    </#list>
                </div>
            <#else>
                <div>
                    ${msg("no-templates")}
                </div>
            </#if>

        </div>

        <script type="text/javascript">//<![CDATA[
        Alfresco.util.createTwister("${el}-heading", "cardTemplates");
        //]]></script>

    </div>
</#if>