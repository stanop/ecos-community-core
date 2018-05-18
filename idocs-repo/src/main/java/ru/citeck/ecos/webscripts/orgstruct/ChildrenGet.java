package ru.citeck.ecos.webscripts.orgstruct;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.config.EcosConfigService;
import ru.citeck.ecos.model.DeputyModel;
import ru.citeck.ecos.model.EcosModel;
import ru.citeck.ecos.model.OrgStructModel;
import ru.citeck.ecos.orgstruct.OrgMetaService;
import ru.citeck.ecos.orgstruct.OrgStructService;
import ru.citeck.ecos.search.ftsquery.FTSQuery;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
    private static final String PARAM_EXCLUDE_AUTHORITIES = "excludeAuthorities";

    private static final String CONFIG_KEY_SHOW_INACTIVE = "orgstruct-show-inactive-user-only-for-admin";
    private static final String CONFIG_KEY_HIDE_IN_ORGSTRUCT = "hide-in-orgstruct";

    private static final String GROUP_PREFIX = "GROUP_";
    private static final int DEFAULT_RESULTS_LIMIT = 50;

    private NodeService nodeService;
    private SearchService searchService;
    private OrgMetaService orgMetaService;
    private OrgStructService orgStructService;
    private AuthorityService authorityService;
    private EcosConfigService ecosConfigService;
    private AuthenticationService authenticationService;

    private LoadingCache<FilterOptions, List<Pair<NodeRef, String>>> searchCache;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ChildrenGet(ServiceRegistry serviceRegistry,
                       EcosConfigService ecosConfigService,
                       OrgStructService orgStructService,
                       OrgMetaService orgMetaService) {

        this.orgMetaService = orgMetaService;
        this.orgStructService = orgStructService;
        this.ecosConfigService = ecosConfigService;
        this.nodeService = serviceRegistry.getNodeService();
        this.searchService = serviceRegistry.getSearchService();
        this.authorityService = serviceRegistry.getAuthorityService();
        this.authenticationService = serviceRegistry.getAuthenticationService();

        searchCache = CacheBuilder.newBuilder()
                                  .expireAfterWrite(5, TimeUnit.MINUTES)
                                  .maximumSize(200)
                                  .build(CacheLoader.from(options ->
                                          AuthenticationUtil.runAsSystem(() -> findAuthorities(options))
                                  ));
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        RequestParams params = getRequestParams(req);

        if (params.groupRef == null) {
            res.getWriter().write("Group " + params.filterOptions.rootGroup + " not found");
            res.setStatus(Status.STATUS_NOT_FOUND);
            return;
        }

        List<Pair<NodeRef, String>> users = getAuthorities(params);
        List<Authority> authorities = formatAuthorities(users);

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
        objectMapper.writeValue(res.getWriter(), authorities);
        res.setStatus(Status.STATUS_OK);
    }

    private List<Authority> formatAuthorities(List<Pair<NodeRef, String>> authorities) {

        List<Authority> result = new ArrayList<>();

        Map<String, Boolean> roleIsManager = new HashMap<>();

        List<NodeRef> roles = orgMetaService.getAllSubTypes("role");

        roles.forEach(ref -> {
            Map<QName, Serializable> props = nodeService.getProperties(ref);
            String name = (String) props.get(ContentModel.PROP_NAME);
            Boolean isManager = Boolean.TRUE.equals(props.get(OrgStructModel.PROP_ROLE_IS_MANAGER));
            roleIsManager.put(name, isManager);
        });

        for (Pair<NodeRef, String> authorityRefName : authorities) {

            Authority authority;
            if (authorityRefName.getSecond().startsWith(GROUP_PREFIX)) {
                authority = formatGroupAuthority(authorityRefName, roleIsManager);
            } else {
                authority = formatUserAuthority(authorityRefName);
            }
            authority.nodeRef = authorityRefName.getFirst().toString();
            authority.fullName = authorityRefName.getSecond();
            authority.shortName = authorityService.getShortName(authorityRefName.getSecond());

            result.add(authority);
        }
        return result;
    }

    private Authority formatUserAuthority(Pair<NodeRef, String> authority) {

        Map<QName, Serializable> props = nodeService.getProperties(authority.getFirst());

        UserAuthority result = new UserAuthority();

        result.setAvailable((Boolean) props.get(DeputyModel.PROP_AVAILABLE));
        result.setPersonDisabled((Boolean) props.get(EcosModel.PROP_IS_PERSON_DISABLED));

        result.firstName = (String) props.get(ContentModel.PROP_FIRSTNAME);
        result.lastName = (String) props.get(ContentModel.PROP_LASTNAME);
        result.displayName = result.firstName + " " + result.lastName;

        return result;
    }

    private Authority formatGroupAuthority(Pair<NodeRef, String> authorityRefName, Map<String, Boolean> roleIsManager) {

        Map<QName, Serializable> props = nodeService.getProperties(authorityRefName.getFirst());

        String branchType = (String) props.get(OrgStructModel.PROP_BRANCH_TYPE);
        String roleType = (String) props.get(OrgStructModel.PROP_ROLE_TYPE);

        Authority authority;

        if (StringUtils.isNotBlank(branchType)) {
            BranchAuthority branchAuthority = new BranchAuthority();
            branchAuthority.groupSubType = branchType;
            authority = branchAuthority;
        } else if (StringUtils.isNotBlank(roleType)) {
            RoleAuthority roleAuthority = new RoleAuthority();
            roleAuthority.groupSubType = roleType;
            roleAuthority.roleIsManager = roleIsManager.getOrDefault(roleType, false);
            authority = roleAuthority;
        } else {
            authority = new GroupAuthority();
        }

        authority.displayName = (String) props.get(ContentModel.PROP_AUTHORITY_DISPLAY_NAME);

        return authority;
    }

    private List<Pair<NodeRef, String>> getAuthorities(RequestParams params) {
        if (params.recurse && StringUtils.isNotBlank(params.filterOptions.filter)) {
            return searchCache.getUnchecked(params.filterOptions);
        }
        boolean immediate = !params.recurse;
        String rootGroup = params.filterOptions.rootGroup;
        Set<String> children = authorityService.getContainedAuthorities(null, rootGroup, immediate);
        return children.stream()
                       .map(this::getAuthorityNameRef)
                       .collect(Collectors.toList());
    }

    private List<Pair<NodeRef, String>> findAuthorities(FilterOptions filterOptions) {

        FTSQuery query = FTSQuery.createRaw()
                                 .eventual()
                                 .permissionsMode(PermissionEvaluationMode.NONE)
                                 .maxItems(100);

        boolean notEmpty = false;

        String filter = filterOptions.filter;
        if (filterOptions.group) {
            notEmpty = true;
            query.open()
                     .value(ContentModel.PROP_AUTHORITY_DISPLAY_NAME, filter).or()
                     .value(ContentModel.PROP_AUTHORITY_NAME, filter)
                 .close();
        }
        if (filterOptions.user) {
            if (notEmpty) {
                query.or();
            }
            query.open()
                 .value(ContentModel.PROP_USERNAME, filterOptions.filter)
                 .or();
            if (filterOptions.filterTokens.size() == 2) {
                List<String> tokens = new ArrayList<>(filterOptions.filterTokens);
                query.value(ContentModel.PROP_FIRSTNAME, tokens.get(0)).and()
                     .value(ContentModel.PROP_LASTNAME, tokens.get(1)).or()
                     .value(ContentModel.PROP_FIRSTNAME, tokens.get(1)).and()
                     .value(ContentModel.PROP_LASTNAME, tokens.get(0));
            } else {
                query.value(ContentModel.PROP_FIRSTNAME, filter).or()
                     .value(ContentModel.PROP_LASTNAME, filter);
            }
            query.close();
        }

        Map<String, Boolean> inRootGroupCache = new HashMap<>();

        return query.query(searchService)
                    .stream()
                    .map(this::getAuthorityNameRef)
                    .filter(auth -> filterAuthority(auth, filterOptions, inRootGroupCache))
                    .limit(filterOptions.limit)
                    .collect(Collectors.toList());
    }

    private boolean filterAuthority(Pair<NodeRef, String> authority,
                                    FilterOptions options,
                                    Map<String, Boolean> inRootGroupCache) {

        String authorityName = authority.getSecond();
        if (StringUtils.isBlank(authorityName)) {
            return false;
        }
        if (options.excludeAuthorities.contains(authorityName)) {
            return false;
        }
        if (!isInRootGroup(authorityName, options.rootGroup, inRootGroupCache)) {
            return false;
        }
        if (!options.showDisabled) {
            if (!authorityName.startsWith(GROUP_PREFIX) &&
                    !authenticationService.getAuthenticationEnabled(authorityName)) {
                return false;
            }
        }
        if (authorityName.startsWith(GROUP_PREFIX)) {
            String groupType = orgStructService.getGroupType(authorityName);
            if (StringUtils.isBlank(groupType)) {
                groupType = "group";
            }
            switch (groupType) {
                case "role":
                    if (!options.role) {
                        return false;
                    }
                    break;
                case "user":
                    if (!options.user) {
                        return false;
                    }
                    break;
                case "group":
                    if (!options.group) {
                        return false;
                    }
                    break;
                case "branch":
                    if (!options.branch) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    private Pair<NodeRef, String> getAuthorityNameRef(String name) {
        return new Pair<>(authorityService.getAuthorityNodeRef(name), name);
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
                        break;
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

    private Set<String> strToSet(String value) {
        if (StringUtils.isNotBlank(value)) {
            String[] arr = value.split(",");
            return new HashSet<>(Arrays.asList(arr));
        }
        return Collections.emptySet();
    }

    private Boolean strToBool(String value, Boolean def) {
        return StringUtils.isNotBlank(value) ? !Boolean.FALSE.toString().equals(value) : def;
    }

    private FilterOptions getFilterOptions(WebScriptRequest req) {

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String rootGroupName = templateVars.get(TEMPLATE_PARAM_GROUPNAME);

        if (StringUtils.isBlank(rootGroupName)) {
            throw new AlfrescoRuntimeException("Template parameter " + TEMPLATE_PARAM_GROUPNAME + " is undefined");
        }

        if (!rootGroupName.startsWith(GROUP_PREFIX)) {
            rootGroupName = GROUP_PREFIX + rootGroupName;
        }

        FilterOptions options = new FilterOptions();

        Boolean defaultEnabled = !Boolean.FALSE.toString().equals(req.getParameter(PARAM_DEFAULT));
        
        options.rootGroup = rootGroupName;
        options.role = strToBool(req.getParameter(PARAM_ROLE), defaultEnabled);
        options.user = strToBool(req.getParameter(PARAM_USER), defaultEnabled);
        options.group = strToBool(req.getParameter(PARAM_GROUP), defaultEnabled);
        options.branch = strToBool(req.getParameter(PARAM_BRANCH), defaultEnabled);
        options.limit = DEFAULT_RESULTS_LIMIT;
        options.subTypes = strToSet(req.getParameter(PARAM_SUB_TYPES));

        Function<String, String> addStars = str -> {
            if (StringUtils.isNotBlank(str)) {
                if (!str.startsWith("*")) {
                    str = "*" + str;
                }
                if (!str.endsWith("*")) {
                    str = str + "*";
                }
            }
            return str;
        };

        String filter = req.getParameter(PARAM_FILTER);

        options.filter = addStars.apply(filter);

        if (StringUtils.isNotBlank(filter)) {
            options.filterTokens =
                    Arrays.stream(options.filter.split("(?<!\\\\) "))
                          .map(addStars)
                          .collect(Collectors.toSet());
        } else {
            options.filterTokens = Collections.emptySet();
        }

        Boolean showInactiveOnlyForAdmin =
                strToBool((String) ecosConfigService.getParamValue(CONFIG_KEY_SHOW_INACTIVE), null);

        if (showInactiveOnlyForAdmin == null || !showInactiveOnlyForAdmin) {
            options.showDisabled = strToBool(req.getParameter(PARAM_SHOW_DISABLED), defaultEnabled);
        } else {
            String currentAuthority = authenticationService.getCurrentUserName();
            options.showDisabled = StringUtils.isNotBlank(currentAuthority) &&
                                   authorityService.isAdminAuthority(currentAuthority);
        }

        Set<String> excludeAuthorities = new HashSet<>();
        excludeAuthorities.addAll(strToSet(req.getParameter(PARAM_EXCLUDE_AUTHORITIES)));
        excludeAuthorities.addAll(strToSet((String) ecosConfigService.getParamValue(CONFIG_KEY_HIDE_IN_ORGSTRUCT)));
        options.excludeAuthorities = excludeAuthorities;

        return options;
    }

    private static class RequestParams {
        boolean recurse;
        NodeRef groupRef;
        FilterOptions filterOptions;
    }

    private static class FilterOptions {

        boolean role;
        boolean user;
        boolean group;
        boolean branch;
        boolean showDisabled;
        int limit;
        String filter;
        Set<String> filterTokens;
        String rootGroup;
        Set<String> subTypes;
        Set<String> excludeAuthorities;

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            FilterOptions that = (FilterOptions) o;

            return branch == that.branch &&
                   role == that.role &&
                   group == that.group &&
                   user == that.user &&
                   showDisabled == that.showDisabled &&
                   limit == that.limit &&
                   Objects.equals(filter, that.filter) &&
                   Objects.equals(rootGroup, that.rootGroup) &&
                   subTypes.equals(that.subTypes) &&
                   excludeAuthorities.equals(that.excludeAuthorities);
        }

        @Override
        public int hashCode() {
            int result = (branch ? 1 : 0);
            result = 31 * result + (role ? 1 : 0);
            result = 31 * result + (group ? 1 : 0);
            result = 31 * result + (user ? 1 : 0);
            result = 31 * result + (showDisabled ? 1 : 0);
            result = 31 * result + Objects.hashCode(rootGroup);
            result = 31 * result + Objects.hashCode(filter);
            result = 31 * result + limit;
            result = 31 * result + subTypes.hashCode();
            result = 31 * result + excludeAuthorities.hashCode();
            return result;
        }
    }

    private static class Authority {

        public String nodeRef;
        public String fullName;
        public String shortName;
        public String displayName;

        public final String authorityType;

        public Authority(String authorityType) {
            this.authorityType = authorityType;
        }
    }

    private static class UserAuthority extends Authority {

        public String firstName;
        public String lastName;

        public boolean available = true;
        public boolean isPersonDisabled = false;

        UserAuthority() {
            super(AuthorityType.USER.toString());
        }

        void setAvailable(Boolean value) {
            if (value != null) {
                this.available = value;
            }
        }

        void setPersonDisabled(Boolean value) {
            if (value != null) {
                this.isPersonDisabled = value;
            }
        }
    }

    private static class GroupAuthority extends Authority {

        public final String groupType;

        GroupAuthority(String type) {
            super(AuthorityType.GROUP.toString());
            groupType = type;
        }
        GroupAuthority() {
            this("group");
        }
    }

    private static class BranchAuthority extends GroupAuthority {

        public String groupSubType;

        BranchAuthority() {
            super("branch");
        }
    }

    private static class RoleAuthority extends GroupAuthority {

        public String groupSubType;
        public boolean roleIsManager;

        RoleAuthority() {
            super("role");
        }
    }
}
