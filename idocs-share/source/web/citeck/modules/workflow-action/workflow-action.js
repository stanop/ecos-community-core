//YAHOO.util.Event.onDOMReady(function() {

//if (Alfresco.component.WorkflowDetailsActions) {
(function(){

    Alfresco.component.WorkflowControlAction = function(htmlId) {
        Alfresco.component.WorkflowControlAction.superclass.constructor.call(this, htmlId);
    }

    YAHOO.extend(Alfresco.component.WorkflowControlAction, Alfresco.component.WorkflowDetailsActions);

    function getUserGroups(userName) {
        var userGroups = [];

        var url = YAHOO.lang.substitute(window.location.origin +
            "/alfresco/service/api/people/{user}?groups=true", {
            user: userName
        });

        Alfresco.util.Ajax.jsonGet({
            url: url,
            successCallback: { fn: function(response) {
                userGroups = response.json.groups;
            }},
            failureCallback: { fn: function(response) {
                logger.error("Unable to get user groups for " + userName + ".\n" + response);
                userGroups = [];
            }}
        });

        return userGroups;
    };

    function recognizeIsAdmin() {
        return this.userGroups && YAHOO.lang.isArray(this.userGroups)
            ? Alfresco.util.arrayIndex(this.userGroups, 'GROUP_ALFRESCO_ADMINISTRATORS', 'itemName') > -1
            : false;
    }

    /*Alfresco.component.WorkflowDetailsActions.prototype.onWorkflowFormReady = function WDA_onWorkflowFormReady(layer, args)
     {*/
    if (!this.userGroups) {
        var userName = Alfresco.constants.USERNAME;
        this.userGroups = getUserGroups(userName);
    }

    var isWorkflowActive = this.workflow.isActive;
    var isInitiator = this.workflow.initiator.userName == Alfresco.constants.USERNAME;
    var isAdmin = recognizeIsAdmin();

    if ((isInitiator || isAdmin) && isWorkflowActive) {
        Dom.removeClass(this.id + "-cancel-button", "hidden");
    }

    if (isInitiator && !isWorkflowActive) {
        Dom.removeClass(this.id + "-delete-button", "hidden");
    }
    //};


}
//});
)();