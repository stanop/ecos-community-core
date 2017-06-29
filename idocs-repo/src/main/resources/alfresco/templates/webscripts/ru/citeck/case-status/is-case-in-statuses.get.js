function main() {
    var nodeRef = args['nodeRef'];
    var statuses = args['statuses'];

    var data = "false";
    if (nodeRef && statuses) {
        data = caseStatusService.isDocumentInStatus(statuses.split(','), nodeRef);
    }
    model.data = data ? "true":"false";
}

main();
