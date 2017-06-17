<@markup id="css" >
    <@link rel="stylesheet" type="text/css" href="${page.url.context}/res/citeck/components/workflow/task-history.css" group="workflow" />
</@>

<@markup id="js">
    <@script type="text/javascript" src="${page.url.context}/res/citeck/components/workflow/task-history.js" group="workflow" />
</@>

<#assign el=args.htmlid?js_string>

<script type="text/javascript">//<![CDATA[
    new Citeck.HistoryTasks("${el}").setOptions({
        enable: true
    }).setMessages(${messages});
//]]></script>


<div id="${el}-tasksHistory" class="form-container hidden">
    <div class="form-fields">
        <div class="set">
            <div class="task-history-title">${msg("header.history")}</div>
        </div>
        <div id="${el}-tasksHistory-form-section" class="task-history">
            <div class="form-field">
                <div class="form-element-background-color"></div>
            </div>
        </div>
    </div>
    <br/>
</div>