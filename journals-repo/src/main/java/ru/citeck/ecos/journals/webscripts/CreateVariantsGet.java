package ru.citeck.ecos.journals.webscripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.model.JournalsModel;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.utils.NodeUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Pavel Simonov
 */
public class CreateVariantsGet extends AbstractWebScript {

    /*=======PARAMS=======*/
    private static final String PARAM_SITE_ID = "site";
    private static final String PARAM_NODETYPE = "nodetype";
    private static final String PARAM_JOURNAL = "journal";
    private static final String PARAM_WRITABLE = "writable";
    /*======/PARAMS=======*/

    private LoadingCache<String, List<CreateVariant>> createVariantsByJournalType;
    private LoadingCache<String, List<CreateVariant>> createVariantsByNodeType;
    private LoadingCache<String, List<CreateVariant>> createVariantsBySite;
    private LoadingCache<NodeRef, CreateVariant> createVariantsData;

    private PermissionService permissionService;
    private SearchService searchService;
    private NodeService nodeService;
    private NodeUtils nodeUtils;

    private long cacheAgeSeconds = 600;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init(Container container, Description description) {
        super.init(container, description);

        createVariantsBySite = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheAgeSeconds, TimeUnit.SECONDS)
                .build(CacheLoader.from(this::getVariantsBySiteId));
        createVariantsByJournalType = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheAgeSeconds, TimeUnit.SECONDS)
                .build(CacheLoader.from(this::getVariantsByJournalId));
        createVariantsByNodeType = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheAgeSeconds, TimeUnit.SECONDS)
                .build(CacheLoader.from(this::getVariantsByNodeType));
        createVariantsData = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheAgeSeconds, TimeUnit.SECONDS)
                .build(CacheLoader.from(this::getCreateVariantData));
    }


    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        if (templateVars == null) {
            templateVars = Collections.emptyMap();
        }

        String siteId = templateVars.get(PARAM_SITE_ID);
        String nodeTypeId = templateVars.get(PARAM_NODETYPE);
        String journalId = templateVars.get(PARAM_JOURNAL);

        if(StringUtils.isBlank(siteId) && StringUtils.isBlank(nodeTypeId) && StringUtils.isBlank(journalId)) {
            throw new RuntimeException("Site or Nodetype or Journal ID should be specified");
        }

        String writableStr = req.getParameter(PARAM_WRITABLE);
        Boolean writable = writableStr != null ? Boolean.parseBoolean(writableStr) : null;

        List<CreateVariant> variants;

        MLPropertyInterceptor.setMLAware(true);

        if (StringUtils.isNotBlank(siteId)) {
            variants = createVariantsBySite.getUnchecked(siteId);
        } else if (StringUtils.isNotBlank(nodeTypeId)) {
            variants = createVariantsByNodeType.getUnchecked(nodeTypeId);
        } else {
            variants = createVariantsByJournalType.getUnchecked(journalId);
        }

        MLPropertyInterceptor.setMLAware(false);

        Response response = new Response();
        response.createVariants = variants.stream()
                                          .filter(v -> hasPermission(v.nodeRef, PermissionService.READ))
                                          .map(ResponseVariant::new)
                                          .filter(v -> writable == null || writable.equals(v.canCreate))
                                          .collect(Collectors.toList());
        response.siteId = siteId;

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");

        Cache cache = new Cache();
        cache.setMaxAge(cacheAgeSeconds);
        res.setCache(cache);

        objectMapper.writeValue(res.getWriter(), response);

        res.setStatus(Status.STATUS_OK);
    }

    private boolean hasPermission(NodeRef nodeRef, String permission) {
        if (nodeRef != null) {
            AccessStatus accessStatus = permissionService.hasPermission(nodeRef, permission);
            return AccessStatus.ALLOWED.equals(accessStatus);
        }
        return false;
    }

    private List<CreateVariant> getVariantsBySiteId(String siteId) {
        return AuthenticationUtil.runAsSystem(() ->
            FTSQuery.create()
                    .type(JournalsModel.TYPE_JOURNALS_LIST).and()
                    .value(ContentModel.PROP_NAME, "site-" + siteId + "-main")
                    .transactional()
                    .query(searchService)
                    .stream()
                    .flatMap(listRef -> nodeService.getTargetAssocs(listRef, JournalsModel.ASSOC_JOURNALS).stream())
                    .flatMap(assocRef -> nodeService.getChildAssocs(assocRef.getTargetRef(),
                                                                    JournalsModel.ASSOC_CREATE_VARIANTS,
                                                                    RegexQNamePattern.MATCH_ALL).stream())
                    .map(variantRef -> createVariantsData.getUnchecked(variantRef.getChildRef()))
                    .collect(Collectors.toList())
        );
    }

    private List<CreateVariant> getVariantsByJournalId(String journalId) {
        return AuthenticationUtil.runAsSystem(() ->
            FTSQuery.create()
                    .type(JournalsModel.TYPE_JOURNAL).and()
                    .value(JournalsModel.PROP_JOURNAL_TYPE, journalId)
                    .transactional()
                    .query(searchService)
                    .stream()
                    .flatMap(journalRef -> nodeService.getChildAssocs(journalRef,
                                                                      JournalsModel.ASSOC_CREATE_VARIANTS,
                                                                      RegexQNamePattern.MATCH_ALL).stream())
                    .map(variantRef -> createVariantsData.getUnchecked(variantRef.getChildRef()))
                    .collect(Collectors.toList())
        );
    }

    private List<CreateVariant> getVariantsByNodeType(String nodeType) {
        return AuthenticationUtil.runAsSystem(() ->
            FTSQuery.create()
                    .type(JournalsModel.TYPE_CREATE_VARIANT).and()
                    .value(JournalsModel.PROP_TYPE, nodeType)
                    .transactional()
                    .query(searchService)
                    .stream()
                    .map(createVariantsData::getUnchecked)
                    .collect(Collectors.toList())
        );
    }

    private CreateVariant getCreateVariantData(NodeRef variant) {

        Map<QName, Serializable> properties = nodeService.getProperties(variant);

        CreateVariant result = new CreateVariant(variant);
        MLText title = (MLText) properties.get(ContentModel.PROP_TITLE);
        if (title == null) {
            title = new MLText();
            title.put(Locale.US, (String) properties.get(ContentModel.PROP_NAME));
        }
        result.title = title;

        List<NodeRef> destinations = nodeUtils.getAssocTargets(variant, JournalsModel.ASSOC_DESTINATION);
        result.destination = destinations.size() > 0 ? destinations.get(0) : null;
        result.type = (String) properties.get(JournalsModel.PROP_TYPE);
        result.formId = (String) properties.get(JournalsModel.PROP_FORM_ID);

        Boolean isDefault = (Boolean) properties.get(JournalsModel.PROP_IS_DEFAULT);
        result.isDefault = Boolean.TRUE.equals(isDefault);

        return result;
    }

    public void clearCache() {
        createVariantsByJournalType.invalidateAll();
        createVariantsByNodeType.invalidateAll();
        createVariantsBySite.invalidateAll();
        createVariantsData.invalidateAll();
    }

    public Map<String, CacheStats> getCacheStats() {
        Map<String, CacheStats> result = new HashMap<>();
        result.put("createVariantsByJournalType", createVariantsByJournalType.stats());
        result.put("createVariantsByNodeType", createVariantsByNodeType.stats());
        result.put("createVariantsBySite", createVariantsBySite.stats());
        result.put("createVariantsData", createVariantsData.stats());
        return result;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        permissionService = serviceRegistry.getPermissionService();
        searchService = serviceRegistry.getSearchService();
        nodeService = serviceRegistry.getNodeService();
    }

    @Autowired
    public void setNodeUtils(NodeUtils nodeUtils) {
        this.nodeUtils = nodeUtils;
    }

    public void setCacheAgeSeconds(long cacheAgeSeconds) {
        this.cacheAgeSeconds = cacheAgeSeconds;
    }

    public class Response {
        public String siteId;
        public List<ResponseVariant> createVariants;
    }

    public class ResponseVariant {

        public final String title;
        public final String destination;
        public final String type;
        public final String formId;
        public final boolean isDefault;
        public final boolean canCreate;

        public ResponseVariant(CreateVariant source) {
            title = source.title.getClosestValue(I18NUtil.getLocale());
            destination = source.destination != null ? source.destination.toString() : null;
            type = source.type;
            formId = source.formId;
            isDefault = source.isDefault;
            canCreate = hasPermission(source.destination, PermissionService.CREATE_CHILDREN);
        }
    }

    public class CreateVariant {

        public final NodeRef nodeRef;
        public MLText title;

        public NodeRef destination;
        public String type;
        public String formId;
        public boolean isDefault;

        public CreateVariant(NodeRef nodeRef) {
            this.nodeRef = nodeRef;
        }
    }
}
