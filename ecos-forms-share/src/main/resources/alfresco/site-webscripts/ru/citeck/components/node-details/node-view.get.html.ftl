<#assign el = args.htmlid?html />
<#assign nodeViewNodeRef = viewNodeRef!args.nodeRef />
<#assign nodeViewEditBtnId = "edit-" + nodeViewNodeRef />

<#if view??>

    <div id="${el}-body" class="document-details-panel node-view <#if args.class??>${args.class?string}</#if>">

        <#if !args.hideHeading??>
            <h2 id="${el}-heading" class="thin dark">
                ${msg(args.header!"header.view")}
                <span class="alfresco-twister-actions <#if args.hideEditAction?? && args.hideEditAction == "true">hidden</#if>">
                    <#if writePermission?? && writePermission>
                        <a id="${nodeViewEditBtnId}" class="edit"
                           href="${url.context}/page/node-edit?nodeRef=${nodeViewNodeRef}<#if args.viewId??>&viewId=${args.viewId}</#if>"></a>
                    </#if>
                 </span>
            </h2>
        </#if>

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
                    url: "/share/page/citeck/components/node-details/node-view?" + refreshArgs,
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

            <#if !args.hideTwister??>
                Alfresco.util.createTwister("${el}-heading", "node-view");
            </#if>

            require(['citeck/components/form/constraints'], function () {

                var editBtn = document.getElementById("${nodeViewEditBtnId}");
                if (editBtn) {
                    editBtn.onclick = function(e) {
                        e.preventDefault();
                        try {
                            Citeck.forms.editRecord({
                                recordRef: "${nodeViewNodeRef}",
                                fallback: function() {
                                    window.location = editBtn.href;
                                },
                                onSubmit: function() {
                                    YAHOO.Bubbling.fire("metadataRefresh");
                                }
                            });
                        } catch (e) {
                            console.error(e);
                            window.location = editBtn.href;
                        }
                    }
                }
            });
        //]]></script>

        <#if args.style??>
            <style type="text/css">
                ${args.style?string}
            </style>
        </#if>

    </div>

</#if>

