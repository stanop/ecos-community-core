/**
 *  Return value for Ecos Config Name
 *
 * @method getEcosConfig
 */
function getEcosConfig() {
    var configName = args.configName;
    if (!configName) {
        status.setCode(status.STATUS_BAD_REQUEST, "Argument 'configName' must be specified");
        return null;
    }

    var nodes = search.luceneSearch('TYPE\:"config:ecosConfig" AND @config\\:key:"' + args.configName + '"');
    var configValue = "";

    if (nodes.length > 0) {

        containerNode = nodes[0].nodeRef;
        if (nodes[0].properties["config:value"]) {
            configValue = nodes[0].properties["config:value"];
        }
    }
    return ({container: containerNode, "value": configValue});
}
model.data = getEcosConfig();


