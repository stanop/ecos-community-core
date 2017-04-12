(function() {

	var autoDeployerBeans = {
		'journals': 'journals.autoDeployer',
		'views': 'nodeViews.autoDeployer',
		'invariants': 'invariants.autoDeployer.module',
		'case-templates': 'caseAutoDeployer'
	};

    var resetStatus = {};

	for (var key in autoDeployerBeans) {
        if (json.has(key) && Packages.java.lang.Boolean.TRUE.equals(json.get(key))) {
            var bean = services.get(autoDeployerBeans[key]);
            if (bean) {
                bean.load();
                resetStatus[key] = 'OK';
            } else {
                resetStatus[key] = 'BEAN_NOT_FOUND';
            }
        }
    }

    model.resetStatus = resetStatus;
})();

