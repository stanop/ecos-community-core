package ru.citeck.ecos.flowable.utils;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.notification.EMailNotificationProvider;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.notification.NotificationContext;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import ru.citeck.ecos.notification.AbstractNotificationSender;
import ru.citeck.ecos.security.NodeOwnerDAO;

import java.io.Serializable;
import java.util.*;

/**
 * Flowable execution notification sender
 */
public class FlowableExecutionEntityNotificationSender extends AbstractNotificationSender<ExecutionEntity> {

    /**
     * Constants
     */
    private static final String FLOWABLE_ENGINE_PREFIX = "flowable$";
    private static final String DOCS_INFO_KEY = FlowableExecutionEntityNotificationSender.class.getName() + ".docsInfo";
    public static final String ARG_TASK = "task";
    public static final String ARG_TASK_ID = "id";
    public static final String ARG_TASK_NAME = "name";
    public static final String ARG_TASK_DESCRIPTION = "description";
    public static final String ARG_TASK_EDITOR = "editor";
    public static final String ARG_TASK_PROPERTIES = "properties";
    public static final String ARG_TASK_PROPERTIES_PRIORITY = "bpm_priority";
    public static final String ARG_TASK_PROPERTIES_DESCRIPTION = "bpm_description";
    public static final String ARG_TASK_PROPERTIES_DUEDATE = "bpm_dueDate";
    public static final String ARG_WORKFLOW = "workflow";
    public static final String ARG_WORKFLOW_ID = "id";
    public static final String ARG_WORKFLOW_PROPERTIES = "properties";
    public static final String ARG_WORKFLOW_DOCUMENTS = "documents";

    private Map<String, Map<String,List<String>>> taskSubscribers;
    protected WorkflowQNameConverter qNameConverter;
    protected PersonService personService;
    protected AuthenticationService authenticationService;
    protected boolean sendToOwner;
    private NodeOwnerDAO nodeOwnerDAO;
    private static final Log logger = LogFactory.getLog(FlowableExecutionEntityNotificationSender.class);
    public static final String ARG_MODIFIER = "modifier";
    List<String> allowDocList;
    Map<String, Map<String,String>> subjectTemplates;
    private TemplateService templateService;
    private String nodeVariable;
    private String templateEngine;

