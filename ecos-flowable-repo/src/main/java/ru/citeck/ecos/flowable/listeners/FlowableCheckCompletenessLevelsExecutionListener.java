package ru.citeck.ecos.flowable.listeners;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.flowable.example.AbstractExecutionListener;
import ru.citeck.ecos.flowable.utils.FlowableListenerUtils;
import ru.citeck.ecos.icase.completeness.CaseCompletenessService;
import ru.citeck.ecos.icase.completeness.CaseCompletenessServiceImpl;
import ru.citeck.ecos.providers.ApplicationContextProvider;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Roman Makarskiy
 */
public class FlowableCheckCompletenessLevelsExecutionListener extends AbstractExecutionListener {

    private static final Log logger = LogFactory.getLog(FlowableCheckCompletenessLevelsExecutionListener.class);

    private static final String REQUIREMENTS_ERROR_MESSAGE = "requirement.message.business-requirements-not-completed";
    private static final String SEPARATOR = ";";

    private CaseCompletenessService caseCompletenessService;
    private NodeService nodeService;

    private Expression completenessLevels;

    @Override
    protected void initImpl() {
        this.nodeService = serviceRegistry.getNodeService();
        this.caseCompletenessService = ApplicationContextProvider.getBean("caseCompletenessService",
                CaseCompletenessServiceImpl.class);
    }

    @Override
    protected void notifyImpl(DelegateExecution execution) {
        NodeRef document = FlowableListenerUtils.getDocument(execution, nodeService);
        if (document == null || !nodeService.exists(document)) {
            return;
        }

        if (completenessLevels == null) {
            throw new IllegalArgumentException("Variable completenessLevels can not be null");
        }

        CompletenessValidator validator = new CompletenessValidator(document, completenessLevels, nodeService);
        validator.checkLevels();
    }

    public void setCompletenessLevels(Expression completenessLevels) {
        this.completenessLevels = completenessLevels;
    }

    private class CompletenessValidator {
        private NodeService nodeService;
        private NodeRef document;
        private List<NodeRef> levels = new ArrayList<>();

        CompletenessValidator(NodeRef document, Expression completeness, NodeService nodeService) {
            this.nodeService = nodeService;
            this.document = document;

            String levelsStr = completeness != null ? completeness.getExpressionText() : "";
            if (StringUtils.isNotBlank(levelsStr)) {
                this.levels = Arrays.stream(levelsStr.split(SEPARATOR)).map(NodeRef::new).collect(Collectors.toList());
            }
        }

        void checkLevels() {
            if (logger.isDebugEnabled()) {
                logger.debug("Start check completeness. "
                        + "\n" + this.toString());
            }

            List<NodeRef> uncompleted = getUncompletedLevels(document);

            if (logger.isDebugEnabled()) {
                logger.debug("uncompleted: " + uncompleted);
            }

            if (uncompleted.isEmpty()) {
                return;
            }

            StringBuilder levelsMsg = new StringBuilder();
            uncompleted.forEach(node -> {
                String levelTitle = RepoUtils.getProperty(node, ContentModel.PROP_TITLE, nodeService);
                levelsMsg.append("\n");
                levelsMsg.append(levelTitle);
            });

            String exceptionMsg = "\n" + String.format(I18NUtil.getMessage(REQUIREMENTS_ERROR_MESSAGE), levelsMsg.toString());
            throw new CompletenessNotCompletedException(exceptionMsg);
        }

        private List<NodeRef> getUncompletedLevels(NodeRef document) {
            List<NodeRef> uncompleted = new ArrayList<>();

            for (NodeRef level : levels) {
                boolean levelCompleted = caseCompletenessService.isLevelCompleted(document, level);
                if (!levelCompleted) {
                    uncompleted.add(level);
                }
            }

            return uncompleted;
        }

        @Override
        public String toString() {
            return "CompletenessValidator{" +
                    "document=" + document +
                    ", levels=" + levels +
                    '}';
        }

        private class CompletenessNotCompletedException extends RuntimeException {
            CompletenessNotCompletedException(String message) {
                super(message);
            }
        }
    }
}
