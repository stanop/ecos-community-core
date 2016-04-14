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

<#macro FIO user>
<#assign lastName = user.properties["org:lastName"]!user.properties["cm:lastName"]/>
<#assign firstName = user.properties["org:firstName"]!user.properties["cm:firstName"] />
<#assign middleName = user.properties["org:middleName"]! />
<#if firstName!="">${firstName?substring(0, 1)}. <#if middleName!="">${middleName?substring(0, 1)}. </#if></#if>${lastName}
</#macro>


<p><b><#if document.associations?? && document.associations["payments:beneficiary"]?? && document.associations["payments:beneficiary"]?size != 0>${document.associations["payments:beneficiary"][0].properties["idocs:fullOrganizationName"]!""}</#if><br/>
Адрес: <#if document.associations?? && document.associations["payments:beneficiary"]?? && document.associations["payments:beneficiary"]?size != 0>${document.associations["payments:beneficiary"][0].properties["idocs:juridicalAddress"]!document.associations["payments:beneficiary"][0].properties["idocs:postAddress"]!}</#if></b></p>

<table width="700px" border="1">

    <tr>
        <td nowrap colspan="2" width="245px">ИНН <#if document.associations?? && document.associations["payments:beneficiary"]?? && document.associations["payments:beneficiary"]?size != 0>${document.associations["payments:beneficiary"][0].properties["idocs:inn"]!""}&nbsp;</#if></td>
        <td nowrap colspan="2" width="230px">КПП <#if document.associations?? && document.associations["payments:beneficiary"]?? && document.associations["payments:beneficiary"]?size != 0>${document.associations["payments:beneficiary"][0].properties["idocs:kpp"]!""}&nbsp;</#if></td>
        <td rowspan="2" width="50px"><#if document.associations?? && document.associations["payments:currency"]?? && document.associations["payments:currency"]?size != 0 && document.associations["payments:currency"][0].nodeRef=='workspace://SpacesStore/currency-usd'>Кор. Сч. №</#if></td>
        <td rowspan="2" width="175px">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="4">Получатель</td>
    </tr>
    <tr>
        <td colspan="4"><#if document.associations?? && document.associations["payments:beneficiary"]?? && document.associations["payments:beneficiary"]?size != 0>${document.associations["payments:beneficiary"][0].properties["idocs:shortOrganizationName"]!""}</#if></td>
        <td align="center"><p>Сч. №</p></td>
        <td><#if document.associations?? && document.associations["payments:beneficiary"][0].sourceAssociations["idocs:legalEntity"]?? && document.associations["payments:beneficiary"][0].sourceAssociations["idocs:legalEntity"]?size != 0>${document.associations["payments:beneficiary"][0].sourceAssociations["idocs:legalEntity"][0].properties["idocs:accountNumber"]!""}</#if></td>
    </tr>
    <tr>
        <td colspan="4">Банк получателя</td>
        <td align="center"><p>БИК</p></td>
        <td><#if document.associations?? && document.associations["payments:beneficiary"][0].sourceAssociations["idocs:legalEntity"]?? && document.associations["payments:beneficiary"][0].sourceAssociations["idocs:legalEntity"]?size != 0>${document.associations["payments:beneficiary"][0].sourceAssociations["idocs:legalEntity"][0].properties["idocs:bankId"]!""}</#if></td>
    </tr>
    <tr>
        <td colspan="4"><#if document.associations?? && document.associations["payments:beneficiary"][0].sourceAssociations["idocs:legalEntity"]?? && document.associations["payments:beneficiary"][0].sourceAssociations["idocs:legalEntity"]?size != 0>${document.associations["payments:beneficiary"][0].sourceAssociations["idocs:legalEntity"][0].properties["idocs:bankTitle"]!""}</#if></td>
        <td rowspan="2" align="center"><p>Сч. №</p></td>
        <td><#if document.associations?? && document.associations["payments:beneficiary"][0].sourceAssociations["idocs:legalEntity"]?? && document.associations["payments:beneficiary"][0].sourceAssociations["idocs:legalEntity"]?size != 0>${document.associations["payments:beneficiary"][0].sourceAssociations["idocs:legalEntity"][0].properties["idocs:corresponentAccountNumber"]!""}</#if></td>
    </tr>
