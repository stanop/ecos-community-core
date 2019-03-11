package ru.citeck.ecos.flowable.listeners;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flowable.bpmn.model.FieldExtension;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.bpmn.behavior.MailActivityBehavior;
import org.flowable.engine.impl.bpmn.helper.ClassDelegate;
import org.flowable.engine.impl.bpmn.parser.FieldDeclaration;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.el.FixedValue;
import org.flowable.ui.common.service.exception.NotFoundException;
import ru.citeck.ecos.flowable.example.AbstractExecutionListener;
import ru.citeck.ecos.flowable.utils.FlowableListenerUtils;
import ru.citeck.ecos.model.ClassificationModel;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.providers.ApplicationContextProvider;
import ru.citeck.ecos.flowable.cmd.GetProcessServiceMailTaskCmd;
import ru.citeck.ecos.utils.RepoUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class FlowableSetAttachmentToMailExecutionListener extends AbstractExecutionListener {

    private static final Log logger = LogFactory.getLog(FlowableSetAttachmentToMailExecutionListener.class);

    private static final String SPACES_STORE_PREFIX = "workspace://SpacesStore/";

    private static final String SEPARATOR = ",";

    private ArrayList<NodeRef> documentTypeRefs = new ArrayList<>();

    private NodeService nodeService;
    private ContentService contentService;
    private ProcessEngineConfigurationImpl engineConfiguration;

    private Expression mailTaskId;
    private Expression filesRefs;

    @Override
    protected void initImpl() {
        this.nodeService = serviceRegistry.getNodeService();
        this.contentService = serviceRegistry.getContentService();
        this.engineConfiguration = ApplicationContextProvider.getBean("flowableEngineConfiguration",
                ProcessEngineConfigurationImpl.class);
        fillNodeRefs(this.documentTypeRefs, filesRefs.getExpressionText());
    }

    @Override
    protected void notifyImpl(DelegateExecution execution) {
        NodeRef document = FlowableListenerUtils.getDocument(execution, nodeService);
        if (document == null || !nodeService.exists(document)) {
            return;
        }

        if (mailTaskId == null) {
            throw new IllegalArgumentException("'mailTaskId' is not set");
        }

        if (documentTypeRefs.isEmpty()) {
            throw new IllegalArgumentException("'filesRefs' is not set or set incorrectly");
        }

        String mailTaskIdStr = mailTaskId.getExpressionText();
        Optional<ServiceTask> mailTaskOpt = getMailServiceTask(execution.getProcessDefinitionId(), mailTaskIdStr);
        if (mailTaskOpt.isPresent()) {
            ServiceTask mailTask = mailTaskOpt.get();
            List<FieldDeclaration> fieldDeclarations = createFieldDeclarations(mailTask.getFieldExtensions(),
                    getAttachmentFiles(document));
            MailActivityBehavior behavior = (MailActivityBehavior)
                    ClassDelegate.defaultInstantiateDelegate(
                            MailActivityBehavior.class, fieldDeclarations, mailTask);
            mailTask.setBehavior(behavior);
        } else {
            throw new NotFoundException("Not found mail task with a given id: " + mailTaskId);
        }
    }

    private List<FieldDeclaration> createFieldDeclarations(List<FieldExtension> fieldList, File[] attachmentFiles) {
        List<FieldDeclaration> fieldDeclarations = new ArrayList<>();

        for (FieldExtension fieldExtension : fieldList) {
            FieldDeclaration fieldDeclaration;
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(fieldExtension.getExpression())) {
                fieldDeclaration = new FieldDeclaration(fieldExtension.getFieldName(), Expression.class.getName(),
                        engineConfiguration.getExpressionManager().createExpression(fieldExtension.getExpression()));
            } else {
                fieldDeclaration = new FieldDeclaration(fieldExtension.getFieldName(), Expression.class.getName(),
                        new FixedValue(fieldExtension.getStringValue()));
            }

            fieldDeclarations.add(fieldDeclaration);
        }
        FieldDeclaration attachments = new FieldDeclaration("attachments", Expression.class.getName(),
                new FixedValue(attachmentFiles));
        fieldDeclarations.add(attachments);

        return fieldDeclarations;
    }

    private Optional<ServiceTask> getMailServiceTask(String workflowDefId, String mailServiceTaskId) {
        GetProcessServiceMailTaskCmd cmd = new GetProcessServiceMailTaskCmd(workflowDefId, mailServiceTaskId);
        return Optional.ofNullable(engineConfiguration.getCommandExecutor().execute(cmd));
    }

    private File[] getAttachmentFiles(NodeRef document) {
        List<File> attachments = new ArrayList<>();
        List<NodeRef> fileRefs = getChildDocuments(document);
        for (NodeRef fileRef : fileRefs) {
            ContentReader reader = contentService.getReader(fileRef, ContentModel.PROP_CONTENT);
            String fileName = RepoUtils.getProperty(fileRef, ContentModel.PROP_NAME, nodeService);
            String prefix = fileName.split("\\.")[0];
            String suffix = "." + fileName.split("\\.")[1];
            File attachmentFile = TempFileProvider.createTempFile(prefix, suffix);
            try (FileOutputStream outputStream = new FileOutputStream(attachmentFile)) {
                InputStream inputStream = reader.getContentInputStream();
                byte[] buffer = new byte[1000];
                while (true) {
                    int size = inputStream.read(buffer);
                    if (size <= 0) break;
                    outputStream.write(buffer, 0, size);
                }
                outputStream.flush();
            } catch (IOException e) {
                logger.error("Error while writing to attached file " + e);
            }
            attachments.add(attachmentFile);
        }
        return attachments.toArray(new File[attachments.size()]);
    }

    private List<NodeRef> getChildDocuments(NodeRef document) {
        List<NodeRef> fileRefs = new ArrayList<>();
        for (NodeRef documentTypeRef : documentTypeRefs) {
            List<NodeRef> childDocuments = RepoUtils.getChildrenByAssoc(document, ICaseModel.ASSOC_DOCUMENTS, nodeService);
            for (NodeRef childDocument : childDocuments) {
                NodeRef documentCategory = RepoUtils.getProperty(childDocument, ClassificationModel.PROP_DOCUMENT_KIND, nodeService);
                if (documentTypeRef.equals(documentCategory)) {
                    fileRefs.add(childDocument);
                }
            }
        }
        return fileRefs;
    }

    private static void fillNodeRefs(Collection<NodeRef> result, Object data) {

        if (data == null) {
            return;
        }

        if (data instanceof NodeRef) {

            result.add((NodeRef) data);

        } else if (data instanceof String) {

            String[] dataStr = ((String) data).split(SEPARATOR);

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
