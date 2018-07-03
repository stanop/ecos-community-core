<#assign htmlId = args.htmlid?js_string />
<#assign wrapperId = htmlId + "-wrapper" />
<div id="${wrapperId}"></div>

<script type="text/javascript">//<![CDATA[
    require(['jquery'], function() {
        Alfresco.util.Ajax.jsonGet({
            url: "/share/service/${args.webscriptUrl}",
            dataObj: {
            <#if args.arguments?has_content>
                <#list args.arguments?split(",") as arg>
                    "${arg}":"${(args[arg]!"")?js_string}",
                </#list>
            </#if>
                "htmlid": "${htmlid}"
            },
            successCallback: {
                fn: function(response) {
                    $("#${wrapperId}").html(response.serverResponse.responseText);
                }
            },
            failureCallback: {
                fn: function(response) {
                    $("#${wrapperId}").html("Ошибка во время загрузки");
                    console.error(response);
                }
            }
        });
    });
//]]></script>