</table>
<br/>
<p align="center"><b>СЧЕТ №${document.properties["payments:paymentNumber"]!""} от <#if document.properties["payments:billDate"]??>${document.properties["payments:billDate"]?string('${dateFormat}')!}</#if></b><br/></p>
<p>Плательщик: <#if document.associations?? && document.associations["payments:payer"]?? && document.associations["payments:payer"]?size != 0>${document.associations["payments:payer"][0].properties["idocs:fullOrganizationName"]!""}</#if><br/>
Грузополучатель: <#if document.associations?? && document.associations["payments:payer"]?? && document.associations["payments:payer"]?size != 0>${document.associations["payments:payer"][0].properties["idocs:fullOrganizationName"]!""}</#if><br/></p>

<table width="700px" border="1">
    <tr>
        <td align="center" width="30px">№</td>
        <td align="center" width="300px">Наименование товара</td>
        <td align="center" width="30px">Единица измерения</td>
        <td align="center" width="40px">Количество </td>
        <td align="center" width="150px">Цена</td>
        <td align="center" width="150px">Сумма</td>
    </tr>
    <#assign count = 0/>
    <#assign totalAmount = 0/>
    <#if document.associations?? && document.associations["pas:containsProductsAndServices"]?? && document.associations["pas:containsProductsAndServices"]?size != 0>
        <#list document.associations["pas:containsProductsAndServices"] as containsProductsAndService>
            <#assign count = count + 1/>
            <tr>
                <td><p align="center">${count?string["0"]}</p></td>
                <td><p align="left">${containsProductsAndService.properties["cm:title"]!""}</p></td>
                <td><p align="center">${containsProductsAndService.associations["pas:entityUnit"][0].properties["pas:unitShortName"]!""}</p></td>
                <td><p align="center">${containsProductsAndService.properties["pas:quantity"]!""}</p></td>
                <td><p align="right">${containsProductsAndService.properties["pas:pricePerUnit"]!""}</p></td>
                <td><p align="right">${containsProductsAndService.properties["pas:total"]!""}</p></td>
            </tr>
            <#assign total = '${containsProductsAndService.properties["pas:total"]?string.computer!0}'/>
            <#assign totalAmount = totalAmount + total?number/>
        </#list>
    </#if>
                <tr>
                        <td style="border-style: hidden" colspan="5"><p align="right"><b>Итого:</b></p></td>
                        <td><p align="right">${totalAmount}</p></td>
                </tr>
                <tr>
                        <td style="border-style: hidden" colspan="5"><p align="right"><b>Сумма НДС:</b></p></td>
                        <td><p align="right">-</p></td>
                </tr>
                <tr>
                        <td colspan="5" style="border-style: hidden"><p align="right"><b>Всего к оплате:</b></p></td>
                        <td><p align="right">${totalAmount}</p></td>
                </tr>
        </table>
<p>Всего наименований ${count?string["0"]}, на сумму ${totalAmount} <#if document.associations?? && document.associations["payments:currency"]?? && document.associations["payments:currency"]?size != 0 && document.associations["payments:currency"][0].nodeRef=='workspace://SpacesStore/currency-rur'>руб.<#elseif document.associations?? && document.associations["payments:currency"]?? && document.associations["payments:currency"]?size != 0 && document.associations["payments:currency"][0].nodeRef=='workspace://SpacesStore/currency-usd'>USD (НДС не облагается согласно части 2 НК РФ, глава 26.2, статья 346.12, статья 346.13)</#if></p><br/>
<table width="700px" border="0" cellspacing="0">
    <tr border="0">
        <td border="0">Руководитель предприятия <@signaturePlace/> <#if document.associations?? && document.associations["payments:beneficiary"]?? && document.associations["payments:beneficiary"]?size != 0 && document.associations["payments:beneficiary"][0].associations["idocs:generalDirector"]?? && document.associations["payments:beneficiary"][0].associations["idocs:generalDirector"][0]?size != 0>(<@FIO document.associations["payments:beneficiary"][0].associations["idocs:generalDirector"][0]/>)<#elseif document.associations?? && document.associations["payments:beneficiary"]?? && document.associations["payments:beneficiary"]?size != 0>(${document.associations["payments:beneficiary"][0].properties["idocs:CEOname"]!})</#if></td>
    </tr>
    <tr border="0">
        <td border="0">Главный бухгалтер <@signaturePlace/> <#if document.associations?? && document.associations["payments:beneficiary"]?? && document.associations["payments:beneficiary"]?size != 0 && document.associations["payments:beneficiary"][0].associations["idocs:accountantGeneral"]?? && document.associations["payments:beneficiary"][0].associations["idocs:accountantGeneral"]?size != 0>(<@FIO document.associations["payments:beneficiary"][0].associations["idocs:accountantGeneral"][0]/>)</#if></td>
    </tr>
</table>

</body>
</html>
</#escape>
