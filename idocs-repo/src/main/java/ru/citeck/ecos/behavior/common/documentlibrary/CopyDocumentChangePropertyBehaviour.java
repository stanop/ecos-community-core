package ru.citeck.ecos.behavior.common.documentlibrary;

import org.alfresco.repo.copy.CopyServicePolicies.OnCopyCompletePolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.namespace.QName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CopyDocumentChangePropertyBehaviour implements OnCopyCompletePolicy {

    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private List<QName> classNames;
    private List<ChangedProperty> changedProperties;
    private ScriptService scriptService;

    public void init() {
        for (QName className : classNames) {
            policyComponent.bindClassBehaviour(OnCopyCompletePolicy.QNAME, className, new JavaBehaviour(this, "onCopyComplete"));
        }
    }

    @Override
    public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {
        for (ChangedProperty changedProperty : changedProperties) {
            if (nodeService.exists(targetNodeRef) && conditionSatisfied(changedProperty, targetNodeRef)) {
                nodeService.setProperty(targetNodeRef, changedProperty.getPropName(), changedProperty.getNewPropValue());
            }
        }
    }

    private boolean conditionSatisfied(final ChangedProperty property, NodeRef target) {
        final Map<String,Object> model = new HashMap<>(1);
        model.put(KEY_NODE, target);

        return AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Boolean>() {
            @Override
            public Boolean doWork() throws Exception {
                return String.valueOf(scriptService.executeScriptString(property.getCondition(), model)).equals("true");
            }
        });

    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setClassNames(List<QName> classNames) {
        this.classNames = classNames;
    }

    public void setChangedProperties(List<ChangedProperty> changedProperties) {
        this.changedProperties = changedProperties;
    }

    public void setScriptService(ScriptService scriptService) {
        this.scriptService = scriptService;
    }

    public static class ChangedProperty {
        private String condition;
        private QName propName;
        private String newPropValue;


        public void setCondition(String condition) {
            this.condition = condition;
        }

        public void setPropName(QName propName) {
            this.propName = propName;
        }

        public void setNewPropValue(String newPropValue) {
            this.newPropValue = newPropValue;
        }

        public String getCondition() {
            return condition;
        }

        public QName getPropName() {
            return propName;
        }

        public String getNewPropValue() {
            return newPropValue;
        }
    }

    private static final String KEY_NODE = "node";
}
