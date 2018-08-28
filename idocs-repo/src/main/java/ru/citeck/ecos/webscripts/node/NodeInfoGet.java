package ru.citeck.ecos.webscripts.node;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import lombok.Getter;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.model.QuickShareModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.rating.RatingService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.state.ItemsUpdateState;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class NodeInfoGet extends AbstractWebScript {

    //========PARAMS========
    private static final String PARAM_NODE_REF = "nodeRef";
    private static final String PARAM_INFO_TYPE = "infoType";
    //=======/PARAMS========

    private static final String INFO_FULL = "full";
    private static final String LIKES_SCHEME = "likesRatingScheme";
    private static final String PREF_DOCUMENT_FAVOURITES = "org.alfresco.share.documents.favourites";
    private static final String PREF_FOLDER_FAVOURITES = "org.alfresco.share.folders.favourites";

    private static final String[] PERMISSIONS = { "Read", "Write" };

    private ObjectMapper objectMapper = new ObjectMapper();

    private ItemsUpdateState updateState;
    private NodeService nodeService;
    private PersonService personService;
    private PermissionService permissionService;
    private VersionService versionService;
    private RatingService ratingService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private PreferenceService preferenceService;
    private MimetypeService mimetypeService;

    private Map<String, Method> fillMethods = new HashMap<>();

    private PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();

    @PostConstruct
    public void initMethods() {
        Method[] methods = getClass().getDeclaredMethods();
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("fill")) {
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 1 && params[0].equals(RequestContext.class)) {
                    String key = Character.toLowerCase(name.charAt(4)) + name.substring(5);
                    fillMethods.put(key, method);
                }
            }
        }
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        NodeRef nodeRef = new NodeRef(req.getParameter(PARAM_NODE_REF));
        Set<String> infoTypes = getInfoTypes(req);

        RequestContext context = new RequestContext(nodeRef);

        Map<String, Object> response = new HashMap<>();
        boolean full = infoTypes.contains(INFO_FULL);
        fillMethods.forEach((key, method) -> {
            try {
                if (full || infoTypes.contains(key)) {
                    response.put(key, method.invoke(this, context));
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });

        res.setContentType(Format.JSON.mimetype() + ";charset=UTF-8");
        objectMapper.writeValue(res.getOutputStream(), response);

        res.setStatus(Status.STATUS_OK);
    }

    private QShare fillQshare(RequestContext context) {
        QShare qshare = new QShare();
        qshare.sharedBy = (String) context.getProps().get(QuickShareModel.PROP_QSHARE_SHAREDBY);
        qshare.sharedId = (String) context.getProps().get(QuickShareModel.PROP_QSHARE_SHAREDID);
        return qshare;
    }

    private boolean fillIsFavourite(RequestContext context) {
        String currentUserName = AuthenticationUtil.getFullyAuthenticatedUser();
        String nodeStr = context.nodeRef.toString();

        return getFavorites(currentUserName, PREF_DOCUMENT_FAVOURITES).contains(nodeStr) ||
               getFavorites(currentUserName, PREF_FOLDER_FAVOURITES).contains(nodeStr);
    }

    private Set<String> getFavorites(String userName, String prefsKey) {
        Map<String, Serializable> prefs = preferenceService.getPreferences(userName, prefsKey);
        try {
            String value = (String) prefs.get(prefsKey);
            return new HashSet<>(Arrays.asList(value.split(",")));
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    private boolean fillIsContainer(RequestContext context) {
        QName type = context.getType();
        return dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER) &&
              !dictionaryService.isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER);
    }

    private String fillNodeType(RequestContext context) {
        return context.getType().toPrefixString(namespaceService);
    }

    private Likes fillLikes(RequestContext context) {
        int totalLikes = ratingService.getRatingsCount(context.nodeRef, LIKES_SCHEME);
        boolean isLiked;
        if (totalLikes == 0) {
            isLiked = false;
        } else {
            isLiked = ratingService.getRatingByCurrentUser(context.nodeRef, LIKES_SCHEME) != null;
        }
        Likes likes = new Likes();
        likes.totalLikes = totalLikes;
        likes.isLiked = isLiked;
        return likes;
    }

    private Integer fillCommentsCount(RequestContext context) {
        Integer count = (Integer) context.getProps().get(ForumModel.PROP_COMMENT_COUNT);
        if (count == null || count < 0) {
            count = 0;
        }
        return count;
    }

    private String fillFileExtension(RequestContext context) {
        String extension = "";
        if (dictionaryService.isSubClass(context.getType(), ContentModel.TYPE_CONTENT)) {
            String name = (String) context.getProps().get(ContentModel.PROP_NAME);
            String mimetype = mimetypeService.guessMimetype(name);
            extension = mimetypeService.getExtension(mimetype);
        }
        return extension;
    }

    private String fillDisplayName(RequestContext context) {
        String displayName = (String) context.getProps().get(ContentModel.PROP_TITLE);
        if (StringUtils.isBlank(displayName)) {
            displayName = (String) context.getProps().get(ContentModel.PROP_NAME);
        }
        return displayName;
    }

    private String fillVersion(RequestContext context) {
        Version version = versionService.getCurrentVersion(context.nodeRef);
        String versionLabel = null;
        if (version != null) {
            versionLabel = version.getVersionLabel();
        }
        if (StringUtils.isBlank(versionLabel)) {
            versionLabel = "1.0";
        }
        return versionLabel;
    }

    private boolean fillPendingUpdate(RequestContext context) {
        return updateState.isPendingUpdate(context.nodeRef);
    }

    private String fillModified(RequestContext context) {
        Date modified = (Date) context.getProps().get(ContentModel.PROP_MODIFIED);
        if (modified == null) {
            modified = new Date(0);
        }
        return ISO8601Utils.format(modified);
    }

    private Person fillModifier(RequestContext context) {

        String modifierUserName = (String) context.getProps().get(ContentModel.PROP_MODIFIER);
        Person modifier = new Person();
        modifier.userName = modifierUserName;

        if (StringUtils.isNotBlank(modifierUserName)) {

            NodeRef personRef = null;
            if (!AuthenticationUtil.SYSTEM_USER_NAME.equals(modifierUserName)) {
                personRef = personService.getPersonOrNull(modifierUserName);
            }

            Map<QName, Serializable> personProps;
            if (personRef != null) {
                personProps = nodeService.getProperties(personRef);
            } else {
                personProps = Collections.emptyMap();
            }

            modifier.firstName = (String) personProps.get(ContentModel.PROP_FIRSTNAME);
            modifier.lastName = (String) personProps.get(ContentModel.PROP_LASTNAME);

            StringBuilder display = new StringBuilder();

            if (StringUtils.isNotBlank(modifier.firstName)) {
                display.append(modifier.firstName);
            }
            if (StringUtils.isNotBlank(modifier.lastName)) {
                if (display.length() > 0) {
                    display.append(" ");
                }
                display.append(modifier.lastName);
            }
            if (display.length() == 0) {
                display.append(modifierUserName);
            }

            modifier.displayName = display.toString();
        }

        return modifier;
    }

    private Map<String, Boolean> fillPermissions(RequestContext context) {
        Map<String, Boolean> permissions = new HashMap<>();
        for (String perm : PERMISSIONS) {
            AccessStatus status = permissionService.hasPermission(context.nodeRef, perm);
            permissions.put(perm, AccessStatus.ALLOWED == status);
        }
        return permissions;
    }

    private Set<String> getInfoTypes(WebScriptRequest request) {
        String infoKind = request.getParameter(PARAM_INFO_TYPE);
        String[] infoKinds;
        if (StringUtils.isBlank(infoKind)) {
            infoKinds = new String[] { INFO_FULL };
        } else {
            infoKinds = infoKind.split(",");
        }
        return new HashSet<>(Arrays.asList(infoKinds));
    }

    @Autowired
    @Qualifier("ecos.itemsUpdateState")
    public void setUpdateState(ItemsUpdateState updateState) {
        this.updateState = updateState;
    }

    @Autowired
    public void setPreferenceService(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.nodeService = serviceRegistry.getNodeService();
        this.personService = serviceRegistry.getPersonService();
        this.permissionService = serviceRegistry.getPermissionService();
        this.versionService = serviceRegistry.getVersionService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.ratingService = serviceRegistry.getRatingService();
        this.dictionaryService = serviceRegistry.getDictionaryService();
        this.mimetypeService = serviceRegistry.getMimetypeService();
    }

    private class RequestContext {

        final NodeRef nodeRef;

        @Getter(lazy = true)
        private final Map<QName, Serializable> props = evalProps();
        @Getter(lazy = true)
        private final QName type = evalType();

        RequestContext(NodeRef nodeRef) {
            this.nodeRef = nodeRef;
        }

        QName evalType() {
            return nodeService.getType(nodeRef);
        }

        Map<QName, Serializable> evalProps() {
            return nodeService.getProperties(nodeRef);
        }
    }

    private static class Person {
        public String userName;
        public String firstName;
        public String lastName;
        public String displayName;
    }

    private static class Likes {
        public boolean isLiked;
        public int totalLikes;
    }

    private static class QShare {
        public String sharedBy;
        public String sharedId;
    }
}