<#assign el = args.htmlid?html />

<#if view??>

    <div id="${el}-body" class="document-details-panel node-view <#if args.class??>${args.class?string}</#if>">

        <h2 id="${el}-heading" class="thin dark">
            ${msg(args.header!"header.view")}
            <span class="alfresco-twister-actions <#if args.hideEditAction?? && args.hideEditAction == "true">hidden</#if>">
                <#if writePermission?? && writePermission>
                    <a class="edit"
                       href="${url.context}/page/node-edit?nodeRef=${viewNodeRef!args.nodeRef}<#if args.viewId??>&viewId=${args.viewId}</#if>"></a>
                </#if>
             </span>
        </h2>

        <div class="panel-body">
            <#include "/ru/citeck/components/invariants/view.get.html.ftl" />
        </div>

        <script type="text/javascript">//<![CDATA[

            var listener = function (layer, args) {

                YAHOO.Bubbling.unsubscribe("metadataRefresh", listener);

                var refreshArgs = "<#list refreshArgs?keys as arg>${arg}=${refreshArgs[arg]}<#if arg_has_next>&</#if></#list>";

                var component = Alfresco.util.ComponentManager.get("${el}-form");
                var full = true;
                component.runtime.node().impl().reset(full, 3);

                Alfresco.util.Ajax.request({
                    url: "citeck/components/node-details/node-view?" + refreshArgs,
                    successCallback: {
                        fn: function (response) {

                            Alfresco.util.ComponentManager.unregister(component);

                            var q = Alfresco.util.Ajax.sanitizeMarkup(response.serverResponse.responseText);

                            YAHOO.util.Dom.get("${el}").innerHTML = q[0];
                            window.setTimeout(q[1], 0)
                        },
                        scope: this
                    }
                });
            };

            YAHOO.Bubbling.on("metadataRefresh", listener);

            Alfresco.util.createTwister("${el}-heading", "node-view");

        //]]></script>

        <#if args.style??>
            <style type="text/css">
                ${args.style?string}
            </style>
        </#if>

    </div>

</#if>

