//<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
//var datatype = AlfrescoUtil.param('datatype');
if (args.datatype == undefined || args.datatype.length == 0) {
    status.code = 400;
    status.message = "Parameter datatype is undefined or empty";
    status.redirect = true;
}
