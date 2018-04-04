package ru.citeck.ecos.behavior;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.SecurityModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DiadocContractNotificationBehaviour extends DiadocAttachmentsNotificationBehaviour {

    @Override
    protected void onUpdatePropertiesImpl(NodeRef attachmentRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
        String statusBefore = (String) before.get(SecurityModel.PROP_PACKAGE_STATUS_COPY);
        String statusAfter = (String) after.get(SecurityModel.PROP_PACKAGE_STATUS_COPY);

        if (nodeService.exists(attachmentRef) && !Objects.equals(statusBefore, statusAfter)) {
            Transition transition = getActualTransition(statusBefore, statusAfter);
            if (transition != null) {
                processTransition(attachmentRef, attachmentRef, transition, getTransitionData(transition));
            }
        }
    }

    @Override
    protected void processTransition(NodeRef parentRef, NodeRef attachmentRef, Transition transition, EventData data) {
        super.processTransition(attachmentRef, attachmentRef, transition, data);
    }

    @Override
    protected Map<String, Serializable> prepareArgs(NodeRef parentRef, NodeRef attachmentRef, Transition transition, EventData data) {
        Map<String, Serializable> args = new HashMap<>();
        args.put(ARG_DOCUMENT, attachmentRef);
        args.put(ARG_ATTACHMENT, attachmentRef);
        args.put(ARG_STATUS_TRANSITION, transition);
        args.put(ARG_EVENT_DATA, data);
        return args;
    }
}
