package ru.citeck.ecos.cmmn.service;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.cmmn.model.Definitions;
import ru.citeck.ecos.content.ContentData;
import ru.citeck.ecos.content.TypeKindContentDAO;
import ru.citeck.ecos.model.ICaseModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Pavel Simonov
 */

public class CaseTemplateRegistry extends TypeKindContentDAO<Definitions> {

    private static final String SCRIPT_ENGINE = "javascript";
    private ScriptService scriptService;

    public Optional<Definitions> getDefinitionForCase(NodeRef caseRef) {

        List<ContentData<Definitions>> configs = getContentDataByNodeTKC(caseRef);

        return configs.stream()
                      .filter(d -> d.getData().isPresent() && evalCondition(d.getNodeRef(), caseRef))
                      .findFirst()
                      .flatMap(ContentData::getData);
    }

    private boolean evalCondition(NodeRef templateRef, NodeRef caseNode) {
        if (templateRef == null) {
            return false;
        }
        String conditionScript = (String) nodeService.getProperty(templateRef, ICaseModel.PROP_CONDITION);
        if (StringUtils.isNotBlank(conditionScript)) {
            Map<String, Object> model = new HashMap<>();
            model.put("caseNode", caseNode);
            Object scriptResult = scriptService.executeScriptString(SCRIPT_ENGINE, conditionScript, model);
            return Boolean.TRUE.equals(scriptResult);
        }
        return true;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        super.setServiceRegistry(serviceRegistry);
        scriptService = (ScriptService) serviceRegistry.getService(ServiceRegistry.SCRIPT_SERVICE);
    }
}
