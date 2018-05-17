package ru.citeck.ecos.webscripts.orgstruct;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.search.ftsquery.OperandExpected;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChildrenGet extends AbstractWebScript {

    private static final String TEMPLATE_PARAM_GROUPNAME = "groupname";

    private static final String PARAM_ROLE = "role";
    private static final String PARAM_USER = "user";
    private static final String PARAM_GROUP = "group";
    private static final String PARAM_BRANCH = "branch";
    private static final String PARAM_FILTER = "filter";
    private static final String PARAM_DEFAULT = "default";
    private static final String PARAM_RECURSE = "recurse";
    private static final String PARAM_SUB_TYPES = "subTypes";
    private static final String PARAM_SHOW_DISABLED = "showdisabled";

    private static final String GROUP_PREFIX = "GROUP_";

    private AuthorityService authorityService;
    private SearchService searchService;
    private NodeService nodeService;

    @Autowired
    public ChildrenGet(ServiceRegistry serviceRegistry) {
        this.authorityService = serviceRegistry.getAuthorityService();
        this.searchService = serviceRegistry.getSearchService();
        this.nodeService = serviceRegistry.getNodeService();
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        RequestParams params = getRequestParams(req);

        if (params.groupRef == null) {
            res.getWriter().write("Group " + params.filterOptions.rootGroup + " not found");
            res.setStatus(Status.STATUS_NOT_FOUND);
            return;
        }




    }

    private List<Pair<NodeRef, String>> findAuthorities(FilterOptions filterOptions) {

        String filter = filterOptions.filter;

        FTSQuery query = FTSQuery.createRaw()
                                 .eventual();

        if (filterOptions.group) {
            ((OperandExpected) query)
                    .open()
                        .type(ContentModel.TYPE_AUTHORITY_CONTAINER).and()
                        .open()
                            .value(ContentModel.PROP_AUTHORITY_DISPLAY_NAME, filter).or()
                            .value(ContentModel.PROP_AUTHORITY_NAME, filter)
                        .close()
                    .close();
        }
        if (filterOptions.user) {
            ((OperandExpected) query)
                    .open()
                        .type(ContentModel.TYPE_PERSON).and()
                        .open()
                            .value(ContentModel.PROP_USERNAME, filter).or()
                            .value(ContentModel.PROP_FIRSTNAME, filter).or()
                            .value(ContentModel.PROP_LASTNAME, filter)
                        .close()
                    .close();
        }

        Map<String, Boolean> inRootGroupCache = new HashMap<>();
        String rootGroup = filterOptions.rootGroup;

        return query.query(searchService)
                    .stream()
                    .map(this::getAuthorityNameRef)
                    .filter(auth -> isInRootGroup(auth.getSecond(), rootGroup, inRootGroupCache))
                    .collect(Collectors.toList());
    }

    private Pair<NodeRef, String> getAuthorityNameRef(NodeRef nodeRef) {
        Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
        String authorityName = (String) props.get(ContentModel.PROP_USERNAME);
        if (authorityName == null) {
            authorityName = (String) props.get(ContentModel.PROP_AUTHORITY_NAME);
        }
        return new Pair<>(nodeRef, authorityName);
    }

    private Boolean isInRootGroup(String authorityName, String rootGroup, Map<String, Boolean> cache) {

        if (StringUtils.isBlank(authorityName)) {
            return false;
        }

        Boolean result = cache.get(authorityName);

        if (result == null) {

            Set<String> containers =
                    authorityService.getContainingAuthorities(AuthorityType.GROUP, authorityName, true);

            result = false;
            if (containers.contains(rootGroup)) {
                result = true;
            } else {
                for (String container : containers) {
                    if (isInRootGroup(container, rootGroup, cache)) {
                        result = true;
                    }
                }
            }
            cache.put(authorityName, result);
        }

        return result;
    }

    private RequestParams getRequestParams(WebScriptRequest req) {

        RequestParams params = new RequestParams();
        params.filterOptions = getFilterOptions(req);
        params.groupRef = authorityService.getAuthorityNodeRef(params.filterOptions.rootGroup);
        params.recurse = Boolean.TRUE.toString().equals(req.getParameter(PARAM_RECURSE));

        return params;
    }

    private FilterOptions getFilterOptions(WebScriptRequest req) {

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String rootGroupName = templateVars.get(TEMPLATE_PARAM_GROUPNAME);

        if (StringUtils.isBlank(rootGroupName)) {
            throw new AlfrescoRuntimeException("Template parameter " + TEMPLATE_PARAM_GROUPNAME + " is undefined");
        }

        Boolean defaultEnabled = !Boolean.FALSE.toString().equals(req.getParameter(PARAM_DEFAULT));

        Function<String, Boolean> processOption = name -> {
            String value = req.getParameter(name);
            return StringUtils.isNotBlank(value) ? !Boolean.FALSE.toString().equals(value) : defaultEnabled;
        };

        FilterOptions result = new FilterOptions();

        if (!rootGroupName.startsWith(GROUP_PREFIX)) {
            rootGroupName = GROUP_PREFIX + rootGroupName;
        }

        result.rootGroup = rootGroupName;
        result.role = processOption.apply(PARAM_ROLE);
        result.user = processOption.apply(PARAM_USER);
        result.group = processOption.apply(PARAM_GROUP);
        result.branch = processOption.apply(PARAM_BRANCH);
        result.showDisabled = processOption.apply(PARAM_SHOW_DISABLED);

        String filter = req.getParameter(PARAM_FILTER);
        if (StringUtils.isNotBlank(filter)) {
            //String patternStr = filter.replaceAll("([*?+])", ".$1");
            result.filter = filter;
        }

        String subTypes = req.getParameter(PARAM_SUB_TYPES);
        if (StringUtils.isNotBlank(subTypes)) {
            result.subTypes = Arrays.asList(subTypes.split(","));
        } else {
            result.subTypes = Collections.emptyList();
        }

        return result;
    }

    private static class RequestParams {
        boolean recurse;
        NodeRef groupRef;
        FilterOptions filterOptions;
    }

    private static class FilterOptions {
        boolean branch;
        boolean role;
        boolean group;
        boolean user;
        boolean showDisabled;
        List<String> subTypes;
        String filter;
        String rootGroup;
    }

    private static class Authority {
        String nodeRef;
        String fullName;
        String shortName;
        String displayName;
        String authorityType;
        String authorityName;
    }

    private static class UserAuthority extends Authority {
        String firstName;
        String lastName;
        boolean available;
        boolean isPersonDisabled;
    }

    private static class BranchAuthority extends Authority {
        final String groupType = "branch";
        String groupSubType;
    }

    private static class RoleAuthority extends Authority {
        final String groupType = "role";
        String groupSubType;
        boolean roleIsManager;
    }

    private static class GroupAuthority extends Authority {
        final String groupType = "group";
    }
}
