package ru.citeck.ecos.flowable.bpm;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import ru.citeck.ecos.workflow.EcosBpmAppModelUtils;

public class DeployBpmProcessPost extends AbstractWebScript {

    private static final String PARAM_NODE_REF = "nodeRef";

    private EcosBpmAppModelUtils ecosBpmAppModelUtils;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) {

        String nodeRefStr = req.getParameter(PARAM_NODE_REF);
        ParameterCheck.mandatoryString(PARAM_NODE_REF, nodeRefStr);

        ecosBpmAppModelUtils.deployProcess(new NodeRef(nodeRefStr));

        res.setStatus(Status.STATUS_OK);
    }

    @Autowired
    public void setEcosBpmAppModelUtils(EcosBpmAppModelUtils ecosBpmAppModelUtils) {
        this.ecosBpmAppModelUtils = ecosBpmAppModelUtils;
    }
}
