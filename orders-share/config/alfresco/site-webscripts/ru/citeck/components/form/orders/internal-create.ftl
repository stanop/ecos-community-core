<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />

<@forms.setMandatoryFields
fieldNames = [
    "prop_tk_kind",
    "assoc_idocs_signatory",
    "prop_orders_header",
    "assoc_orders_fileCode",
    "prop_idocs_summary",
    "assoc_idocs_initiator",
    "assoc_orders_branch"
]/>

<@forms.fileUploadSupport />

<#if formUI == "true">
    <@formLib.renderFormsRuntime formId=formId />
</#if>


<@formLib.renderFormContainer formId=formId>

    <@forms.renderField field="prop_tk_kind" />

    <@forms.renderField field="prop_orders_creationDate" extension = {
        "disabled": false,
        "control" : {
            "template" : "/ru/citeck/components/form/controls/date.ftl",
            "params": {
                "showTime": "false",
                "appendDaysToCurrentValue" : 0
            }
        }
    }/>
    
    <@forms.renderField field="assoc_idocs_signatory" extension = {
        "control": {
            "template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
            "params": {
                "searchQuery" : "user=true&default=false"
            }
        }
    }/>

    <script type="text/javascript">// <![CDATA[
    (function() {
        var onInitiatorControlReady = function(scope, args) {
            if (args && args.length > 1 && args[1]) {
                var event = args[1],
                        initiatorCtrlId = "${args.htmlid}_assoc_idocs_initiator-cntrl";
                if (event.eventGroup && event.eventGroup.id == initiatorCtrlId) {
                    var initiatorCtrl = Alfresco.util.ComponentManager.get(initiatorCtrlId);
                    if (initiatorCtrl) {
                        var uri = Alfresco.constants.PROXY_URI_RELATIVE + "api/orgstruct/authority/" + Alfresco.constants.USERNAME;
                        Alfresco.util.Ajax.jsonGet({
                            url: uri,
                            successCallback: {
                                scope: this,
                                fn: function(response) {
                                    if (response && response.json && response.json.nodeRef) {
                                        initiatorCtrl.selectItems(response.json.nodeRef);
                                    }
                                    else {
                                        Alfresco.util.PopupManager.displayMessage({
                                            text: Alfresco.util.message('internal.form.get.initiator.failed')
                                        });
                                    }
                                }
                            },
                            failureCallback: {
                                scope: this,
                                fn: function() {
                                    Alfresco.util.PopupManager.displayMessage({
                                        text: Alfresco.util.message('internal.form.get.initiator.failed')
                                    });
                                }
                            }
                        });
                    }
                }
            }
        };
        YAHOO.Bubbling.on("objectFinderReady", onInitiatorControlReady, this);
    })();


    // ]]></script>

    <@forms.renderField field="assoc_idocs_initiator" extension = {
        "control": {
            "template": "/ru/citeck/components/form/controls/orgstruct-select.ftl",
            "params": {
                "searchQuery" : "user=true&default=false"
            }
        },
        "disabled": false
    }/>

    <script type="text/javascript">// <![CDATA[
        (function() {
            var onBranchControlReady = function(scope, args) {
            if (args && args.length > 1 && args[1]) {
                var event = args[1],
                        branchCtrlId = "${args.htmlid}_assoc_orders_branch-cntrl";
                if (event.eventGroup && event.eventGroup.id == branchCtrlId) {
                    var branchCtrl = Alfresco.util.ComponentManager.get(branchCtrlId);
                    if (branchCtrl) {
                        var uri = Alfresco.constants.PROXY_URI_RELATIVE + "api/deputy/currentUserBranches";
                        Alfresco.util.Ajax.jsonGet({
                            url: uri,
                            successCallback: {
                                scope: this,
                                fn: function(response) {
                                    if (response && response.json && response.json.length > 0) {
                                        branchCtrl.selectItems(response.json[0].nodeRef);
                                    }
                                    else {
                                        Alfresco.util.PopupManager.displayMessage({
                                            text: Alfresco.util.message('internal.form.get.branch.failed')
                                        });
                                    }
                                }
                            },
                            failureCallback: {
                                scope: this,
                                fn: function() {
                                    Alfresco.util.PopupManager.displayMessage({
                                        text: Alfresco.util.message('internal.form.get.branch.failed')
                                    });
                                }
                            }
                        });
                    }
                }
            }
        };
        YAHOO.Bubbling.on("objectFinderReady", onBranchControlReady, this);
    })();
    // ]]></script>

    <@forms.renderField field="assoc_orders_branch" extension = {
        "disabled": false
    }/>

    <@forms.renderField field="prop_orders_header" />

    <@forms.renderField field="prop_idocs_summary" extension = {
        "control": {
            "template": "/org/alfresco/components/form/controls/textarea.ftl",
            "params": {}
        }
    }/>

    <@forms.renderField field="assoc_orders_fileCode" />
    
    <@forms.renderField field="prop_dms_updateContent" extension = {
        "force" : true,
        "visible" : "create",
        "value" : false,
        "control" : {
            "template" : "/org/alfresco/components/form/controls/checkbox.ftl",
            "params" : {}
        }
    }/>
    <@forms.displayConditional "prop_dms_updateContent" "false">
        <@forms.renderField field="prop_cm_content" extension = {
            "label": msg("internal.form.attachment"),
            "mandatory": true,
            "control": {
                "template": "/ru/citeck/components/form/controls/fileUpload.ftl"
            }
        } />
    </@>
</@>
