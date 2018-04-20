(function() {
if(!document.hasPermission("Delete")) return;
if(document.isContainer) return;
if(!document.parent.isContainer) return;

var ecosConfigService = services.get("ecosConfigService");
var moveToCreatorDirectory = ecosConfigService.getParamValue("contractsFolderPermissionConfig") == "true";

var moduleService = services.get("moduleService");
var ecosEntInstalled = (moduleService.getModule("ecos-enterprise-repo"));

if (moveToCreatorDirectory && ecosEntInstalled) {
    var userName = document.properties.creator;
    var directory = space.childByNamePath(userName);
    if(!directory) {
        directory = space.createFolder(userName);
        directory.setInheritsPermissions(false);
        directory.setPermission("NodeManager", userName);

        var managerGroup = document.parent.properties["idocs:managerGroup"];
        var managerPermission = document.parent.properties["idocs:managerPermission"];
        if(managerGroup && managerPermission)
        {
            directory.setPermission(managerPermission, managerGroup);
        }
    }
    document.move(directory);
}
})();