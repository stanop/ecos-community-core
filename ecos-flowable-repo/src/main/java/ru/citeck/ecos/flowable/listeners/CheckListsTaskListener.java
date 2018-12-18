package ru.citeck.ecos.flowable.listeners;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.lang.StringUtils;
import org.flowable.engine.common.api.delegate.Expression;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.variable.api.delegate.VariableScope;
import ru.citeck.ecos.flowable.example.AbstractServiceProvider;
import ru.citeck.ecos.flowable.utils.FlowableListenerUtils;
import ru.citeck.ecos.icase.completeness.CompletenessUtils;
import ru.citeck.ecos.providers.ApplicationContextProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CheckListsTaskListener extends AbstractServiceProvider implements TaskListener {

    private static final String SPACES_STORE_PREFIX = "workspace://SpacesStore/";

    private CompletenessUtils completenessUtils;
    private NodeService nodeService;

    private final List<NodeRef> lists = new ArrayList<>();
    private final List<String> outcomesToCheck = new ArrayList<>();
    private String outcomeField = "outcome";
    private Expression checkEnabled;

    @Override
    protected void initImpl() {
        this.nodeService = serviceRegistry.getNodeService();
        this.completenessUtils = ApplicationContextProvider.getBean(CompletenessUtils.class);
    }

    @Override
    public void notify(DelegateTask delegateTask) {

        init();

        boolean isEnabled = processEnabledState(delegateTask);

        if (!isEnabled || lists.isEmpty()) {
            return;
        } else if (!outcomesToCheck.isEmpty()) {
            String outcome = (String) delegateTask.getVariable(outcomeField);
            if (outcome == null || !outcomesToCheck.contains(outcome)) {
                return;
            }
        }

        NodeRef document = FlowableListenerUtils.getDocument(delegateTask, nodeService);
        if (document == null || !nodeService.exists(document)) {
            return;
        }

        if (lists.isEmpty()) {
            throw new IllegalArgumentException("Variable levels is not set");
        }

        completenessUtils.assertLevelsCompleted(document, lists);
    }

    private boolean processEnabledState(VariableScope variableScope) {
        if (checkEnabled == null) {
            return Boolean.TRUE;
        }

        final String expText = checkEnabled.getExpressionText();
        if (Boolean.TRUE.toString().equals(expText) || Boolean.FALSE.toString().equals(expText)) {
            return Boolean.valueOf(expText);
        }

        return (boolean) checkEnabled.getValue(variableScope);
    }

    public void setCheckEnabled(Expression checkEnabled) {
        this.checkEnabled = checkEnabled;
    }

    public void setOutcomeField(Expression value) {
        this.outcomeField = value != null ? value.getExpressionText() : "";
    }

    public void setLists(Expression value) {
        this.lists.clear();
        fillNodeRefs(this.lists, value.getExpressionText());
    }

    public void setOutcomesToCheck(Expression values) {
        outcomesToCheck.clear();
        String[] outcomes = values.getExpressionText().split(",");
        for (String outcome : outcomes) {
            if (StringUtils.isNotBlank(outcome)) {
                outcomesToCheck.add(outcome);
            }
        }
    }

    public static void fillNodeRefs(Collection<NodeRef> result, Object data) {

        if (data == null) {
            return;
        }

        if (data instanceof NodeRef) {

            result.add((NodeRef) data);

        } else if (data instanceof String) {

            String[] dataStr = ((String) data).split(",");

            if (dataStr.length < 2 && StringUtils.isBlank(dataStr[0])) {
                return;
            }

            for (String strNodeRef : dataStr) {

                if (!strNodeRef.startsWith(SPACES_STORE_PREFIX)) {
                    strNodeRef = SPACES_STORE_PREFIX + strNodeRef;
                }

                result.add(new NodeRef(strNodeRef));
            }

        } else if (data instanceof Collection) {

            for (Object obj : (Collection) data) {
                fillNodeRefs(result, obj);
            }

        } else if (data instanceof String[]) {

            for (Object str : (String[]) data) {
                fillNodeRefs(result, str);
            }
        }
    }
}
