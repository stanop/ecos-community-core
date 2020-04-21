package ru.citeck.ecos.icase.actions;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.stereotype.Service;
import ru.citeck.ecos.action.node.CreateNodeAction;
import ru.citeck.ecos.action.node.NodeActionDefinition;
import ru.citeck.ecos.action.node.RequestAction;
import ru.citeck.ecos.icase.activity.dto.*;
import ru.citeck.ecos.icase.activity.service.CaseActivityEventService;
import ru.citeck.ecos.icase.activity.service.eproc.EProcActivityService;
import ru.citeck.ecos.icase.activity.service.eproc.EProcUtils;
import ru.citeck.ecos.icase.activity.service.eproc.importer.parser.CmmnDefinitionConstants;
import ru.citeck.ecos.model.EcosProcessModel;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.role.CaseRoleService;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EProcCaseActionsProvider implements CaseActionsProvider {

    private static final String USER_ACTION_TYPE = "user-action";

    private static final String FIRE_EVENT_URL_TEMPLATE = "citeck/event/fire-event?eventRef=%s";

    private EProcActivityService eprocActivityService;
    private CaseActivityEventService caseActivityEventService;
    private CaseRoleService caseRoleService;

    private NamespaceService namespaceService;
    private AuthorityService authorityService;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private Repository repositoryHelper;

    @Autowired
    public EProcCaseActionsProvider(EProcActivityService eprocActivityService,
                                    CaseActivityEventService caseActivityEventService,
                                    CaseRoleService caseRoleService,
                                    NamespaceService namespaceService,
                                    AuthorityService authorityService,
                                    DictionaryService dictionaryService,
                                    NodeService nodeService,
                                    @Qualifier("repositoryHelper") Repository repositoryHelper) {
        this.eprocActivityService = eprocActivityService;
        this.caseActivityEventService = caseActivityEventService;
        this.caseRoleService = caseRoleService;
        this.namespaceService = namespaceService;
        this.authorityService = authorityService;
        this.dictionaryService = dictionaryService;
        this.nodeService = nodeService;
        this.repositoryHelper = repositoryHelper;
    }

    @Override
    public List<NodeActionDefinition> getCaseActions(NodeRef caseNodeRef) {
        List<SentryDefinition> sentryDefinitions = getUserActionSentryDefinitions(caseNodeRef);
        List<NodeActionDefinition> actions = new ArrayList<>(sentryDefinitions.size());
        for (SentryDefinition sentryDefinition : sentryDefinitions) {
            ActivityDefinition userActionDef = getUserActionDefinition(sentryDefinition);

            String additionalDataType = EProcUtils.getDefAttribute(userActionDef,
                    CmmnDefinitionConstants.ADDITIONAL_DATA_TYPE);
            if (StringUtils.isBlank(additionalDataType)) {
                actions.add(composeCreateRequestAction(caseNodeRef, userActionDef, sentryDefinition));
            } else {
                actions.add(composeCreateNodeAction(caseNodeRef, userActionDef, additionalDataType, sentryDefinition));
            }
        }
        return actions;
    }

    private List<SentryDefinition> getUserActionSentryDefinitions(NodeRef caseNodeRef) {
        RecordRef caseRef = RecordRef.valueOf(caseNodeRef.toString());
        List<SentryDefinition> result = eprocActivityService.findSentriesBySourceRefAndEventType(
                caseRef, ActivityRef.ROOT_ID, USER_ACTION_TYPE);
        if (CollectionUtils.isEmpty(result)) {
            return Collections.emptyList();
        }

        return result.stream()
                .filter(sentry -> checkRoles(caseNodeRef, sentry))
                .filter(sentry -> checkSentryConditions(caseRef, sentry))
                .collect(Collectors.toList());
    }

    private boolean checkRoles(NodeRef caseNodeRef, SentryDefinition sentryDefinition) {
        ActivityDefinition userActionDef = getUserActionDefinition(sentryDefinition);
        String[] authorizedRoleVarNames = EProcUtils.getDefAttribute(userActionDef,
                CmmnDefinitionConstants.AUTHORIZED_ROLE_VAR_NAMES_SET, String[].class);
        if (authorizedRoleVarNames == null || authorizedRoleVarNames.length == 0) {
            return true;
        }

        Set<String> authorizedAuthorities = Arrays.stream(authorizedRoleVarNames)
                .flatMap(roleVarName -> caseRoleService.getAssignees(caseNodeRef, roleVarName).stream())
                .map(assigneeRef -> RepoUtils.getAuthorityName(assigneeRef, nodeService, dictionaryService))
                .collect(Collectors.toSet());

        return userInAnyAuthority(authorizedAuthorities);
    }

    private boolean userInAnyAuthority(Set<String> authorizedAuthorities) {
        NodeRef person = repositoryHelper.getPerson();
        String userName = (String) nodeService.getProperty(person, ContentModel.PROP_USERNAME);
        Set<String> userAuthorities = new HashSet<>(authorityService.getAuthoritiesForUser(userName));
        userAuthorities.add(userName);

        userAuthorities.retainAll(authorizedAuthorities);
        return !userAuthorities.isEmpty();
    }

    private boolean checkSentryConditions(RecordRef caseRef, SentryDefinition sentryDefinition) {
        EventRef eventRef = EventRef.of(CaseServiceType.EPROC, caseRef, sentryDefinition.getId());
        return caseActivityEventService.checkConditions(eventRef);
    }

    private ActivityDefinition getUserActionDefinition(SentryDefinition sentryDefinition) {
        return sentryDefinition.getParentTriggerDefinition()
                .getParentActivityTransitionDefinition()
                .getParentActivityDefinition();
    }

    private NodeActionDefinition composeCreateRequestAction(NodeRef caseNodeRef, ActivityDefinition userActionDef,
                                                            SentryDefinition sentry) {

        RequestAction result = new RequestAction();
        EventRef eventRef = composeEventRef(caseNodeRef, sentry);
        result.setUrl(String.format(FIRE_EVENT_URL_TEMPLATE, URLEncoder.encode(eventRef.toString())));
        result.setConfirmationMessage(getMessage(userActionDef, CmmnDefinitionConstants.CONFIRMATION_MESSAGE));
        result.setSuccessMessage(getMessage(userActionDef, CmmnDefinitionConstants.SUCCESS_MESSAGE));
        String spanClass = EProcUtils.getDefAttribute(userActionDef, CmmnDefinitionConstants.SUCCESS_MESSAGE_SPAN_CLASS);
        if (spanClass != null) {
            result.setSuccessMessageSpanClass(spanClass);
        }
        result.setTitle(EProcUtils.getDefAttribute(userActionDef, CmmnDefinitionConstants.TITLE));
        return result;
    }

    private NodeActionDefinition composeCreateNodeAction(NodeRef caseNodeRef, ActivityDefinition userActionDef,
                                                         String additionalDataType, SentryDefinition sentryDefinition) {

        CreateNodeAction result = new CreateNodeAction();
        result.setNodeType(additionalDataType);
        result.setDestination(caseNodeRef);
        String destinationAssoc = EcosProcessModel.ASSOC_ADDITIONAL_EVENT_DATA_ITEMS.toPrefixString(namespaceService);
        result.setDestinationAssoc(destinationAssoc);
        result.setTitle(EProcUtils.getDefAttribute(userActionDef, CmmnDefinitionConstants.TITLE));
        result.setEventRef(composeEventRef(caseNodeRef, sentryDefinition).toString());
        return result;
    }

    private EventRef composeEventRef(NodeRef caseNodeRef, SentryDefinition sentry) {
        RecordRef caseRef = RecordRef.valueOf(caseNodeRef.toString());
        return EventRef.of(CaseServiceType.EPROC, caseRef, sentry.getId());
    }

    private String getMessage(ActivityDefinition userActionDef, String messageKey) {
        String message = EProcUtils.getDefAttribute(userActionDef, messageKey);
        if (StringUtils.isNotBlank(message)) {
            String localizedMessage = I18NUtil.getMessage(message);
            return StringUtils.defaultIfBlank(localizedMessage, message);
        }
        return "";
    }
}
