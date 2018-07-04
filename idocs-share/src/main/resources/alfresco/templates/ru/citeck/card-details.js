<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/head/resources.get.js">
<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
<import resource="classpath:/alfresco/site-webscripts/ru/citeck/components/dependencies/dependencies.lib.js">
// See if a site page's title has been renamed by the site manager
model.metaPage = AlfrescoUtil.getMetaPage();

model.nodeBaseInfo = {
    permissions: {
        Read: false,
        Write: false
    }
};
model.nodeBaseInfoStr = '{"modified": null}';

const connector = remote.connect("alfresco");
const nodeRef = page.url.args.nodeRef;

if (nodeRef) {
    const nodeBaseInfoUrl = "/citeck/node/base-info?nodeRef=" + nodeRef;
    try {
        const result = connector.get(nodeBaseInfoUrl);
        if (result.status == 200) {
            model.nodeBaseInfoStr = result + '';
            model.nodeBaseInfo = eval('(' + result + ')');
        }
    } catch (e) {
        logger.warn("Connection to " + nodeBaseInfoUrl + " is failed");
        logger.warn("Error", e);
    }

    model.hasReadPermissions = model.nodeBaseInfo.permissions.Read;

    if (model.hasReadPermissions) {
        model.pageDependencies = Dependencies.getScoped('CiteckPage/card-details/dependencies');
    }
}
