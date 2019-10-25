package ru.citeck.ecos.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.action.node.NodeActionDefinition;
import ru.citeck.ecos.action.node.NodeActionsProvider;
import ru.citeck.ecos.action.node.NodeActionsService;
import ru.citeck.ecos.apps.app.module.type.type.action.ActionDto;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author deathNC on 30.04.2016.
 * @author Pavel Simonov
 */
@Slf4j
public class NodeActionsServiceImpl implements NodeActionsService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String PARAM_ACTION_ID = "actionId";
    private static final String PARAM_ACTION_TITLE = "title";
    private static final String PARAM_ACTION_TYPE = "actionType";

    private static final List<String> EXCLUDE_FROM_CONFIG = Arrays.asList(PARAM_ACTION_ID, PARAM_ACTION_TITLE,
            PARAM_ACTION_TYPE);

    private List<NodeActionsProvider> providerList = new ArrayList<>();

    private LoadingCache<Pair<String, NodeRef>, NodeActions> cache;
    private long cacheAge = 600;
    private boolean enableCache = true;

    private NodeService nodeService;

    public void init() {
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheAge, TimeUnit.SECONDS)
                .maximumSize(1000)
                .build(CacheLoader.from(this::getNodeActionsImpl));
    }

    public List<Map<String, String>> getNodeActionsRaw(NodeRef nodeRef) {

        NodeActions data;
        Pair<String, NodeRef> key = new Pair<>(AuthenticationUtil.getRunAsUser(), nodeRef);

        if (enableCache) {
            data = cache.getUnchecked(key);
            if (getLastModified(nodeRef).after(data.lastModified)) {
                cache.invalidate(key);
                data = cache.getUnchecked(key);
            }
        } else {
            data = getNodeActionsImpl(key);
        }

        return data.actionsData;
    }

    @Override
    public List<ActionDto> getNodeActions(NodeRef nodeRef) {
        List<Map<String, String>> rawActions = getNodeActionsRaw(nodeRef);

        List<ActionDto> result = new ArrayList<>();

        for (Map<String, String> actionRaw : rawActions) {
            ActionDto action = new ActionDto();
            action.setId(actionRaw.get("actionId"));
            action.setName(actionRaw.get("title"));
            action.setType(actionRaw.get("actionType"));

            Map<String, String> config = actionRaw.entrySet()
                    .stream()
                    .filter(x -> !EXCLUDE_FROM_CONFIG.contains(x.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            JsonNode configNode = OBJECT_MAPPER.convertValue(config, JsonNode.class);

            action.setConfig(configNode);
            result.add(action);
        }

        return result;
    }

    private NodeActions getNodeActionsImpl(Pair<String, NodeRef> userNode) {

        List<Map<String, String>> actionsData = new ArrayList<>();

        int id = 0;
        for (NodeActionsProvider provider : providerList) {
            List<ru.citeck.ecos.action.node.NodeActionDefinition> list = provider.getNodeActions(userNode.getSecond());
            for (NodeActionDefinition action : list) {
                action.setActionId(Integer.toString(id++));
                if (action.isValid()) {
                    String newTitle = I18NUtil.getMessage(action.getTitle());
                    if (newTitle != null) {
                        action.setTitle(newTitle);
                    }
                    actionsData.add(action.getProperties());
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, String> entry : action.getProperties().entrySet()) {
                        sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("; ");
                    }
                    log.warn("Server action is invalid. Properties: " + sb.toString());
                }
            }
        }

        return new NodeActions(getLastModified(userNode.getSecond()), actionsData);
    }

    private Date getLastModified(NodeRef nodeRef) {
        Date modified = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
        if (modified == null) {
            modified = new Date();
        }
        return modified;
    }

    public void clearCache(NodeRef nodeRef) {
        List<Pair<String, NodeRef>> keysToInvalidate = new ArrayList<>();
        cache.asMap().forEach((k, v) -> {
            if (k.getSecond().equals(nodeRef)) {
                keysToInvalidate.add(k);
            }
        });
        cache.invalidateAll(keysToInvalidate);
    }

    public void clearCache() {
        cache.invalidateAll();
    }

    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }

    public CacheStats getCacheStats() {
        return cache.stats();
    }

    public void addActionProvider(NodeActionsProvider actionsProvider) {
        providerList.add(actionsProvider);
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.nodeService = serviceRegistry.getNodeService();
    }

    public void setCacheAge(long cacheAge) {
        this.cacheAge = cacheAge;
    }

    private class NodeActions {

        final Date lastModified;
        final List<Map<String, String>> actionsData;

        NodeActions(Date lastModified, List<Map<String, String>> actionsData) {
            this.lastModified = lastModified;
            this.actionsData = actionsData;
        }
    }
}
