package ru.citeck.ecos.flowable.temp;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.service.ServiceDescriptorRegistry;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.flowable.engine.delegate.DelegateTask;
import org.flowable.engine.delegate.TaskListener;
import org.json.JSONException;
import ru.citeck.ecos.confirm.ConfirmService;
import ru.citeck.ecos.providers.ApplicationContextProvider;
import ru.citeck.ecos.service.CiteckServices;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Add version task listener
 */
public class FlowableAddConsiderableVersion implements TaskListener {

    private ConfirmService confirmService;
    private NamespaceService namespaceService;
    private NodeService nodeService;

    private void initServices() {
        ServiceRegistry services = ApplicationContextProvider.getBean(ServiceDescriptorRegistry.class);;
        confirmService = (ConfirmService) services.getService(CiteckServices.CONFIRM_SERVICE);
        namespaceService = services.getNamespaceService();
        nodeService = services.getNodeService();

    }

    @Override
    public void notify(DelegateTask delegateTask) {
        if(delegateTask.getAssignee() == null) {
            return;
        }

        initServices();
        WorkflowQNameConverter qNameConverter = new WorkflowQNameConverter(namespaceService);
        NodeRef packageRef = ((ScriptNode) delegateTask
                .getVariable(qNameConverter.mapQNameToName(WorkflowModel.ASSOC_PACKAGE))).getNodeRef();
        Set<QName> includeQNames = new HashSet<QName>();
        includeQNames.add(WorkflowModel.ASSOC_PACKAGE_CONTAINS);
        includeQNames.add(ContentModel.ASSOC_CONTAINS);
        List<ChildAssociationRef> documentRefs = nodeService.getChildAssocs(packageRef);
        for (ChildAssociationRef documentRef : documentRefs) {
            if(!includeQNames.contains(documentRef.getTypeQName()) || documentRef.getChildRef() == null) {
                continue;
            }
            try {
                confirmService.addCurrentVersionToConsiderable(delegateTask.getAssignee(), documentRef.getChildRef());
            } catch (JSONException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }
}
