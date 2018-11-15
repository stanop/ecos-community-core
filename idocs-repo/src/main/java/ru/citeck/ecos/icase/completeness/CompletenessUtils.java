package ru.citeck.ecos.icase.completeness;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.Collection;
import java.util.Set;

@Component
public class CompletenessUtils {

    private static final String LEVEL_ERROR_MSG = "requirement.message.business-requirements-not-completed";

    private NodeService nodeService;
    private CaseCompletenessService caseCompletenessService;

    public void assertLevelsCompleted(NodeRef caseRef, Collection<NodeRef> levels) {

        Set<NodeRef> uncompletedLevels = caseCompletenessService.getUncompletedLevels(caseRef, levels);

        if (!uncompletedLevels.isEmpty()) {

            StringBuilder levelsMsg = new StringBuilder();
            uncompletedLevels.forEach(node -> {
                String levelTitle = RepoUtils.getProperty(node, ContentModel.PROP_TITLE, nodeService);
                levelsMsg.append("\n");
                levelsMsg.append(levelTitle);
            });

            String exceptionMsg = "\n" + String.format(I18NUtil.getMessage(LEVEL_ERROR_MSG), levelsMsg.toString());
            throw new LevelsNotCompletedException(uncompletedLevels, exceptionMsg);
        }
    }

    @Autowired
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Autowired
    @Qualifier("caseCompletenessService")
    public void setCaseCompletenessService(CaseCompletenessService caseCompletenessService) {
        this.caseCompletenessService = caseCompletenessService;
    }
}
