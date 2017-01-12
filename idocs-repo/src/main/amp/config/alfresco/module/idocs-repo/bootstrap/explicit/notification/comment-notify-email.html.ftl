<html>
<head>
</head>

<body bgcolor="#dddddd">
<table width="100%" cellpadding="20" cellspacing="0" border="0" bgcolor="#dddddd">
    <tr>
        <td width="100%" align="center">
            <table width="70%" cellpadding="0" cellspacing="0" bgcolor="white"
                   style="background-color: white; border: 1px solid #aaaaaa;">
                <tr>
                    <td width="100%">
                        <table width="100%" cellpadding="0" cellspacing="0" border="0">
                            <tr>
                                <td style="padding: 10px 30px 0px;">
                                    <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                        <tr>
                                            <td>
                                                <table cellpadding="0" cellspacing="0" border="0">
                                                    <tr>
                                                        <td>
                                                            <div style="font-size: 22px; padding-bottom: 4px;">
                                                                Файл <a href="${shareUrl}/${args.nodeDetailsLink}"> ${args.fileName} </a>
                                                                был прокомментирован пользователем ${args.commentAuthor}
                                                            </div>
                                                        </td>
                                                    </tr>
                                                </table>
                                                <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">

                                                    ${args.commentText}

                                                    <br/><br/>

                                                    <a href="${shareUrl}/${args.nodeDetailsLink}"> Просмотреть </a>

                                                    <br/><br/>

                                                </div>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>

                        </table>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
</body>
</html>