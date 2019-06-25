(function () {

    if (!args.key) {
        throw "key is a mandatory parameter";
    }

    importPackage(Packages.org.alfresco.repo.admin.registry);

    var key = new RegistryKey(null, ["ecos", "cardlets", args.key]);
    var registry = services.get('registryService');

    var value = null;
    Packages.org.alfresco.repo.security.authentication.AuthenticationUtil.runAsSystem(function () {
        value = registry.getProperty(key);
    });

    model.result = value;

})();