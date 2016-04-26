<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
		"http://www.w3.org/TR/html4/loose.dtd">
<#escape x as x?html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <style>
        * {
            font-size: 16px;
        }
        h1 {
            font-size: 32px;
        }
		table {
			border-collapse: collapse;
		}
    </style>
</head>
<body>
<#setting number_format=",##0.00"/>
<#assign none = "(Нет)" />
<#assign dateFormat = "dd.MM.yyyy" />
<#assign dateTimeFormat = "dd.MM.yyyy HH:mm" />
<#macro signaturePlace>_____________________</#macro>
<#assign tableWidth = "700px" />
<#assign columnWidth = "300px" />
<#assign creator = people.getPerson(document.properties["cm:creator"]) />
<#assign currency = "руб." />

<#macro FIO user>
<#assign lastName = user.properties["org:lastName"]!user.properties["cm:lastName"]/>
<#assign firstName = user.properties["org:firstName"]!user.properties["cm:firstName"] />
<#assign middleName = user.properties["org:middleName"]! />
<#if firstName!="">${firstName?substring(0, 1)}. <#if middleName!="">${middleName?substring(0, 1)}. </#if></#if>${lastName}
</#macro>

	<#macro findCurrency>
		<#if document.associations?? && document.associations["contracts:closingDocumentCurrency"]?? && document.associations["contracts:closingDocumentCurrency"]?size != 0>
		    <#if document.associations["contracts:closingDocumentCurrency"][0].nodeRef=='workspace://SpacesStore/currency-eur'>
		        <#assign currency = "EUR" />
			<#elseif document.associations["contracts:closingDocumentCurrency"][0].nodeRef=='workspace://SpacesStore/currency-usd'>
				<#assign currency = "USD" />
			</#if>
		</#if>
		<#if document.associations?? && document.associations["contracts:closingDocumentAgreement"]?? && document.associations["contracts:closingDocumentAgreement"]?size != 0 && document.associations["contracts:closingDocumentAgreement"][0].associations["contracts:agreementCurrency"]?? && document.associations["contracts:closingDocumentAgreement"][0].associations["contracts:agreementCurrency"]?size != 0>
			<#if document.associations["contracts:closingDocumentAgreement"][0].associations["contracts:agreementCurrency"][0].nodeRef=='workspace://SpacesStore/currency-eur'>
				<#assign currency = "EUR" />
			<#elseif document.associations["contracts:closingDocumentAgreement"][0].associations["contracts:agreementCurrency"][0].nodeRef=='workspace://SpacesStore/currency-usd'>
				<#assign currency = "USD" />
			</#if>
		</#if>
	</#macro>

	<@findCurrency />

<p><b>Акт № ${document.properties["contracts:closingDocumentNumber"]!""} от <#if document.properties["contracts:closingDocumentDate"]??>${document.properties["contracts:closingDocumentDate"]?string('${dateFormat}')!}</#if></b><br/>
<hr />

Исполнитель: <#if document.associations?? && document.associations["idocs:legalEntity"]?? && document.associations["idocs:legalEntity"]?size != 0>${document.associations["idocs:legalEntity"][0].properties["idocs:fullOrganizationName"]!""}
<#elseif document.associations?? && document.associations["contracts:closingDocumentAgreement"]?? && document.associations["contracts:closingDocumentAgreement"]?size != 0 && document.associations["contracts:closingDocumentAgreement"][0].associations["contracts:agreementLegalEntity"]?? && document.associations["contracts:closingDocumentAgreement"][0].associations["contracts:agreementLegalEntity"]?size != 0>${document.associations["contracts:closingDocumentAgreement"][0].associations["contracts:agreementLegalEntity"][0].properties["cm:name"]!""}</#if><br/>
Заказчик: <#if document.associations?? && document.associations["contracts:contractor"]?? && document.associations["contracts:contractor"]?size != 0>${document.associations["contracts:contractor"][0].properties["idocs:fullOrganizationName"]!""}</#if><br/>
</p>

<table width="700px" border="1">
	<tr border="1">
		<td align="center" width="30px">№</td>
		<td align="center" width="300px">Наименование работ, услуг</td>
		<td align="center" width="40px">Кол-во</td>
		<td align="center" width="30px">Ед.</td>
		<td align="center" width="150px">Цена</td>
		<td width="150px" align="center">Сумма</td>
	</tr>
	<#assign count = 0/>
	<#assign totalAmount = 0/>
	<#if document.associations?? && document.associations["pas:containsProductsAndServices"]?? && document.associations["pas:containsProductsAndServices"]?size != 0>
		<#list document.associations["pas:containsProductsAndServices"] as containsProductsAndService>
			<#assign count = count + 1/>
			<tr>
				<td align="center">${count?string["0"]}</td>
				<td align="left">${containsProductsAndService.properties["cm:title"]!""}</td>
				<td align="right">${containsProductsAndService.properties["pas:quantity"]!""}</td>
				<td align="center">${containsProductsAndService.associations["pas:entityUnit"][0].properties["pas:unitShortName"]!""}</td>
				<td align="right">${containsProductsAndService.properties["pas:pricePerUnit"]!""}</td>
				<td align="right">${containsProductsAndService.properties["pas:total"]!""}</td>
			</tr>
			<#assign total = '${containsProductsAndService.properties["pas:total"]?string.computer!0}'/>
			<#assign totalAmount = totalAmount + total?number/>
		</#list>
	</#if>
</table>
<table width="700px" border="0">
			<tr>
				<td width="30px"></td>
				<td width="300px"></td>
				<td width="40px"></td>
				<td width="30px"></td>
				<td width="150px" align="right">Итого:</td>
				<td width="150px" align="right">${totalAmount}</td>
			</tr>
			<tr>
				<td></td>
				<td></td>
				<td></td>
				<td></td>
				<td align="right">Без налога (НДС)</td>
				<td align="right">-</td>
			</tr>
</table>
<p></p>
<p></p>

<table width="700px" border="0" cellspacing="0">
	<tr>
		<td>
            Всего наименований ${count?string["0"]}, на сумму ${totalAmount} ${currency}
		</td>
	</tr>
    <tr>
        <td>
			<#if document.properties["contracts:closingDocumentAmountInWords"]??>${document.properties["contracts:closingDocumentAmountInWords"]}</#if>
			<#if currency!="руб.">
                (НДС не облагается согласно части 2 НК РФ, глава 26.2, статья 346.12, статья 346.13)
			</#if>
        </td>
    </tr>
	<tr>
		<td>
            Выше перечисленные услуги выполнены полностью и в срок. Заказчик претензий по объему, качеству и срокам оказания услуг не имеет.
		</td>
	</tr>
</table>


<hr />
<table width="700px" border="0" cellspacing="0">
	<tr>
		<td width="350px">Исполнитель <@signaturePlace/></td>
		<td width="350px">Заказчик <@signaturePlace/></td>
	</tr>
</table>

</body>
</html>
</#escape>
