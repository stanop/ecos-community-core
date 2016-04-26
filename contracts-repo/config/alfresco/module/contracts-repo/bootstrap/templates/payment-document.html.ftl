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
        <#if firstName!="">${firstName?substring(0, 1)}. <#if middleName!="">${middleName?substring(0, 1)}
        . </#if></#if>${lastName}
    </#macro>

    <#macro findBankAccount>
        <#if beneficiary?? && beneficiary.sourceAssociations["idocs:legalEntity"]?? && beneficiary.sourceAssociations["idocs:legalEntity"]?size != 0>
            <#assign assocs = beneficiary.sourceAssociations["idocs:legalEntity"] />
            <#list assocs as assoc>
                <#if assoc.typeShort == "idocs:bankAccount" && assoc.properties["idocs:currencyEnabled"] == true && document.associations["payments:currency"]?? && document.associations["payments:currency"]?size != 0 && document.associations["payments:currency"][0].nodeRef=='workspace://SpacesStore/currency-usd'>
                    <#assign bankAccount = assoc />
                    <#break>
                <#elseif assoc.typeShort == "idocs:bankAccount" && assoc.properties["idocs:currencyEnabled"] == false && document.associations["payments:currency"]?? && document.associations["payments:currency"]?size != 0 && document.associations["payments:currency"][0].nodeRef=='workspace://SpacesStore/currency-rur'>
                    <#assign bankAccount = assoc />
                    <#break>
                </#if>
            </#list>
        </#if>
    </#macro>

    <#macro findBeneficiary>
        <#if document.associations?? && document.associations["payments:payer"]?? && document.associations["payments:payer"]?size != 0>
            <#assign beneficiary = document.associations["payments:payer"][0] />
        </#if>
    </#macro>

    <#macro findPayer>
        <#if document.associations?? && document.associations["payments:beneficiary"]?? && document.associations["payments:beneficiary"]?size != 0>
            <#assign payer = document.associations["payments:beneficiary"][0] />
        </#if>
    </#macro>

    <#assign VAT = 0>
    <#if document.properties["payments:paymentVAT"]??>
        <#assign VAT = '${document.properties["payments:paymentVAT"]?string.computer!0}'>
    </#if>

    <@findPayer />
    <@findBeneficiary />
    <@findBankAccount />


<p><b><#if beneficiary??>${beneficiary.properties["idocs:fullOrganizationName"]!""}</#if><br/>
Адрес: <#if beneficiary??>${beneficiary.properties["idocs:juridicalAddress"]!beneficiary.properties["idocs:postAddress"]!}</#if></b></p>
<p align="center"><b>Образец заполнения платежного поручения</b><br/></p>
<table width="700px" border="1">
    <tr>
        <td nowrap colspan="2" width="230px">ИНН <#if beneficiary??>${beneficiary.properties["idocs:inn"]!""}&nbsp;</#if></td>
        <td nowrap colspan="2" width="200px">КПП <#if beneficiary??>${beneficiary.properties["idocs:kpp"]!""}&nbsp;</#if></td>
        <td rowspan="2" width="60px" align="center"><#if bankAccount?? && bankAccount.properties["idocs:currencyEnabled"] == true >Кор. сч. №</#if></td>
        <td rowspan="2" width="210px">
            <#if bankAccount?? && bankAccount.properties["idocs:currencyEnabled"] == true>${bankAccount.properties["idocs:corresponentAccountNumber"]!""}</#if>
        </td>
    </tr>
    <tr>
        <td colspan="4">Получатель</td>
    </tr>
    <tr>
        <td colspan="2"><#if beneficiary??>${beneficiary.properties["idocs:shortOrganizationName"]!""}</#if></td>
        <td colspan="2">
            <#if bankAccount?? && bankAccount.properties["idocs:currencyEnabled"] == true && beneficiary??>
                ${beneficiary.properties["idocs:engOrganizationName"]!""}
            </#if>
        </td>
        <td align="center">Сч. №</td>
        <td><#if bankAccount?? && bankAccount.properties["idocs:accountNumber"] != "">${bankAccount.properties["idocs:accountNumber"]!""}</#if></td>
    </tr>
    <tr>
        <td colspan="4">Банк получателя</td>
        <td align="center">БИК</td>
        <td><#if bankAccount?? && bankAccount.properties["idocs:bankId"] != "">${bankAccount.properties["idocs:bankId"]!""}</#if></td>
    </tr>
    <tr>
        <td colspan="2"><#if bankAccount?? && bankAccount.properties["idocs:bankTitle"] != "">${bankAccount.properties["idocs:bankTitle"]!""}</#if></td>
        <td colspan="2">
            <#if bankAccount?? && bankAccount.properties["idocs:currencyEnabled"] == true>
                ${bankAccount.properties["idocs:bankTitleEng"]}
            </#if>
        </td>
        <td rowspan="2" align="center">
            <#if bankAccount?? && bankAccount.properties["idocs:currencyEnabled"] == true>
                SWIFT<#elseif bankAccount?? && bankAccount.properties["idocs:currencyEnabled"] == false>Сч. №</#if>

        </td>
        <td>
            <#if bankAccount?? && bankAccount.properties["idocs:currencyEnabled"] == true>
                ${bankAccount.properties["idocs:swift"]!""}<#elseif bankAccount?? && bankAccount.properties["idocs:currencyEnabled"] == false>${bankAccount.properties["idocs:corresponentAccountNumber"]!""}</#if>
        </td>
    </tr>
