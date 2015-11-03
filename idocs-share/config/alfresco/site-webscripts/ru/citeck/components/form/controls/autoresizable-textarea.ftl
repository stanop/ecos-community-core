
<#if form.mode == 'view'>
<div class="form-field">
   <div class="viewmode-field">
      <#if field.mandatory && field.value == "">
      <span class="incomplete-warning"><img src="${url.context}/res/components/form/images/warning-16.png" title="${msg("form.field.incomplete")}" /><span>
      </#if>
      <span class="viewmode-label">${field.label?html}:</span>
      <#if field.control.params.activateLinks?? && field.control.params.activateLinks == "true">
         <#assign fieldValue=field.value?html?replace("((http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?\\^=%&:\\/~\\+#]*[\\w\\-\\@?\\^=%&\\/~\\+#])?)", "<a href=\"$1\" target=\"_blank\">$1</a>", "r")>
      <#else>
         <#assign fieldValue=field.value?html>
      </#if>
      <span class="viewmode-value"><#if fieldValue == "">${msg("form.control.novalue")}<#else>${fieldValue}</#if></span>
   </div>
</div>
<#else>
<#assign step=20/>
<#assign maxHeight=field.control.params.maxHeight!"0"/>
<div class="form-field">
    <label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
    <textarea id="${fieldHtmlId}" name="${field.name}" onload="textAreaAdjust(this)" onkeyup="textAreaAdjust(this)" style=<#if field.control.params.style??>"${field.control.params.style}"</#if><#if field.control.params.showScroll?? && field.control.params.showScroll=="true">"overflow-y: auto"<#else>"overflow:hidden"</#if> >${(field.value!)?html}</textarea>
</div>
<script>
    function textAreaAdjust(o) {
        o.style.height = "1px";
        var newHeight=${step}+o.scrollHeight;
        var maxHeight = ${maxHeight};
        if (maxHeight!=0 && newHeight>maxHeight) {
            o.style.height = maxHeight+"px";
        } else {
            o.style.height = newHeight+"px";
        }
        //test;
    }
	(function() {
		var id = "${fieldHtmlId}";
		YAHOO.util.Event.onAvailable(id, function() {
			textAreaAdjust(Dom.get(id));
		});
	})();
</script>
</#if>