package ru.citeck.ecos.webscripts.document;

import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import ru.citeck.ecos.spring.registry.MappingRegistry;

import java.util.Map;

import java.util.stream.Collectors;

public class GetSubmitMessage extends AbstractWebScript {

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
            // construct resulting JSON
            res.setContentType("application/json");
            res.setContentEncoding("UTF-8");
            res.addHeader("Cache-Control", "no-cache");
            res.addHeader("Pragma", "no-cache");
            // write JSON into response stream
            JSONObject result = buildResult(getMessageKey(document));
            result.write(res.getWriter());
        } catch (Exception e) {
            throw new WebScriptException("Caught exception", e);
        }
    }

    private JSONObject buildResult(String messageKey) throws Exception{
        JSONObject result = new JSONObject();
        if (messageKey != null) {
            String message = I18NUtil.getMessage(messageKey);
            if (messageKey.equals("disabled") || message.length() == 0) {
                result.put("disabled", true);
                result.put("message", "");
            } else {
                result.put("disabled", false);
                result.put("message", message);
            }
        } else {
            result.put("disabled", true);
            result.put("message", "");
        }
        return result;
    }

    private String getMessageKey(NodeRef document) {
        QName documentType = nodeService.getType(document);

        Map<QName, String> mapping = documentToMessage.getMapping().entrySet().stream().collect(Collectors.toMap(
                entry -> QName.resolveToQName(namespaceService, entry.getKey()),
                entry -> (entry.getValue())
        ));

        if (mapping.containsKey(documentType)) {
            return mapping.get(documentType);
        }

        ClassDefinition classDefinition = dictionaryService.getClass(documentType);
        while (classDefinition != null) {
            classDefinition = classDefinition.getParentClassDefinition();
            if (classDefinition != null) {
                QName currentName = classDefinition.getName();
                if (mapping.containsKey(currentName)) {
                    return mapping.get(currentName);
                }
            }
        }
        return null;
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
