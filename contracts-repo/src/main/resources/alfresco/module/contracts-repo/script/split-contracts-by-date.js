(function() {

    if (!document.hasPermission("Delete")
      || document.typeShort != "contracts:agreement") {
        return;
    }

    var date = document.properties['cm:created'] || new Date();

    var year = (date.getFullYear()) + "";
    var month = (date.getMonth() + 1) + "";

    var parent = getFolder(space, [year, month]);
    makeUniqueName(parent, document);
    document.move(parent);

})();

function makeUniqueName(parent, node) {

    var baseName = node.properties['cm:name'];
    var currentParent = node.parent;
    var name = baseName;
    var counter = 1;
    
    var child = parent.childByNamePath(name);
    while (child) {
        name = baseName + " (" + counter + ")"; 
        counter++;
        child = parent.childByNamePath(name);
        if (!child) {
            child = currentParent.childByNamePath(name);
        }
    }
    
    if (baseName != name) {
        node.properties['cm:name'] = name;
        node.save();
    }
}

function getFolder(root, path) {
    var folder = root;
    for (var i = 0; i < path.length; i++) {
        folder = getOrCreateFolder(folder, path[i]);
    }
    return folder;
}

function getOrCreateFolder(parent, childName) {
    var result = parent.childByNamePath(childName);
    if (!result) {
        result = parent.createFolder(childName);
    }
    return result;
}