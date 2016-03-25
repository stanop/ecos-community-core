<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
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


<p><b><#if document.associations?? && document.associations["payments:beneficiary"]?? && document.associations["payments:beneficiary"]?size != 0>${document.associations["payments:beneficiary"][0].properties["idocs:fullOrganizationName"]!""}</#if><br/>
Адрес: <#if document.associations?? && document.associations["payments:beneficiary"]?? && document.associations["payments:beneficiary"]?size != 0>${document.associations["payments:beneficiary"][0].properties["idocs:juridicalAddress"]!document.associations["payments:beneficiary"][0].properties["idocs:postAddress"]!}</#if></b></p>
<p align="center"><b>Образец заполнения платежного поручения</b><br/></p>
<table width="700px" border="1">
    <tr>
        <td width="120px">ИНН <#if document.associations?? && document.associations["payments:beneficiary"]?? && document.associations["payments:beneficiary"]?size != 0>${document.associations["payments:beneficiary"][0].properties["idocs:inn"]!""}</#if></td>
        <td align="center" width="120px">&nbsp;</td>
        <td width="120px">КПП <#if document.associations?? && document.associations["payments:beneficiary"]?? && document.associations["payments:beneficiary"]?size != 0>${document.associations["payments:beneficiary"][0].properties["idocs:kpp"]!""}</#if></td>
        <td align="center" width="55px">&nbsp;</td>
        <td rowspan="2" width="45px"><#if document.associations?? && document.associations["payments:currency"]?? && document.associations["payments:currency"]?size != 0 && document.associations["payments:currency"][0].nodeRef=='workspace://SpacesStore/currency-usd'>Кор. Сч. №</#if></td>
        <td width="240px">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="4">Получатель</td>
        <td>&nbsp;</td>
    </tr>
    <tr>
        <td><#if document.associations?? && document.associations["payments:beneficiary"]?? && document.associations["payments:beneficiary"]?size != 0>${document.associations["payments:beneficiary"][0].properties["idocs:shortOrganizationName"]!""}</#if></td>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
        <td>Сч.</td>
        <td><#if document.associations?? && document.associations["payments:beneficiary"]?? && document.associations["payments:beneficiary"]?size != 0>${document.associations["payments:beneficiary"][0].properties["idocs:bill"]!""}</#if></td>
    </tr>
    <tr>
        <td colspan="4">Банк получателя</td>
        <td>БИК</td>
        <td><#if document.associations?? && document.associations["payments:beneficiaryAccount"]?? && document.associations["payments:beneficiaryAccount"]?size != 0>${document.associations["payments:beneficiaryAccount"][0].properties["idocs:bankId"]!""}</#if></td>
    </tr>
    <tr>
        <td><#if document.associations?? && document.associations["payments:beneficiaryAccount"]?? && document.associations["payments:beneficiaryAccount"]?size != 0>${document.associations["payments:beneficiaryAccount"][0].properties["idocs:bankTitle"]!""}</#if></td>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
        <td rowspan="2">&nbsp;</td>
        <td><#if document.associations?? && document.associations["payments:beneficiaryAccount"]?? && document.associations["payments:beneficiaryAccount"]?size != 0>${document.associations["payments:beneficiaryAccount"][0].properties["idocs:accountNumber"]!""}</#if></td>
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
                <td>${containsProductsAndService.associations["pas:entityUnit"][0].properties["pas:unitShortName"]!""}</td>
                <td>${containsProductsAndService.properties["pas:quantity"]!""}</td>
                <td>${containsProductsAndService.properties["pas:pricePerUnit"]!""}</td>
                <td>${containsProductsAndService.properties["pas:total"]?string.computer!0}</td>
            </tr>
            <#assign total = '${containsProductsAndService.properties["pas:total"]?string.computer!0}'/>
            <#assign totalAmount = totalAmount?number + total?number/>
        </#list>
    </#if>
            <tr>
                <td colspan="5" rowspan="3"></td>
                <td>${totalAmount}</td>
            </tr>
            <tr>
                <td>-</td>
            </tr>
            <tr>
                <td>${totalAmount}</td>
            </tr>
        </table>
<p>Всего оказано услуг ${count}, на сумму ${totalAmount} <#if document.associations?? && document.associations["payments:currency"]?? && document.associations["payments:currency"]?size != 0 && document.associations["payments:currency"][0].nodeRef=='workspace://SpacesStore/currency-rur'>руб.<#elseif document.associations?? && document.associations["payments:currency"]?? && document.associations["payments:currency"]?size != 0 && document.associations["payments:currency"][0].nodeRef=='workspace://SpacesStore/currency-usd'>USD (НДС не облагается согласно части 2 НК РФ, глава 26.2, статья 346.12, статья 346.13)</#if></p><br/>
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
