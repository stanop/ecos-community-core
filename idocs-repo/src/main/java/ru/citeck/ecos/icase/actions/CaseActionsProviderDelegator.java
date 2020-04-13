package ru.citeck.ecos.icase.actions;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.action.node.NodeActionDefinition;
import ru.citeck.ecos.action.node.NodeActionsProvider;
import ru.citeck.ecos.action.node.NodeActionsService;
import ru.citeck.ecos.icase.activity.dto.CaseServiceType;
import ru.citeck.ecos.icase.activity.service.ActivityCommonService;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class CaseActionsProviderDelegator extends NodeActionsProvider {

    private ActivityCommonService activityCommonService;
    private NodeActionsService nodeActionsService;
    private AlfCaseActionsProvider alfCaseActionsProvider;
    private EProcCaseActionsProvider eprocCaseActionsProvider;

    @Autowired
    public CaseActionsProviderDelegator(ActivityCommonService activityCommonService,
                                        AlfCaseActionsProvider alfCaseActionsProvider,
                                        EProcCaseActionsProvider eprocCaseActionsProvider,
                                        NodeActionsService nodeActionsService) {
        this.activityCommonService = activityCommonService;
        this.alfCaseActionsProvider = alfCaseActionsProvider;
        this.eprocCaseActionsProvider = eprocCaseActionsProvider;
        this.nodeActionsService = nodeActionsService;
    }

    @PostConstruct
    public void init() {
        this.nodeActionsService.addActionProvider(this);
    }

    @Override
    public List<NodeActionDefinition> getNodeActions(NodeRef caseRef) {
        CaseServiceType caseType = activityCommonService.getCaseType(caseRef);
        if (caseType == CaseServiceType.ALFRESCO) {
            return alfCaseActionsProvider.getCaseActions(caseRef);
        } else {
            return eprocCaseActionsProvider.getCaseActions(caseRef);
        }
    }

}
