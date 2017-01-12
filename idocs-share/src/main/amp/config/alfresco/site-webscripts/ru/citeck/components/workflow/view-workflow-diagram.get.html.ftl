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

