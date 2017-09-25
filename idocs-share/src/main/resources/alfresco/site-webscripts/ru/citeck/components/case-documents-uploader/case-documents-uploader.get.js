(function() {

  if(!args.nodeRef) return;
  model.nodeRef = args.nodeRef;

  var documentType = getNodeInfo(args.nodeRef);
  model.documentType = documentType["tk:type"] ? documentType["tk:type"]["nodeRef"] : "";
  model.documentKind = documentType["tk:kind"] ? documentType["tk:kind"]["nodeRef"] : "";

  if (args.documentUploadDefaultType)
    model.documentUploadDefaultType = args.documentUploadDefaultType;

  if (args.documentUploadDefaultKind)
    model.documentUploadDefaultKind = args.documentUploadDefaultKind;

  // intermediate dialog formId
  if (args.intermediateDialogFormId) model.intermediateDialogFormId = args.intermediateDialogFormId;

  var icase_documents_permissions = getPermission(args.nodeRef, "icase:documents");
  model.documentUploadPermission = icase_documents_permissions ? icase_documents_permissions.editable : false;

})();

function jsonGet(url) {
    var result = remote.connect("alfresco").get(url);

    try {
        if (result.status == 200) {
            var json = eval('(' + result + ')');
            if (json != null) return json;
        }
    } catch (e) {
        return null;
    }

    return null;
}

function getNodeInfo(nodeRef) {
  var details = jsonGet('/citeck/node?nodeRef=' + nodeRef + "&props=tk:type,tk:kind&assocs=false&childAssocs=false&children=false");
  return details && details.props ? details.props : null;
}

function getPermission(nodeRef, field, mode) {
  return jsonGet('/citeck/permisions/is-field-editable?caseRef=' + nodeRef + (field ? '&field=' + field : '') + (mode ? '&mode=' + mode : ''));
}