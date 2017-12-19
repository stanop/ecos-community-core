function main() {

    if (!args.nodeRef) return;

    var response = remote.call('/citeck/activity-create-variants');
    var variants = eval('(' + response + ')');

    model.createMenu = formatMenu(variants, null, {});
}

function formatMenu(variants, parent, menuRoot) {

    var getSubMenu = function (variant) {
        var subMenu = menuRoot[variant.id];
        if (!subMenu) {
            subMenu = {
                title: variant.title,
                variants: []
            };
            menuRoot[variant.id] = subMenu;
        }
        return subMenu;
    };

    for (var i = 0; i < variants.length; i++) {

        var variant = variants[i];
        if (variant.canBeCreated) {
            var subMenu = getSubMenu(parent || variant);
            subMenu.variants.push(variant);
        }

        formatMenu(variant.children, variant, menuRoot);
    }

    return menuRoot;
}

main();