</table>
<br/>
<p align="center"><b>СЧЕТ №${document.properties["payments:paymentNumber"]!""} от <#if document.properties["payments:billDate"]??>${document.properties["payments:billDate"]?string('${dateFormat}')!}</#if></b><br/></p>
<p>Плательщик: <#if payer??>${payer.properties["idocs:fullOrganizationName"]!""}</#if><br/>
Грузополучатель: <#if payer??>${payer.properties["idocs:fullOrganizationName"]!""}</#if><br/></p>

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
                        <td style="border-style: hidden" colspan="5"><p align="right"><b>НДС:</b></p></td>
                        <td><p align="right">
                            <#if VAT?? && VAT?number gt 0>
                                ${VAT?number}
                            <#else>-
                            </#if>
                        </p></td>
                </tr>
                <tr>
                        <td colspan="5" style="border-style: hidden"><p align="right"><b>Всего к оплате:</b></p></td>
                        <td><p align="right">${totalAmount + VAT?number}</p></td>
                </tr>
        </table>

<table width="700px" border="0" cellspacing="0">
    <tr>
        <td>
            Всего наименований ${count?string["0"]}, на сумму ${totalAmount}
            <#if document.associations?? && document.associations["payments:currency"]?? && document.associations["payments:currency"]?size != 0 && document.associations["payments:currency"][0].nodeRef=='workspace://SpacesStore/currency-rur'>руб.
            <#elseif document.associations?? && document.associations["payments:currency"]?? && document.associations["payments:currency"]?size != 0 && document.associations["payments:currency"][0].nodeRef=='workspace://SpacesStore/currency-usd'>USD
            <#elseif document.associations?? && document.associations["payments:currency"]?? && document.associations["payments:currency"]?size != 0 && document.associations["payments:currency"][0].nodeRef=='workspace://SpacesStore/currency-eur'>EUR
            </#if>
        </td>
    </tr>
    <tr>
        <td>
            <#if document.properties["payments:paymentAmountInWords"]??>${document.properties["payments:paymentAmountInWords"]}</#if>
            <#if document.associations?? && document.associations["payments:currency"]?? && document.associations["payments:currency"]?size != 0 && document.associations["payments:currency"][0].nodeRef=='workspace://SpacesStore/currency-usd' && VAT?number == 0>(НДС не облагается согласно части 2 НК РФ, глава 26.2, статья 346.12, статья 346.13)
            <#elseif document.associations?? && document.associations["payments:currency"]?? && document.associations["payments:currency"]?size != 0 && document.associations["payments:currency"][0].nodeRef=='workspace://SpacesStore/currency-eur'&& VAT?number == 0>(НДС не облагается согласно части 2 НК РФ, глава 26.2, статья 346.12, статья 346.13)
            </#if>
        </td>
    </tr>
</table>
</br>
<table width="700px" border="0" cellspacing="0">
    <tr>
        <td>Руководитель предприятия <@signaturePlace/> <#if beneficiary?? && beneficiary.associations["idocs:generalDirector"]?? && beneficiary.associations["idocs:generalDirector"][0]?size != 0>(<@FIO beneficiary.associations["idocs:generalDirector"][0]/>)<#elseif beneficiary??>(${beneficiary.properties["idocs:CEOname"]!})</#if></td>
    </tr>
    <tr>
        <td>Главный бухгалтер <@signaturePlace/> <#if beneficiary?? && beneficiary.associations["idocs:accountantGeneral"]?? && beneficiary.associations["idocs:accountantGeneral"]?size != 0>(<@FIO beneficiary.associations["idocs:accountantGeneral"][0]/>)</#if></td>
    </tr>
</table>

</body>
</html>
</#escape>
