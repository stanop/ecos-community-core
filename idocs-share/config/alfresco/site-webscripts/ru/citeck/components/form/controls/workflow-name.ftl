<script type="text/javascript">//<![CDATA[
YAHOO.Bubbling.on("taskDetailedData", function(layer, args) {
	var task = args[1];
	Dom.get("${fieldHtmlId}").innerHTML = task.workflowInstance.description;
});
//]]></script>

<div class="form-field">
    <div class="viewmode-field">
		<span class="viewmode-label">${field.label?html}:</span>
		<span id="${fieldHtmlId}" class="viewmode-value"></span>
    </div>
</div>
