<#include "/ru/citeck/components/journals2/journals-picker.ftl" />

<#assign pickerId = fieldHtmlId + "-picker" />

<#if field.control.params.endpointType??>
	<#assign endpointType = field.control.params.endpointType />
<#elseif field.endpointType??>
	<#assign endpointType = field.endpointType />
<#else>
	<#stop "endpointType must be specified for journals-select.ftl" />
</#if>

<script type="text/javascript">//<![CDATA[
(function() {
	var picker = <@renderJournalsPickerJS pickerId endpointType />;
})();
//]]></script>

<@renderJournalsPickerHTML pickerId />
