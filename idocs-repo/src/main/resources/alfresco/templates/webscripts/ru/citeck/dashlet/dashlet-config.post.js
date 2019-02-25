(function () {

    if (!args.key) {
        throw "key is a mandatory parameter";
    }

    importPackage(Packages.org.alfresco.repo.admin.registry);

    var key = new RegistryKey(null, ["ecos", "cardlets", args.key]);
    var registry = services.get('registryService');

    registry.addProperty(key, requestbody.getContent());

    model.result = {
        success: true
    };
})();