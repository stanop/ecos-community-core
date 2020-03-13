(function() {

    var autoDeployerBeans = {
        'ecos-apps': 'ecosAppsModulesProviderImpl',
        'journals': 'journals.autoDeployer',
        'views': 'nodeViews.autoDeployer',
        'invariants': 'invariants.autoDeployer.module',
        'case-templates': 'caseAutoDeployer',
        'fields-perm-matrix': 'permissionsMatrixDeployer',
        'perm-matrix': 'docACLDeployer',
        'cardlets': 'ecos.cardlets.contentDeployer',
        'forms': 'ecos.eform.contentDeployer'
    };

    var customActions = {
        'ecos-apps': function (bean) {
            bean.update();
        }
    };

    var additionalActions = {
        'journals': function () {
            services.get('journalService').clearCache();
            services.get('webscript.ru.citeck.journals2.create-variants.get').clearCache();
        },
        'cardlets': function () {
            services.get('ecos.cardlets.cardletsRegistry').clearCache();
        }
    };

    var resetStatus = {};

    for (var key in autoDeployerBeans) {
        if (json.has(key) && Packages.java.lang.Boolean.TRUE.equals(json.get(key))) {
            var bean = services.get(autoDeployerBeans[key]);
            if (bean) {
                if (customActions[key]) {
                    customActions[key](bean);
                } else {
                    bean.load();
                    if (additionalActions[key]) {
                        additionalActions[key]();
                    }
                }
                resetStatus[key] = 'OK';
            } else {
                resetStatus[key] = 'BEAN_NOT_FOUND';
            }
        }
    }

    model.resetStatus = resetStatus;
})();