    @Override
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        super.setServiceRegistry(serviceRegistry);
        this.qNameConverter = new WorkflowQNameConverter(namespaceService);
        this.authenticationService = serviceRegistry.getAuthenticationService();
        this.personService = serviceRegistry.getPersonService();
    }

    /**
     * Recipients provided as parameter taskSubscribers: "task name"-{"doc type1"-"recepient field1", ...}
     */
    public void setTaskSubscribers(Map<String, Map<String,List<String>>> taskSubscribers) {
        this.taskSubscribers = taskSubscribers;
    }

    // get notification template arguments for the task
    protected Map<String, Serializable> getNotificationArgs(ExecutionEntity task) {
        Map<String, Serializable> args = new HashMap<String, Serializable>();
        //args.put(ARG_TASK, getTaskInfo(task));
        args.put(ARG_WORKFLOW, getWorkflowInfo(task));
        String userName = authenticationService.getCurrentUserName();
        NodeRef person = personService.getPerson(userName);
        String last_name = (String)nodeService.getProperty(person, ContentModel.PROP_FIRSTNAME);
        String first_name = (String)nodeService.getProperty(person,ContentModel.PROP_LASTNAME);
        args.put(ARG_MODIFIER, last_name+" "+first_name);
        return args;
    }

    private Serializable getWorkflowInfo(ExecutionEntity task) {
        HashMap<String, Object> workflowInfo = new HashMap<String, Object>();
        workflowInfo.put(ARG_WORKFLOW_ID, task.getId());
        HashMap<String, Serializable> properties = new HashMap<String, Serializable>();
        workflowInfo.put(ARG_WORKFLOW_PROPERTIES, properties);
        for(Map.Entry<String, Object> entry : task.getVariables().entrySet()) {
            if(entry.getValue()!=null) {
                properties.put(entry.getKey(), entry.getValue().toString());
            }
            else {
                properties.put(entry.getKey(), null);
            }
        }
        workflowInfo.put(ARG_WORKFLOW_DOCUMENTS, getDocsInfo());
        return workflowInfo;
    }

    /**
     * Method send notificatiion about start task to notification recipients.
     * Mail sends to each document to subscriber because task can containls a lot of different documents
     * and these documents can contains different subscriber.
     */
    @Override
    public void sendNotification(ExecutionEntity task) {
        String subject = null;
        NodeRef workflowPackage = null;
        Vector<String> recipient = new Vector<String>();
        workflowPackage = (NodeRef) task.getVariable("bpm_package");
        if(workflowPackage!=null && nodeService.exists(workflowPackage)) {
            List<ChildAssociationRef> children = services.getNodeService().getChildAssocs(workflowPackage);
            for(ChildAssociationRef child : children) {
                recipient.clear();
                NodeRef node = child.getChildRef();
                if(node!=null  && nodeService.exists(node)) {
                    if(allowDocList==null) {
                        setDocsInfo(node);
                        break;
                    }
                    else {
                        if(allowDocList.contains(qNameConverter.mapQNameToName(nodeService.getType(node)))) {
                            setDocsInfo(node);
                            break;
                        }
                    }
                }
            }
            if(getDocsInfo()!=null && nodeService.exists(getDocsInfo())) {
                NotificationContext notificationContext = new NotificationContext();
                NodeRef template = getNotificationTemplate(task);
                if(template!=null && nodeService.exists(template)) {
                    recipient.addAll(getRecipients(task, template, getDocsInfo()));
                    String from = null;
                    String notificationProviderName = EMailNotificationProvider.NAME;
                    if(subjectTemplates!=null) {
                        String processDef = task.getProcessDefinitionId();
                        String wfkey = FLOWABLE_ENGINE_PREFIX + processDef.substring(0,processDef.indexOf(":"));
                        if(subjectTemplates.containsKey(wfkey)) {
                            Map<String,String> taskSubjectTemplate = subjectTemplates.get(wfkey);
                            if(taskSubjectTemplate.containsKey(qNameConverter.mapQNameToName(nodeService.getType(getDocsInfo())))) {
                                HashMap<String,Object> model = new HashMap<String,Object>(1);
                                model.put(nodeVariable, getDocsInfo());
                                subject = templateService.processTemplateString(templateEngine, taskSubjectTemplate.get(qNameConverter.mapQNameToName(nodeService.getType(getDocsInfo()))), model);
                            }
                        }
                        else {
                            subject = (String) nodeService.getProperty(template, ContentModel.PROP_TITLE);
                        }
                    }
                    else {
                        subject = (String) nodeService.getProperty(template, ContentModel.PROP_TITLE);
                    }
                    for(String to : recipient) {
                        notificationContext.addTo(to);
                    }
                    notificationContext.setSubject(subject);
                    setBodyTemplate(notificationContext, template);
                    notificationContext.setTemplateArgs(getNotificationArgs(task));
                    notificationContext.setAsyncNotification(getAsyncNotification());
                    if (null != from) {
                        notificationContext.setFrom(from);
                    }
                    // send
                    logger.debug("Send notification");
                    services.getNotificationService().sendNotification(notificationProviderName, notificationContext);
                }
            }
        }

    }

    public NodeRef getNotificationTemplate(ExecutionEntity task) {
        String processDef = task.getProcessDefinitionId();
        String wfkey = FLOWABLE_ENGINE_PREFIX + processDef.substring(0,processDef.indexOf(":"));
        String tkey = (String)task.getVariableLocal("taskFormKey");
        logger.debug("template for notification "+getNotificationTemplate(wfkey, tkey));
        return getNotificationTemplate(wfkey, tkey, nodeService.getType(getDocsInfo()));
    }

    /**
     * Include initiator of process to recipients
     */
    public void setSendToOwner(Boolean sendToOwner) {
        this.sendToOwner = sendToOwner.booleanValue();
    }

    public void setNodeOwnerDAO(NodeOwnerDAO nodeOwnerDAO) {
        this.nodeOwnerDAO = nodeOwnerDAO;
    }

    protected void sendToAssignee(ExecutionEntity task, Set<String> authorities) {

    }

    protected void sendToInitiator(ExecutionEntity task, Set<String> authorities) {
        NodeRef initiator = (NodeRef)task.getVariable("initiator");
        String initiator_name = (String) nodeService.getProperty(initiator, ContentModel.PROP_USERNAME);
        authorities.add(initiator_name);
    }

    protected void sendToOwner(Set<String> authorities, NodeRef node) {
        String owner = nodeOwnerDAO.getOwner(node);
        authorities.add(owner);
    }

    public void setAllowDocList(List<String> allowDocList) {
        this.allowDocList = allowDocList;
    }


    protected void sendToSubscribers(ExecutionEntity task, Set<String> authorities, List<String> taskSubscribers) {
        for(String subscriber : taskSubscribers) {
            QName sub = qNameConverter.mapNameToQName(subscriber);
            NodeRef workflowPackage = null;
            workflowPackage = (NodeRef) task.getVariable("bpm_package");
            if(workflowPackage!=null) {
                List<ChildAssociationRef> children = nodeService.getChildAssocs(workflowPackage);
                for(ChildAssociationRef child : children) {
                    NodeRef node = child.getChildRef();
                    Collection<AssociationRef> assocs = nodeService.getTargetAssocs(node, sub);
                    for (AssociationRef assoc : assocs) {
                        NodeRef ref = assoc.getTargetRef();
                        if(nodeService.exists(ref)) {
                            String sub_name = (String) nodeService.getProperty(ref, ContentModel.PROP_USERNAME);
                            authorities.add(sub_name);
                        }
                    }
                }
            }
        }
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public void setTemplateEngine(String templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void setSubjectTemplates(Map<String, Map<String,String>> subjectTemplates) {
        this.subjectTemplates = subjectTemplates;
    }

    public void setNodeVariable(String nodeVariable) {
        this.nodeVariable = nodeVariable;
    }

    private void setDocsInfo(NodeRef docsInfo) {
        AlfrescoTransactionSupport.bindResource(DOCS_INFO_KEY, docsInfo);
    }

    private NodeRef getDocsInfo() {
        return AlfrescoTransactionSupport.getResource(DOCS_INFO_KEY);
    }
}
