<#macro currency fieldName hasSelectedItem = false selectedItem ="workspace://SpacesStore/currency-rub">
	<!-- по умолчанию Рубль workspace://SpacesStore/currency-rub -->
	<!-- if form.mode = "create"-->
	<#if hasSelectedItem>
		<@forms.renderField field=fieldName extension = { "control": {
		"template": "/ru/citeck/components/form/controls/select.ftl",
		"params": {
		"optionsUrl": "/share/proxy/alfresco/citeck/search/simple?type=idocs:currency&amp;properties=idocs:currencyCode,idocs:currencyName",
		"resultsList":"nodes",
		"valueField":"nodeRef",
		"titleField":"name",
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
		"titleField":"name",
		"style":"width:230px"
		}
		}} />
	</#if>
</#macro>