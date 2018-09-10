<@markup id="css" >
   <#if config.global.header?? && config.global.header.dependencies?? && config.global.header.dependencies.css??>
      <#list config.global.header.dependencies.css as cssFile>
         <@link href="${url.context}/res${cssFile}" group="header"/>
      </#list>
   </#if>
   <@link href="${url.context}/res/js/citeck/lib/css/bootstrap.min.css" group="header" />
</@>

<@markup id="js">
   <#if config.global.header?? && config.global.header.dependencies?? && config.global.header.dependencies.js??>
      <#list config.global.header.dependencies.js as jsFile>
         <@script src="${url.context}/res${jsFile}" group="header"/>
      </#list>
   </#if>

   <#if __alf_current_site__?has_content>
      <@inlineScript group="header">
         var __alf_lastsite__ = "${__alf_current_site__}", expirationDate = new Date();
         expirationDate.setFullYear(expirationDate.getFullYear() + 1);
         document.cookie="alf_lastsite=" + __alf_lastsite__ + "; path=/; expires=" + expirationDate.toUTCString() + ";";
      </@>
   </#if>
</@>

<@markup id="widgets">
   <@inlineScript group="dashlets">
      <#if page.url.templateArgs.site??>
         Alfresco.constants.DASHLET_RESIZE = ${siteData.userIsSiteManager?string} && YAHOO.env.ua.mobile === null;
      <#else>
         Alfresco.constants.DASHLET_RESIZE = ${((page.url.templateArgs.userid!"-") = (user.name!""))?string} && YAHOO.env.ua.mobile === null;
      </#if>
   </@>

   <#assign pageArgsMap = ((page.url.templateArgs!{}) + (page.url.args!{}) + {
                   "pageid": "card-details",
                   "theme": "${theme!}"
               }) />

   <script type="text/javascript">//<![CDATA[
       var header = document.createElement('div');
       header.id = "share-header";
       document.getElementById('alf-hd').appendChild(header);
       var searchUrl = Alfresco.constants.PROXY_URI + "/citeck/ecosConfig/ecos-config-value?configName=default-ui-main-menu";
       var request = new XMLHttpRequest();
       request.open('GET', searchUrl, false);  // `false` makes the request synchronous
       request.send(null);
       if (request.status == 200 && request.responseText) {
           var data = eval('(' + request.responseText + ')');
           var isReactMenu = data && data.value == "left";
           if (isReactMenu) {
                var userName = '${user.name}';
                var userMenuItems = [{id: "HEADER_SITE_DASHBOARD", label:"hh66", url: "site/contract/dashboard",}];

               require(['react',
                        'react-dom',
                        'js/citeck/modules/header/share-header'], function(React, ReactDOM, ShareHeader) {
                            ReactDOM.render(React.createElement(ShareHeader.default, {label: 'test007', userMenuItems: userMenuItems, userName:userName}), document.getElementById('share-header'));
               });
           } else {
               require(['react',
                        'react-dom',
                        'js/citeck/modules/surf/surf-region'], function(React, ReactDOM, SurfRegion) {
                            var pageArgs = {
                                            <#list pageArgsMap?keys as argKey>
                                                "${argKey}":"${pageArgsMap[argKey]!}"<#if argKey_has_next>,</#if>
                                            </#list>
                                        };
                            var arguments = {regionId: "share-header",
                                        scope: "global",
                                        chromeless: "true",
                                        pageid: "card-details",
                                        site: pageArgs.site,
                                        theme: pageArgs.theme,
                                        cacheAge: 300,
                                        userName: "${((userName)!"")?js_string}"};

                            ReactDOM.render(React.createElement(SurfRegion.default, {args: arguments}), document.getElementById('share-header'));
               });
           }
       }
   //]]></script>
   <@inlineScript group="header">
      <#assign runtimeKey = args.runtimeKey!args.htmlid />
   </@>
</@>
