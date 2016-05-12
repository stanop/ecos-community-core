<#if field.control.params.rows??><#assign rows=field.control.params.rows><#else><#assign rows=3></#if>
<#if field.control.params.columns??><#assign columns=field.control.params.columns><#else><#assign columns=60></#if>
<#assign step=20/>

<#-- ========================================== -->
<#--       set document name to field value     -->
<#-- ========================================== -->
<script type="text/javascript">//<![CDATA[
YAHOO.Bubbling.on("mandatoryControlValueUpdated", function (layer, args) {
    var control = args[1];
    // react only on packageItems
    if (!control.id.match("assoc_packageItems")) {
        return;
    }
    var fieldId = control.id.replace(/^(.*assoc_packageItems).*$/, "$1");
    var nodeRef = Dom.get(fieldId).value;
    // send only if a document is selected
    if (!nodeRef) {
        return;
    } else {
        var fieldDescriptionId = fieldId.replace("assoc_packageItems", "prop_bpm_workflowDescription");
        var descr = Dom.get(fieldDescriptionId);
        var searchUrl = Alfresco.constants.PROXY_URI + "citeck/node?nodeRef=" + nodeRef;
        var request = new XMLHttpRequest();
        request.open('GET', searchUrl, false);  // `false` makes the request synchronous
        request.send(null);
        if (request.status === 200) {
            if (request.responseText) {
                var data = eval('(' + request.responseText + ')');
                var name = data.props['cm:name'];
            }
            descr.value = name;
        }
    }

});
//]]>
function textAreaAdjust(o) {
    o.style.height = "1px";
    o.style.height = (${step}+o.scrollHeight) + "px";
}
</script>
<div class="form-field">
<#if form.mode == "view">
    <div class="viewmode-field">
        <#if field.mandatory && field.value == "">
        <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png"
                                              title="${msg("form.field.incomplete")}"/><span>
        </#if>
        <span class="viewmode-label">${field.label?html}:</span>
        <#if field.control.params.activateLinks?? && field.control.params.activateLinks == "true">
            <#assign fieldValue=field.value?html?replace("((http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?\\^=%&:\\/~\\+#]*[\\w\\-\\@?\\^=%&\\/~\\+#])?)", "<a href=\"$1\" target=\"_blank\">$1</a>", "r")>
        <#else>
            <#assign fieldValue=field.value?html>
        </#if>
        <span class="viewmode-value"><#if fieldValue == "">${msg("form.control.novalue")}<#else>${fieldValue}</#if></span>
    </div>
<#else>
    <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span
            class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
    <textarea id="${fieldHtmlId}" name="${field.name}" cols="${columns}" tabindex="0" onload="textAreaAdjust(this)"
              onkeyup="textAreaAdjust(this)" style="overflow:hidden"
              <#if field.description??>title="${field.description}"</#if>
              <#if field.control.params.styleClass??>class="${field.control.params.styleClass}"</#if>
              <#if field.control.params.style??>style="${field.control.params.style}"</#if>
              <#if field.disabled && !(field.control.params.forceEditable?? && field.control.params.forceEditable == "true")>disabled="true"</#if>>${field.value?html}</textarea>
    <@formLib.renderFieldHelp field=field />
</#if>
</div>