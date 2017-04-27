<html>
<head>
    <style type="text/css"></style>
</head>
<body bgcolor="white">
<div style="font-size: 14px; margin: 0px 0px 0px 0px; padding-top: 0px; border-top: 0px solid #aaaaaa;">
<#if args.workflow.documents??>
    <div style="font-size: 14px; margin: 0px 0px 0px 0px; padding-top: 0px; border-top: 0px solid #aaaaaa;">
        <#if args.workflow.documents.typeShort == "contracts:agreement">
            <p> Договор №: <u>${args.workflow.documents.properties["contracts:agreementNumber"]!"б-н"}</u> был рассмотрен и согласован. <br></p>
        <#elseif args.workflow.documents.typeShort == "contracts:supplementaryAgreement">
            <p> Доп.соглашение №: <u>${args.workflow.documents.properties["contracts:agreementNumber"]!"б-н"}</u> было рассмотрено и согласовано. <br></p>
            <#else>
            <p>Завершено согласование документа.</p>
        </#if>
     </div>
</#if>

    <div style="font-size: 14px; margin: 0px 0px 0px 0px; padding-top: 10px; border-top: 0px solid #aaaaaa;">
        Перейти к документу можно по ссылке: <a href="${shareUrl}/page/document-details?nodeRef=${args.workflow.documents.nodeRef}"><u>${shareUrl}/page/document-details?nodeRef=${args.workflow.documents.nodeRef}</u></a>
    </div>

    <div style="font-size: 14px; margin: 0px 0px 0px 0px; padding-top: 10px; border-top: 0px solid #aaaaaa;">
        Сведения о процессе: <a href="${shareUrl}/page/workflow-details?workflowId=activiti$${args.workflow.id}&amp;nodeRef=${args.workflow.documents.nodeRef}"><u>${shareUrl}/page/workflow-details?workflowId=activiti$${args.workflow.id}&amp;nodeRef=${args.workflow.documents.nodeRef}</u></a>
    </div>

</div>
</body>
</html>