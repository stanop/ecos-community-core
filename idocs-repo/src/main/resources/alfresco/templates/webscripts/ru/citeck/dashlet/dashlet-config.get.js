(function () {

    if (!args.key) {
        throw "key is a mandatory parameter";
    }

    importPackage(Packages.org.alfresco.repo.admin.registry);

    var key = new RegistryKey(null, ["ecos", "cardlets", args.key]);
    var registry = services.get('registryService');

    var value = registry.getProperty(key);

    if (!value) {
        status.code = 404;
        model.result = null;
        return;
    }

    model.result = value;

})();