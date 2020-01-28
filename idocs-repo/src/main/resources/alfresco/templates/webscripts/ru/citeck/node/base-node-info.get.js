
function main() {

    var nodeRef = args['nodeRef'];

    if (nodeRef) {

        var node = search.findNode(nodeRef);

        var updateUtil = services.get("ecos.itemsUpdateState");

        model.result = {
            permissions: {
                Read: node.hasPermission('Read'),
                Write: node.hasPermission('Write')
            },
            modified: utils.toISO8601(node.properties['cm:modified']),
            pendingUpdate: updateUtil.isPendingUpdate(node.nodeRef),
            isOldCardDetailsRequired: isOldCardDetaildRequired(nodeRef)
        }

    } else {
        throw "nodeRef is a mandatory parameter";
    }
}

function isOldCardDetaildRequired(nodeRef) {
    try {
        var newUIUtils = services.get('newUIUtils');
        var recordRef = Packages.ru.citeck.ecos.records2.RecordRef.valueOf(nodeRef);
        return newUIUtils.isOldCardDetailsRequired(recordRef);
    } catch (e) {
        try {
            logger.error(e);
        } catch (ee) {
            try {
                logger.warn("Problem with logging", e);
            } catch (eee) {
            }
        }
        return true;
    }
}

main();
