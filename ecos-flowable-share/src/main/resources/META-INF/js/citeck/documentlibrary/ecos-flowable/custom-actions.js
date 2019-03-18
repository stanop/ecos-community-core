(function() {

    YAHOO.Bubbling.fire("registerAction", { actionName: "onActionDeployBpmProcess",
        fn: function (record) {

            var showError = function(e) {

                Alfresco.util.PopupManager.displayMessage({
                    text: Alfresco.util.message('actions.deploy-ecos-bpm-process.failure'),
                    displayTime: 5
                });
                console.log(e);
            };

            fetch(Alfresco.constants.PROXY_URI + 'citeck/ecos/bpm/process/deploy?nodeRef=' + record.nodeRef, {
                method: 'POST',
                credentials: 'include'
            }).then(function(resp) {

                if (resp.status === 200) {

                    Alfresco.util.PopupManager.displayMessage({
                        text: Alfresco.util.message('actions.deploy-ecos-bpm-process.success'),
                        displayTime: 5
                    });
                } else {
                    showError(resp);
                }
            }).catch(function(error) {
                showError(error);
            });
        }
    });

})();