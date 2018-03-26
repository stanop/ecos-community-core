(function () {
    const STATUS_TYPE_CASE = 'case-status';
    const STATUS_TYPE_DOCUMENT = 'document-status';

    var caseStatus = caseStatusService.getStatusNode(args.nodeRef);
    var statusName = "";
    var statusType = "status";
    if (!caseStatus) {
        var node = search.findNode(args.nodeRef);
        caseStatus = node.properties['idocs:documentStatus'];
        if (caseStatus) {
            importPackage(Packages.org.springframework.extensions.surf.util);
            var statusKey = "listconstraint.idocs_constraint_documentStatus." + caseStatus;
            statusName = I18NUtil.getMessage(statusKey) || statusKey;
            statusType = STATUS_TYPE_DOCUMENT;
        }
    } else {
        statusName = caseStatus.properties['cm:title'] || caseStatus.properties['cm:name'];
        statusType = STATUS_TYPE_CASE;
    }
    model.json = {
        statusName: statusName,
        statusType: statusType
    };
})();


