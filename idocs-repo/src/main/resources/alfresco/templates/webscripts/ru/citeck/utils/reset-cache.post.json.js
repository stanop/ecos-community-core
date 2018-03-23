(function() {

    var autoDeployerBeans = {
        'journals': 'journals.autoDeployer',
        'views': 'nodeViews.autoDeployer',
        'invariants': 'invariants.autoDeployer.module',
        'case-templates': 'caseAutoDeployer',
        'fields-perm-matrix': 'permissionsMatrixDeployer',
        'perm-matrix': 'docACLDeployer'
    };

    var additionalActions = {
        'journals': function () {
            services.get('journalService').clearCache();
            services.get('webscript.ru.citeck.journals2.records.records.post').clearCache();
            services.get('webscript.ru.citeck.journals2.create-variants.get').clearCache();
        }
    };

    var resetStatus = {};

    for (var key in autoDeployerBeans) {
        if (json.has(key) && Packages.java.lang.Boolean.TRUE.equals(json.get(key))) {
            var bean = services.get(autoDeployerBeans[key]);
            if (bean) {
                bean.load();
                if (additionalActions[key]) {
                    additionalActions[key]();
                }
                resetStatus[key] = 'OK';
            } else {
                resetStatus[key] = 'BEAN_NOT_FOUND';
            }
        }
    }

    model.resetStatus = resetStatus;
})();

