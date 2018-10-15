package ru.citeck.ecos.webscripts.cases;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.model.ActivityModel;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FlatCaseActivitiesGet extends AbstractWebScript {

    private static final String PARAM_ROOT_REF = "rootRef";

    private CaseActivityService caseActivityService;
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private MessageService messageService;
    private NodeService nodeService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        String rootRefStr = req.getParameter(PARAM_ROOT_REF);
        ParameterCheck.mandatoryString(PARAM_ROOT_REF, rootRefStr);

        NodeRef rootRef = new NodeRef(rootRefStr);
        List<Activity> activities = new ArrayList<>();

        fillActivities(activities, new HashMap<>(), null, rootRef, new AtomicInteger());

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
        objectMapper.writeValue(res.getOutputStream(), activities);
        res.setStatus(Status.STATUS_OK);
    }

    private void fillActivities(List<Activity> activities,
                                Map<QName, String> types,
                                Integer parentId,
                                NodeRef parentRef,
                                AtomicInteger idCounter) {

        for (NodeRef activityRef : caseActivityService.getActivities(parentRef)) {

            Activity activity = new Activity();
            activity.parent = parentId;

            Map<QName, Serializable> activityProps = nodeService.getProperties(activityRef);
            activity.start = formatDate(activityProps.get(ActivityModel.PROP_ACTUAL_START_DATE));
            activity.end = formatDate(activityProps.get(ActivityModel.PROP_ACTUAL_END_DATE));
            activity.title = (String) activityProps.get(ContentModel.PROP_TITLE);
            activity.type = types.computeIfAbsent(nodeService.getType(activityRef), this::getTypeTitle);
            activity.id = idCounter.getAndIncrement();

            activities.add(activity);

            fillActivities(activities, types, activity.id, activityRef, idCounter);
        }
    }

    private String formatDate(Serializable date) {
        return date != null ? ISO8601Utils.format((Date) date) : null;
    }

    private String getTypeTitle(QName type) {
        TypeDefinition typeDef = dictionaryService.getType(type);
        String title = typeDef.getTitle(messageService);
        return StringUtils.isBlank(title) ? type.toPrefixString(namespaceService) : title;
    }

    @Autowired
    public void setCaseActivityService(CaseActivityService caseActivityService) {
        this.caseActivityService = caseActivityService;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.nodeService = serviceRegistry.getNodeService();
        this.messageService = serviceRegistry.getMessageService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.dictionaryService = serviceRegistry.getDictionaryService();
    }

    private static class Activity {
        public Integer id;
        public String title;
        public String type;
        public String start;
        public String end;
        public Integer parent;
    }
}
