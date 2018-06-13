<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/head/resources.get.js">
<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/dependencies/dependencies.lib.js">
// See if a site page's title has been renamed by the site manager
model.metaPage = AlfrescoUtil.getMetaPage();

model.hasReadPermissions = false;
const connector = remote.connect("alfresco");
const nodeRef = page.url.args.nodeRef;

if (nodeRef) {
    const permCheckUrl = "/citeck/has-permission?nodeRef=" + nodeRef + "&permission=Read";
    try {
        const result = connector.get(permCheckUrl);
        model.hasReadPermissions = result.status == 200 && result.response == "true";
    } catch (e) {
        logger.warn("Connection to " + permCheckUrl + " is failed");
        logger.warn("Error", e);
    }

    if (model.hasReadPermissions) {
        model.pageDependencies = Dependencies.getByPage('card-details');
    }
}
