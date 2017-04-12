<import resource="classpath:/alfresco/site-webscripts/org/alfresco/share/imports/share-header.lib.js">
<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/header/header-tokens.lib.js">

var customTokenMap = null;
function getTokenMap()
{
    if (customTokenMap == null)
    {
        customTokenMap = {
            site: (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
            pageid: (page.url.templateArgs.pageid != null) ? page.url.templateArgs.pageid : "",
            userid: encodeURIComponent(user.name)
        };
        
        // insert lastsite into tokens
        var lastsite = getLastSiteFromCookie();
        customTokenMap.lastsite = lastsite;
    }
    return customTokenMap;
}

var getDefaultPageTitle = getPageTitle;
function getCustomPageTitle() {
    var titleConfig = config.scoped['ShareHeader']['page-titles'];
    if(!titleConfig) return null;
    var pages = titleConfig.childrenMap['page'],
        currentPageConfig = null;
    
    // get current page config
    for(var i = 0, ii = pages.size(); i < ii; i++) {
        var pageConfig = pages.get(i);
        if(pageConfig.attributes.id == page.id) {
            currentPageConfig = pageConfig;
            break;
        }
    }
    if(currentPageConfig == null) return null;
    
    // get title configs
    var titleConfigs = pageConfig.childrenMap['title'],
        titleConfigList = [],
        titleConfigMap = {};
    for(var i = 0, ii = titleConfigs.size(); i < ii; i++) {
        var titleConfig = titleConfigs.get(i);
        titleConfigJS = {
            id: titleConfig.attributes.id,
            before: titleConfig.attributes.before || null,
            template: titleConfig.attributes.template
        };
        titleConfigMap[titleConfigJS.id] = titleConfigJS;
        var beforeConfig = titleConfigMap[titleConfigJS.before];
        if(beforeConfig) {
            var beforeIndex = titleConfigList.indexOf(beforeConfig);
            titleConfigList.splice(beforeIndex, 0, titleConfigJS);
        } else {
            titleConfigList.push(titleConfigJS);
        }
    }
    
    // process title configs one by one
    var args = page.url.args;
    for(var i in titleConfigList) {
        var titleConfig = titleConfigList[i],
            template = titleConfig.template,
            titleId = template;
        for(var name in args) {
            titleId = titleId.replace('{' + name + '}', (args[name] || '').replace(/[:]/g, '_'));
        }
        var title = messages.get(titleId);
        if(title != null) return title;
    }
    
    return null;
}
getPageTitle = function() {
    return getCustomPageTitle() || getDefaultPageTitle();
};

model.jsonModel = {
    rootNodeId: "share-header",
    services: getHeaderServices(),
    widgets: [
       {
          id: "SHARE_VERTICAL_LAYOUT",
          name: "alfresco/layout/VerticalWidgets",
          config: 
          {
             widgets: getHeaderModel()
          }
       }
    ]
};
