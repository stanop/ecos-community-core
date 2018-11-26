<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/head/resources.get.js">
<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

const connector = remote.connect("alfresco");
const formKey = page.url.args.formKey;
const formType = page.url.args.formType;

if (formKey && formType) {
    model.formKey = formKey;
    model.formType = formType;
    const checkUrl = "/citeck/invariants/view-check?" + formType + "=" + formKey;
    try {
        const result = connector.get(checkUrl);
        if (result.status == 200) {
            var resultCheck = eval('(' + result + ')');
            model.isExist = resultCheck.exists;
            model.isDefaultExist = resultCheck.defaultExists;
        }
    } catch (e) {
        logger.warn("Connection to " + checkUrl + " is failed");
        logger.warn("Error", e);
    }
}
