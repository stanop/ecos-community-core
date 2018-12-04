package ru.citeck.ecos.webscripts.document;

import lombok.Getter;
import lombok.Setter;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.spring.registry.MappingRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class GetSubmitMessage extends AbstractWebScript {

    private ObjectMapper objectMapper = new ObjectMapper();
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private MappingRegistry<String, String> documentToMessage;

    public static String NODE_REF = "nodeRef";

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) {
        String docRefParam = req.getParameter(NODE_REF);
        NodeRef document = new NodeRef(docRefParam);
        try {
            res.setContentType("application/json");
            res.setContentEncoding("UTF-8");
            res.setCache(new Cache());
            res.addHeader("Pragma", "no-cache");
            Result result = buildResult(getMessageKey(document));
            objectMapper.writeValue(res.getOutputStream(), result);
        } catch (Exception e) {
            throw new WebScriptException("Caught exception", e);
        }
    }

    private Result buildResult(String messageKey) {
        Result result = new Result();
        if (messageKey != null) {
            String message = I18NUtil.getMessage(messageKey);
            if (!messageKey.equals("disabled") && message != null && message.length() > 0) {
                result.setDisabled(false);
                result.setMessage(message);
            } else {
                result.setDisabled(true);
                result.setMessage("");
            }
        } else {
            result.setDisabled(true);
            result.setMessage("");
        }
        return result;
    }

    private String getMessageKey(NodeRef document) {
        QName documentType = nodeService.getType(document);
        String docShortQName = documentType.toPrefixString(namespaceService);

        Map<String, String> mapping = documentToMessage.getMapping();

        if (mapping.containsKey(docShortQName)) {
            return mapping.get(docShortQName);
        }

        ClassDefinition classDefinition = dictionaryService.getClass(documentType);
        while (classDefinition != null) {
            classDefinition = classDefinition.getParentClassDefinition();
            if (classDefinition != null) {
                QName currentName = classDefinition.getName();
                String currentShortQName = currentName.toPrefixString(namespaceService);
                if (mapping.containsKey(currentShortQName)) {
                    return mapping.get(currentShortQName);
                }
            }
        }
        return null;
    }

    private static class Result {
        @Setter @Getter private boolean disabled;
        @Setter @Getter private String message;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public NamespaceService getNamespaceService() {
        return namespaceService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public MappingRegistry<String, String> getDocumentToMessage() {
        return documentToMessage;
    }

    public void setDocumentToMessage(MappingRegistry<String, String> documentToMessage) {
        this.documentToMessage = documentToMessage;
    }

    public DictionaryService getDictionaryService() {
        return dictionaryService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }
}
