<html>
    <head>
        <style type="text/css"></style>
    </head>
    <body bgcolor="white">
        <#if args.document??>
            <div style="font-size: 14px; margin: 0px 0px 0px 0px; padding-top: 0px; border-top: 0px solid #aaaaaa;">
                <p>В документе <span style="text-decoration:underline">${args.document.properties["cm:name"]}</span> произошли изменения. </p>
            </div>
            <p>Изменения: </p>
            <#if args.addition.properties?? && args.addition.properties?size != 0>
                <#list args.addition.properties as prop>
                    <div style="font-size: 14px; margin: 0px 0px 0px 20px; padding-top: 0px; border-top: 0px solid #aaaaaa;">
                        <#if (prop.title)??>
                            свойство:
                            <i>${prop.title}</i>,
                            <#if (prop.before)??>
                                значение до:
                                <#if prop.before?is_date>
                                    <i>${prop.before?date?string("dd.MM.yyyy")}</i>,
                                <#elseif prop.before?is_boolean>
                                    <i>${prop.before?string("yes", "no")}</i>,
                                <#else>
                                    <i>${prop.before}</i>,
                                </#if>
                            </#if>
                            <#if (prop.after)??>
                                значение после:
                                <#if prop.after?is_date>
                                    <i>${prop.after?date?string("dd.MM.yyyy")}</i>.
                                <#elseif prop.after?is_boolean>
                                    <i>${prop.after?string("yes", "no")}</i>.
                                <#else>
                                    <i>${prop.after}</i>.
                                </#if>
                            </#if>
                        <#elseif (prop.event)??>
                            <#if prop.event == "added">
                                <i>Добавлена ассоциация с </i>
                            <#elseif prop.event == "deleted">
                                <i>Удалена ассоциация с </i>
                            </#if>
                            <#if (prop.target)??>
                                <#assign target = prop.target />
                                <#if target.typeShort == "cm:person">
                                    <i>${target.properties["cm:lastName"]} ${target.properties["cm:firstName"]}. </i>
                                <#elseif (target.properties["cm:title"])??>
                                    <i>${target.properties["cm:title"]}</i>
                                <#else>
                                    <i>${target.properties["cm:name"]}. </i>
                                </#if>
                            </#if>
                            <#if (prop.type)??>
                                <i>Тип ассоциации: ${prop.type}</i>
                            </#if>
                        </#if>
                    </div>
                </#list>
            </#if>
            <p>Автор изменений <#if args.modifier??><span style="text-decoration:underline">${args.modifier}</span></#if>
                <#if (args.document.properties["cm:modified"])??>
                    , дата изменений <span style="text-decoration:underline">${args.document.properties["cm:modified"]?date?string("dd.MM.yyyy")}</span>
                </#if>. <br>
            </p>
        </#if>
    </body>
</html>