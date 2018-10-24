package ru.citeck.ecos.webscripts.cases;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.config.EcosConfigService;
import ru.citeck.ecos.icase.activity.CaseActivityService;
import ru.citeck.ecos.icase.activity.CaseActivityServiceImpl;
import ru.citeck.ecos.model.*;
import ru.citeck.ecos.utils.NodeUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class FlatCaseActivitiesGet extends AbstractWebScript {

    private static final String DEFAULT_SLA_JOURNAL_ITEM_ID = "actual-default-sla-duration";
    private static final String PARAM_NODE_REF = "nodeRef";

    private static final Log logger = LogFactory.getLog(FlatCaseActivitiesGet.class);

    private CaseActivityService caseActivityService;
    private EcosConfigService ecosConfigService;
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private MessageService messageService;
    private NodeService nodeService;
    private NodeUtils nodeUtils;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        String rootRefStr = req.getParameter(PARAM_NODE_REF);
        ParameterCheck.mandatoryString(PARAM_NODE_REF, rootRefStr);

        String rawSla = (String) ecosConfigService.getParamValue(DEFAULT_SLA_JOURNAL_ITEM_ID);
        int defaultSla = 8;
        if (rawSla != null) {
            try {
                defaultSla = Integer.parseInt(rawSla);
            } catch (NumberFormatException e) {
                //do nothing
            }
        }

        NodeRef rootRef = new NodeRef(rootRefStr);
        Map<NodeRef, Activity> activities = new HashMap<>();

        fillActivities(activities, new HashMap<>(), null, rootRef, defaultSla);
        fillDepends(activities, ICaseEventModel.ASSOC_ACTIVITY_START_EVENTS, Direction.SS, Direction.FS);
        fillDepends(activities, ICaseEventModel.ASSOC_ACTIVITY_RESTART_EVENTS, Direction.SS, Direction.FS);
        fillDepends(activities, ICaseEventModel.ASSOC_ACTIVITY_END_EVENTS, Direction.SF, Direction.FF);
        fillStartEndTime(activities);

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
        objectMapper.writeValue(res.getOutputStream(), activities.values());
        res.setStatus(Status.STATUS_OK);
    }

    private void fillStartEndTime(Map<NodeRef, Activity> activities) {
        activities.forEach((activityRef, activity) -> {
            fillStartTime(activity, new HashSet<>(), new HashSet<>());
            fillEndTime(activity, new HashSet<>(), new HashSet<>());
        });
    }

    private void fillStartTime(Activity activity, Set<Integer> startTimeContext, Set<Integer> endTimeContext) {

        if (activity.start == null) {

            if (startTimeContext.contains(activity.id)) {
                activity.start = new Date();
                logger.error("[fillStartTime] Found infinite loop. Activity: " + activity +
                             " context: " + startTimeContext);
                return;
            }

            startTimeContext.add(activity.id);

            Date maxTime = null;

            for (Depend depend : activity.depend) {

                Date endTime = null;

                if (Direction.FS.equals(depend.direction)) {
                    fillEndTime(depend.target, startTimeContext, endTimeContext);
                    if (depend.target.end != null) {
                        endTime = depend.target.end;
                    }
                } else if (Direction.SS.equals(depend.direction)) {
                    fillStartTime(depend.target, startTimeContext, endTimeContext);
                    endTime = depend.target.start;
                }

                if (endTime != null && (maxTime == null || maxTime.getTime() < endTime.getTime())) {
                    maxTime = endTime;
                }
            }

            if (maxTime == null) {
                maxTime = new Date();
            }

            activity.start = maxTime;

            startTimeContext.remove(activity.id);
        }
    }

    private void fillEndTime(Activity activity, Set<Integer> startTimeContext, Set<Integer> endTimeContext) {

        if (activity.end == null) {

            if (endTimeContext.contains(activity.id)) {
                activity.end = activity.start;
                logger.error("[fillEndTime] Found infinite loop. Activity: " + activity +
                             " context: " + endTimeContext);
                return;
            }

            endTimeContext.add(activity.id);

            fillStartTime(activity, startTimeContext, endTimeContext);

            if (dictionaryService.isSubClass(activity.typeQName, ActionModel.TYPE_ACTION)) {
                activity.end = activity.start;
                endTimeContext.remove(activity.id);
                return;
            }

            Date maxTime = null;

            for (Depend depend : activity.depend) {

                Date endTime = null;

                if (Direction.FF.equals(depend.direction)) {
                    if (depend.target == activity) {
                        endTime = activity.start;
                        Activity lastActivity = activity;
                        for (Activity child : activity.children) {
                            fillEndTime(child, startTimeContext, endTimeContext);
                            if (child.end != null && child.end.getTime() > endTime.getTime()) {
                                endTime = child.end;
                                lastActivity = child;
                            }
                        }
                        depend.target = lastActivity;
                        //stage completed when all children completed
                        break;
                    } else {
                        fillEndTime(depend.target, startTimeContext, endTimeContext);
                        if (depend.target.end != null) {
                            endTime = depend.target.end;
                        }
                    }
                } else if (Direction.SF.equals(depend.direction)) {
                    fillStartTime(depend.target, startTimeContext, endTimeContext);
                    endTime = depend.target.start;
                }

                if (endTime != null && (maxTime == null || maxTime.getTime() < endTime.getTime())) {
                    maxTime = endTime;
                }
            }

            if (maxTime == null) {
                maxTime = new Date(activity.start.getTime() + (long) (activity.sla / 8.0) * 24 * 60 * 60 * 1000);
            }

            activity.end = maxTime;

            endTimeContext.remove(activity.id);
        }
    }

    private void fillDepends(Map<NodeRef, Activity> activities,
                             QName eventsAssoc,
                             Direction startedDependType,
                             Direction stoppedDependType) {

        activities.forEach((activityRef, activity) -> {

            List<ChildAssociationRef> startEvents = nodeService.getChildAssocs(activityRef,
                                                                               eventsAssoc,
                                                                               RegexQNamePattern.MATCH_ALL);

            for (ChildAssociationRef ref : startEvents) {

                NodeRef eventRef = ref.getChildRef();
                Optional<NodeRef> source = nodeUtils.getAssocTarget(eventRef, EventModel.ASSOC_EVENT_SOURCE);

                if (source.isPresent()) {

                    Activity sourceActivity = activities.get(source.get());

                    if (sourceActivity != null) {
                        QName eventType = nodeService.getType(eventRef);

                        Depend depend = null;
                        if (ICaseEventModel.TYPE_ACTIVITY_STARTED_EVENT.equals(eventType)) {
                            depend = new Depend();
                            depend.direction = startedDependType;
                            depend.target = sourceActivity;
                        } else if (ICaseEventModel.TYPE_ACTIVITY_STOPPED_EVENT.equals(eventType)) {
                            depend = new Depend();
                            depend.direction = stoppedDependType;
                            depend.target = sourceActivity;
                        } else if (ICaseEventModel.TYPE_STAGE_CHILDREN_STOPPED.equals(eventType)) {
                            depend = new Depend();
                            depend.direction = stoppedDependType;
                            depend.target = sourceActivity;
                        }
                        if (depend != null) {
                            activity.depend.add(depend);
                        }
                    }
                }
            }
        });
    }

    private void fillActivities(Map<NodeRef, Activity> activities,
                                Map<QName, String> types,
                                Activity parent,
                                NodeRef parentRef,
                                int defaultSla) {

        for (NodeRef activityRef : caseActivityService.getActivities(parentRef)) {

            if (activities.containsKey(activityRef)) {
                continue;
            }

            Activity activity = new Activity();
            activity.parent = parent;

            Map<QName, Serializable> activityProps = nodeService.getProperties(activityRef);
            activity.start = (Date) activityProps.get(ActivityModel.PROP_ACTUAL_START_DATE);
            activity.end = (Date) activityProps.get(ActivityModel.PROP_ACTUAL_END_DATE);
            activity.title = (String) activityProps.get(ContentModel.PROP_TITLE);
            activity.type = types.computeIfAbsent(nodeService.getType(activityRef), this::getTypeTitle);
            //for client purposes we start id from 1
            activity.id = activities.size() + 1;
            activity.isGroup = nodeService.hasAspect(activityRef, ActivityModel.ASPECT_HAS_ACTIVITIES);

            activity.state = (String) activityProps.get(LifeCycleModel.PROP_STATE);
            activity.isCompleted = CaseActivityServiceImpl.STATE_COMPLETED.equals(activity.state);
            activity.isOpen = activity.start != null && !activity.isCompleted;

            Integer sla = (Integer) activityProps.get(ActivityModel.PROP_EXPECTED_PERFORM_TIME);
            activity.sla = sla != null ? sla : defaultSla;

            activity.typeQName = nodeService.getType(activityRef);

            activities.put(activityRef, activity);
            if (parent != null) {
                parent.children.add(activity);
            }

            fillActivities(activities, types, activity, activityRef, defaultSla);
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
    public void setEcosConfigService(EcosConfigService ecosConfigService) {
        this.ecosConfigService = ecosConfigService;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.nodeService = serviceRegistry.getNodeService();
        this.messageService = serviceRegistry.getMessageService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.dictionaryService = serviceRegistry.getDictionaryService();
    }

    @Autowired
    public void setNodeUtils(NodeUtils nodeUtils) {
        this.nodeUtils = nodeUtils;
    }

    private enum Direction {
        FF, SS, FS, SF
    }

    private static class Depend {

        public Direction direction;

        Activity target;

        public int getTarget() {
            return target.id;
        }

        @Override
        public String toString() {
            return "Depend{" +
                    "target=" + getTarget() +
                    ", direction=" + direction +
                    '}';
        }
    }

    private class Activity {

        public int id;
        public String title;
        public String type;
        public boolean isGroup;
        public boolean isCompleted;
        public boolean isOpen;
        public int sla;
        public String state;

        public List<Depend> depend = new ArrayList<>();

        Date start;
        Date end;
        Activity parent;
        List<Activity> children = new ArrayList<>();
        QName typeQName;

        public String getStart() {
            return formatDate(start);
        }

        public String getEnd() {
            return formatDate(end);
        }

        public Integer getParent() {
            return parent != null ? parent.id : null;
        }

        @Override
        public String toString() {
            return "Activity{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    ", type='" + type + '\'' +
                    ", isGroup=" + isGroup +
                    ", isCompleted=" + isCompleted +
                    ", isOpen=" + isOpen +
                    ", sla=" + sla +
                    ", state='" + state + '\'' +
                    ", depend=" + depend +
                    ", start=" + getStart() +
                    ", end=" + getEnd() +
                    ", parent=" + getParent() +
                    '}';
        }
    }
}