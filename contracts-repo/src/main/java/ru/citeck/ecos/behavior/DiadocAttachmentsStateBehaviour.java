package ru.citeck.ecos.behavior;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.model.SecurityModel;
import ru.citeck.ecos.model.IdocsModel;
import ru.citeck.ecos.utils.RepoUtils;

public class DiadocAttachmentsStateBehaviour extends DiadocAttachmentsStatusBehaviour<Object> {

    @Override
    protected void processTransition(NodeRef parentRef, NodeRef attachmentRef, Transition transition, Object data) {
        String state = getState(attachmentRef, transition);
        nodeService.setProperty(attachmentRef, IdocsModel.PROP_ATTACHMENT_STATE, state);
    }

    private String getState(NodeRef attachmentRef, Transition transition) {
        Boolean shouldBeSigned = RepoUtils.getProperty(attachmentRef, SecurityModel.PROP_SHOULD_BE_SIGNED, Boolean.class, nodeService);
        String state = "";
        String from = transition.from != null ? transition.from : "";
        String to = transition.to != null ? transition.to : "";
        switch (to) {
            case SecurityModel.PKG_ATMNT_STATUS_RECEIVED:
                if (shouldBeSigned != null && shouldBeSigned) {
                    state = IdocsModel.CONSTR_SIGN_REQUIRED;
                } else {
                    state = IdocsModel.CONSTR_RECEIVED;
                }
                break;
            case SecurityModel.PKG_ATMNT_STATUS_SIGNED:
            case SecurityModel.PKG_ATMNT_STATUS_BUYER_TITLE_SIGNED:
                if (from.equals(SecurityModel.PKG_ATMNT_STATUS_SENT)) {
                    state = IdocsModel.CONSTR_COUNTERPARTY_SIGNED;
                } else {
                    state = IdocsModel.CONSTR_SIGNED;
                }
                break;
            case SecurityModel.PKG_ATMNT_STATUS_SENT:
                state = IdocsModel.CONSTR_COUNTERPARTY_SIGN_REQUESTED;
                break;
            case SecurityModel.PKG_ATMNT_STATUS_DELIVERY_FAILED:
                state = IdocsModel.DELIVERY_FAILED;
                break;
            case SecurityModel.PKG_ATMNT_STATUS_REQUESTS_MY_REVOCATION:
                state = IdocsModel.REQUESTS_MY_REVOCATION;
                break;
            case SecurityModel.PKG_ATMNT_STATUS_REVOCATION_IS_REQUESTED_BY_ME:
                state = IdocsModel.REVOCATION_IS_REQUESTED_BY_ME;
                break;
            case SecurityModel.PKG_ATMNT_STATUS_REVOCATION_ACCEPTED:
                state = IdocsModel.REVOCATION_ACCEPTED;
                break;
            case SecurityModel.PKG_ATMNT_STATUS_REVOCATION_REJECTED:
                state = IdocsModel.REVOCATION_REJECTED;
                break;
            case SecurityModel.PKG_ATMNT_STATUS_REJECTION_SENT:
                state = IdocsModel.REJECTION_SENT;
                break;
            case SecurityModel.PKG_ATMNT_STATUS_REJECTED:
                state = IdocsModel.REJECTED;
                break;
            case SecurityModel.PKG_ATMNT_STATUS_REVISIONED:
                state = IdocsModel.REVISIONED;
                break;
            case SecurityModel.PKG_ATMNT_STATUS_CORRECTED:
                state = IdocsModel.CORRECTED;
                break;
            case SecurityModel.PKG_ATMNT_STATUS_REVISION_CORRECTED:
                state = IdocsModel.REVISION_CORRECTED;
                break;
        }

        return state;
    }
}
