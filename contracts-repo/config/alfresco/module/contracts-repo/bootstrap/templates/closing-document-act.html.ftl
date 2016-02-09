<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<#escape x as x?html>
<html>
<head>
<style>
*
{
	font-size: 16px;
}
h1
{
	font-size: 32px;
}
</style>
</head>
<body>
<#assign none = "(Нет)" />
<#assign dateFormat = "dd.MM.yyyy" />
<#assign dateTimeFormat = "dd.MM.yyyy HH:mm" />
<#macro signaturePlace>_____________________</#macro>
<#assign tableWidth = "700px" />
<#assign columnWidth = "300px" />
<#assign creator = people.getPerson(document.properties["cm:creator"]) />

<#macro FIO user>
<#assign lastName = user.properties["org:lastName"]!user.properties["cm:lastName"]/>
<#assign firstName = user.properties["org:firstName"]!user.properties["cm:firstName"] />
<#assign middleName = user.properties["org:middleName"]! />
<#if firstName!="">${firstName?substring(0, 1)}. <#if middleName!="">${middleName?substring(0, 1)}. </#if></#if>${lastName}
</#macro>

<p><b>Акт № ${document.properties["contracts:closingDocumentNumber"]!""} от <#if document.properties["contracts:closingDocumentDate"]??>${document.properties["contracts:closingDocumentDate"]?string('${dateFormat}')!}</#if></b><br/>
<hr />

Исполнитель: <#if document.associations?? && document.associations["idocs:legalEntity"]?? && document.associations["idocs:legalEntity"]?size != 0>${document.associations["idocs:legalEntity"][0].properties["cm:name"]!""}
<#elseif document.associations?? && document.associations["contracts:closingDocumentAgreement"]?? && document.associations["contracts:closingDocumentAgreement"]?size != 0 && document.associations["contracts:closingDocumentAgreement"][0].associations["contracts:agreementLegalEntity"]?? && document.associations["contracts:closingDocumentAgreement"][0].associations["contracts:agreementLegalEntity"]?size != 0>${document.associations["contracts:closingDocumentAgreement"][0].associations["contracts:agreementLegalEntity"][0].properties["cm:name"]!""}</#if><br/>
Заказчик: <#if document.associations?? && document.associations["contracts:contractor"]?? && document.associations["contracts:contractor"]?size != 0>${document.associations["contracts:contractor"][0].properties["cm:name"]!""}</#if><br/>
</p>

<table width="700px" border="1">
	<tr border="1">
		<td align="center" width="30px">№</td>
		<td align="center" width="300px">Наименование работ, услуг</td>
		<td align="center" width="40px">Кол-во</td>
		<td align="center" width="30px">Ед.</td>
		<td align="center" width="150px">Цена</td>
		<td width="150px">Сумма</td>
	</tr>
	<#assign count = 0/>
	<#assign totalAmount = 0/>
	<#if document.associations?? && document.associations["pas:containsProductsAndServices"]?? && document.associations["pas:containsProductsAndServices"]?size != 0>
		<#list document.associations["pas:containsProductsAndServices"] as containsProductsAndService>
			<#assign count = count + 1/>
			<tr>
				<td>${count}</td>
				<td>${containsProductsAndService.properties["cm:title"]!""}</td>
				<td>${containsProductsAndService.properties["pas:quantity"]!""}</td>
				<td>${containsProductsAndService.associations["pas:entityUnit"].properties["pas:unitShortName"]!""}</td>
				<td>${containsProductsAndService.properties["pas:pricePerUnit"]!""}</td>
				<td>${containsProductsAndService.properties["pas:total"]!""}</td>
			</tr>
			<#assign total = '${containsProductsAndService.properties["pas:total"]!0}'/>
			<#assign totalAmount = totalAmount + total/>
		</#list>
	</#if>
</table>
<table width="700px" border="0">
			<tr>
				<td width="30px"></td>
				<td width="300px"></td>
				<td width="40px"></td>
				<td width="30px"></td>
				<td width="150px">Итого:</td>
				<td width="150px">${totalAmount}</td>
			</tr>
			<tr>
				<td></td>
				<td></td>
				<td></td>
				<td></td>
				<td>Без налога (НДС)</td>
				<td>-</td>
			</tr>
</table>
<p>Всего оказано услуг ${count}, на сумму ${totalAmount} <#if document.associations?? && document.associations["contracts:closingDocumentAgreement"]?? && document.associations["contracts:closingDocumentAgreement"]?size != 0 && document.associations["contracts:closingDocumentAgreement"][0].associations["contracts:agreementCurrency"]?? && document.associations["contracts:closingDocumentAgreement"][0].associations["contracts:agreementCurrency"]?size != 0 && document.associations["contracts:closingDocumentAgreement"][0].associations["contracts:agreementCurrency"][0].nodeRef=='workspace://SpacesStore/currency-rur'>руб.<#elseif document.associations?? && document.associations["contracts:closingDocumentAgreement"]?? && document.associations["contracts:closingDocumentAgreement"]?size != 0 && document.associations["contracts:closingDocumentAgreement"][0].associations["contracts:agreementCurrency"]?? && document.associations["contracts:closingDocumentAgreement"][0].associations["contracts:agreementCurrency"]?size != 0 && document.associations["contracts:closingDocumentAgreement"][0].associations["contracts:agreementCurrency"][0].nodeRef=='workspace://SpacesStore/currency-usd'>USD</#if><br/>
Выше перечисленные услуги выполнены полностью и в срок. Заказчик претензий по объему, качеству и срокам оказания услуг не имеет.</p>
<hr />
<table width="700px" border="0" cellspacing="0">
	<tr border="0">
		<td width="350px" border="0">Исполнитель <@signaturePlace/></td>
		<td width="350px" border="0">Заказчик <@signaturePlace/></td>
	</tr>
</table>

</body>
</html>
</#escape>
