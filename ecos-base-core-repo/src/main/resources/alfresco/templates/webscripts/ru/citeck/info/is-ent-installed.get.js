(function () {
    var moduleService = services.get("moduleService");
    var eSignInstalled = (moduleService.getModule("ecos-esign-repo") != null);
    var diadocInstalled = (moduleService.getModule("ecos-diadoc-repo") != null);
    var isEntInstalled = (eSignInstalled && diadocInstalled);

    model.isEntInstalled = isEntInstalled;
})();