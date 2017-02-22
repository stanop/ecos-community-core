<#macro currency fieldName selectedItem ="workspace://SpacesStore/currency-rur">
	<!-- по умолчанию Рубль workspace://SpacesStore/currency-rur -->
	<#if form.mode == "create">
		<@forms.renderField field=fieldName extension = { "control": {
		"template": "/ru/citeck/components/form/controls/select.ftl",
		"params": {
		"optionsUrl": "/share/proxy/alfresco/citeck/search/simple?type=idocs:currency&amp;properties=idocs:currencyCode,idocs:currencyName",
		"resultsList":"nodes",
		"valueField":"nodeRef",
		"titleField":"title",
		"selectedItem":selectedItem,
		"style":"width:230px"
		}
		}} />
	<#else>
		<@forms.renderField field=fieldName extension = { "control": {
		"template": "/ru/citeck/components/form/controls/select.ftl",
		"params": {
		"optionsUrl": "/share/proxy/alfresco/citeck/search/simple?type=idocs:currency&amp;properties=idocs:currencyCode,idocs:currencyName",
		"resultsList":"nodes",
		"valueField":"nodeRef",
		"titleField":"title",
		"style":"width:230px"
		}
		}} />
	</#if>
</#macro>

<#macro association fieldName endpointType>
	<#if form.mode == "create">
		<@forms.renderField field=fieldName extension = {
			"endpointType":endpointType,
			"control": {
			"template": "/ru/citeck/components/form/controls/association_search.ftl",
			"params": {
			"flatButtonMode": "true",
			"searchWholeRepo": "true",
			"evaluateDLDestFolder": "true",
			"showTargetLink": "true"
			}
		}} />
	<#else>
		<@forms.renderField field=fieldName extension = {
			"control": {
			"template": "/ru/citeck/components/form/controls/association_search.ftl",
			"params": {
			"searchWholeRepo": "true",
			"showTargetLink": "true"
		}
		}} />
	</#if>
</#macro>
