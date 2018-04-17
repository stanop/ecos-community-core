<script type="text/javascript">//<![CDATA[
    Alfresco.ActivitiTransitions.prototype._generateTransitionButton = function(transition) {
        var self = this;

        $("#" + this.id + "-buttons").append(
            $("<INPUT>", {
                "id": this.id + "-" + transition.id,
                "value": transition.label,
                "type": "button"
            })
        );

        // create the YUI button and register the event handler
        var button = Alfresco.util.createYUIButton(this, transition.id, function(e, p_obj) {
            var btn = this;
            var nodeRef = Citeck.utils.getURLParameterByName("nodeRef"),
                searchUrl = Alfresco.constants.PROXY_URI + 'citeck/get-document-package?nodeRef=' + nodeRef;

            var request = new XMLHttpRequest();

            request.open('GET', searchUrl, false);
            request.send(null);
            if (request.status === 200) {
                if (request.responseText) {
                    var nodeRefs = eval('(' + request.responseText + ')');
                    if (nodeRefs && nodeRefs.length > 0) {
                        btn.nodesToSign = nodeRefs;
                        btn.signedNodes = [];
                        if (!this.message_listener) {
                            window.addEventListener("message", function(event) {
                                if (typeof event.data != 'string') {
                                    return;
                                }
                                var groups = /^sign_completed_(.*)$/gi.exec(event.data);
                                if (groups && groups.length > 0) {
                                    var signedRef = groups[1];
                                    if (btn.nodesToSign.indexOf(signedRef) >= 0) {
                                        btn.signedNodes.push(signedRef);
                                        if (btn.signedNodes.length >= btn.nodesToSign.length) {
                                            self.onClick(e, p_obj);
                                        }
                                    }
                                }
                            });
                            this.message_listener = true;
                        }
                        documentsSignMetadataRefresh(nodeRefs, false);
                    } else {
                        self.onClick(e, p_obj);
                    }
                } else {
                    alert("Can't get documents to sign");
                }
            } else {
                alert(request.statusText);
            }
        });

        // register the button as a submitElement with the forms runtime instance
        YAHOO.Bubbling.fire("addSubmitElement", button);
    }

//]]>
</script>

<#import "/ru/citeck/components/form/ftl-forms.lib.ftl" as forms />
<@forms.renderFormsRuntime formId=formId />

<@formLib.renderFormContainer formId=formId>
    <div style="float: left"><@forms.renderField field="prop_bpm_description" extension=extensions.controls.info /></div>

    <#if form.fields["prop_cwf_isOptionalTask"].value><span>${msg("workflowtask.field.cwf_isOptionalTask")}</span></#if>
    <#include "../common/task-plain-response.ftl" />
</@>
