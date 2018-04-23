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
<#assign attachmentName = args.attachment.properties["cm:title"]!attachment.properties["cm:name"] />

<#switch args.eventData.getEmailType()>
    <#case "SIGNING_DELIVERY_FAILED">
    Не удалось отправить подписанный документ ${attachmentName}
        <#assign errorMessage = (args.attachment.properties["sam:packageErrorCode"])! />
        <#break>
</#switch>
<br>
<table border="1">
    <caption>${args.document.properties["cm:title"]!""}</caption>
    <tr>
        <td>Рег. (системный) номер:</td>
        <td><a href="${shareUrl}/page/document-details?nodeRef=${args.document.nodeRef}"><u><i>${args.document.properties["contracts:agreementNumber"]!"Number not found"}</i></u></a></td>
    </tr>
    <tr>
        <td>Инициатор:</td>
        <td><#if args.document?? && args.document.associations["idocs:initiator"]??
        && args.document.associations["idocs:initiator"]?size != 0>
            ${args.document.associations["idocs:initiator"][0].properties["cm:firstName"]!""} ${args.document.associations["att:initiator"][0].properties["cm:lastName"]!""}
        </#if></td>
    </tr>
    <tr>
        <td>Дата документа:</td>
        <td><#if args.document.properties["contracts:agreementDate"]??>${args.document.properties["contracts:agreementDate"]?date}</#if></td>
    </tr>
    <tr>
        <td>Наименование Контрагента:</td>
        <td>
        <#if args.document.associations["idocs:contractor"]?? && args.document.associations["idocs:contractor"]?size != 0>
                ${args.document.associations["idocs:contractor"][0].properties["idocs:fullOrganizationName"]!""}
            </#if>
        </td>
    </tr>
    <tr>
        <td>Статус:</td>
        <td><#if args.document.associations["icase:caseStatusAssoc"]?? && args.document.associations["icase:caseStatusAssoc"]?size != 0>
        ${args.document.associations["icase:caseStatusAssoc"][0].properties["cm:title"]!""}
        </#if>
        </td>
    </tr>

<#if counterpartyComment??>
    <tr>
        <td>Комментарий:</td>
        <td>${counterpartyComment}</td>
    </tr>
</#if>
<#if errorMessage??>
    <tr>
        <td>Сообщение об ошибке:</td>
        <td>${errorMessage}</td>
    </tr>
</#if>
</table>
<hr>
</body>
</html>