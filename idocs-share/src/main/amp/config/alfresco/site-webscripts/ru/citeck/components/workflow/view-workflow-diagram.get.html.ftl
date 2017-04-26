<@markup id="css" >
    <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/citeck/components/workflow/view-workflow-diagram.css" group="workflow" />
</@>

<@markup id="js">
    <@script type="text/javascript" src="${page.url.context}/res/citeck/components/workflow/view-workflow-diagram.js" group="workflow" />
</@>

<#assign el=args.htmlid?js_string>

<script type="text/javascript">//<![CDATA[
    new Citeck.viewWorkflowDiagram("${el}").setOptions({
        enable: true
    }).setMessages(${messages});
//]]></script>

<@markup id="html">
    <@uniqueIdDiv>
        <#assign el=args.htmlid?html>
    <div id="${el}-body" ></div>
    <div> 
        <div id="${el}-section">
            <div class="task-buttons">
                <button id="${el}-viewWorkflowDiagram" class="hidden">${msg("button.viewWorkflowDiagram")}</button>
            </div>
        </div>
     </div>
    </@uniqueIdDiv>
</@markup>

