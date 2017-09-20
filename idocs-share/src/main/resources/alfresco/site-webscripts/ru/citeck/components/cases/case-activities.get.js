function getCreateMenu(createVariants, groupLevel, typeNames) {
    
    // get main sub-menus
    var subMenus = {};
    for(var i in createVariants) {
        var variant = createVariants[i];
        var parentTypes = variant.parentTypes;
        var index = parentTypes.indexOf(groupLevel);
        var level = index == 0 ? variant.type :
                    index == -1 ? 'other' :
                    parentTypes[index - 1];
        var variants = subMenus[level];
        if(!variants) variants = subMenus[level] = [];
        variants.push(variant);
    }
    
    typeNames['other'] = "Other";
    
    var menu = [];
    for(var level in subMenus) {
        menu.push({
            id: level,
            title: typeNames[level],
            variants: subMenus[level]
        });
    }
    
    return menu;
}

(function() {
    if(!args.nodeRef) return;
    var baseType = "activ:activity";
    var response = remote.call('/citeck/invariants/create-views?type=' + baseType);
    var data = eval('(' + response + ')');
    var typeNames = data.typeNames;
    var createVariants = data.createVariants;
    
    model.createMenu = getCreateMenu(createVariants, baseType, typeNames);
})();