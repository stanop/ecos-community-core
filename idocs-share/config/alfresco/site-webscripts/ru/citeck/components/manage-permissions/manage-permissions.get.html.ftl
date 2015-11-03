<#include "/ru/citeck/components/form/controls/common/dynamic-tree-picker.inc.ftl" />
<#assign id=args.htmlid>
    <script type="text/javascript">

        new MP.ManagePermissions("${args.htmlid}").setOptions({
            nodeRef: "${(page.url.args["nodeRef"]!"")?js_string}",
            supportedPermissions: [
            <#list supportedPermissions as permissionGroup>
              {
                groupBrief: '${msg("permissions." + permissionGroup.group)}',
                permissions: [
                <#list permissionGroup.permissions as permission>
                    { alias: '${permission}', brief: '${msg("permission." + permission)}' }<#if permission_has_next>,</#if>
                </#list>
                ]
              }<#if permissionGroup_has_next>,</#if>
            </#list>
            ]
        }).setMessages(${messages});

        (function () {

            Citeck = typeof Citeck != "undefined" ? Citeck : {};
            Citeck.widget = Citeck.widget || {};

            var Dom = YAHOO.util.Dom;

            Citeck.widget.SelectNewOwner = function (htmlid,nodeRef, params) {
                Citeck.widget.SelectNewOwner.superclass.constructor.call(this, htmlid, htmlid + "-finder",
                        Alfresco.constants.URL_SERVICECONTEXT + "components/people-finder/people-finder", params);
                this.createEvent("itemSelected");
                YAHOO.Bubbling.on("personSelected", function (layer, args) {

                    var request = new XMLHttpRequest();
                    request.open("GET", Alfresco.constants.PROXY_URI + '/node/owner/set?nodeRef='+nodeRef+'&owner='+args[1].userName )
                    request.onload = function (response) {
                        var result = response.target.response;
                        if(result){
                            window.location.reload()
                        }
                    };
                    request.send(null);
                    this.fireEvent("itemSelected", args[1]);
                    this.hide();
                }, this);
            };
            YAHOO.extend(Citeck.widget.SelectNewOwner, Citeck.widget.AbstractFinderDialog);
        })();


        jQuery(document).ready(function(){
            var nodeRef = '${(page.url.args["nodeRef"]!"")?js_string}'
            window.SelectNewOwner = new Citeck.widget.SelectNewOwner('peoplepicker', nodeRef);
        jQuery('#${id}-change-owner').bind('click', function () {

            window.SelectNewOwner.show()
        })
        })
    </script>


    <div id="${id}-body" class="permissions">

        <div id="${id}-authorityFinder" class="authority-finder-container"></div>
        <div id="${id}-headerBar" class="header-bar flat-button">
            <div class="left">
                ${nodeBrief}<#--<span id="${id}-title"></span>-->
            </div>
            <div class="right">

                <div id="${id}-inheritedButtonContainer" class="inherited">
            <span id="${id}-inheritedButton" class="yui-button yui-push-button">
               <span class="first-child">
                  <button>${msg("button.inherited")}</button>
               </span>
            </span>
                </div>
                <div class="add-user-group">
            <span id="${id}-addUserGroupButton" class="yui-button yui-push-button">
               <span class="first-child">
                  <button>${msg("button.addUserGroup")}</button>
               </span>
            </span>
                </div>
            </div>
        </div>

        <div id="${id}-authorityTableContainer" class="container">
            <div class="title">
                <div style="display: inline-block;">${msg("title")}</div>
                <div class="owner">
                       <span>
                         <span class="first-child">
                             <#if owner == ''>
                                 <#assign owner= msg('no-owners')>
                             </#if>
                         ${msg("owner")}: ${owner}
                         </span>
                       </span>
                     <span class="yui-button yui-push-button">
                        <span class="first-child">
                             <button id="${id}-change-owner">${msg('owner.change')}</button>
                        </span>
            </span>

                </div>
            </div>
            <div id="${id}-authorityTable" class="permissions-list"></div>
        </div>

        <div class="center">
            <span id="${id}-cancelButton" class="yui-button yui-push-button">
                <span class="first-child">
                     <button>${msg("button.back")}</button>
                </span>
            </span>
        </div>

	    <@renderDynamicTreePickerHTML id />

    </div>
    <div id="peoplepicker" class="people-picker " style="visibility: hidden;">
        <div class="hd"><span id="peoplepicker-title">${msg("owner.choose")}</span></div>
        <div class="bd">
            <div style="margin: auto 10px;">
                <div id="peoplepicker-finder"></div>
            </div>
        </div>
    </div>

