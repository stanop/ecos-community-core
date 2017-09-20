package ru.citeck.ecos.cmmn;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;
import ru.citeck.ecos.cmmn.model.*;
import ru.citeck.ecos.model.*;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Provides Marshaller and Unmarshaller for mappings in CMMN standard v1.1
 * 2016-06-22
 *
 * @author deathNC
 * @author Maxim Strizhov (maxim.strizhov@citeck.ru)
 */
public class CMMNUtils {

    private static final String TRANSACTION_ID_BY_NODEREF = CMMNUtils.class.getName() + ".id-storage";
    private static final String TRANSACTION_ID_COUNTER = CMMNUtils.class.getName() + ".id-counter";

    private static final Pattern NODE_REF_PATTERN =
            Pattern.compile("(?ui)(workspace://SpacesStore/)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    public static final Logger log = LoggerFactory.getLogger(CMMNUtils.class);

    private static final String SCHEMA_LOCATION = "alfresco/module/ecos-enterprise-repo/cmmn/";
    private static final String[] SCHEMA_FILES = {
            "CMMN11.xsd",
            "CMMN11CaseModel.xsd",
            "CMMNDI11.xsd",
            "DC.xsd",
            "DI.xsd"
    };

    private static final String CASE_ROLES_ROOT_PATH = "app:company_home/app:dictionary/cm:dataLists/cm:case-role";

    public static final String NAMESPACE = "http://www.citeck.ru/ecos/case/cmmn/1.0";

    public static final QName QNAME_NODE_TYPE = new QName(NAMESPACE, "nodeType");
    public static final QName QNAME_SOURCE_ID = new QName(NAMESPACE, "sourceId");

    public static final QName QNAME_TITLE = new QName(NAMESPACE, "title");
    public static final QName QNAME_DESCRIPTION = new QName(NAMESPACE, "description");
    public static final QName QNAME_NEW_ID = new QName(NAMESPACE, "newId");
    public static final QName QNAME_ORIGINAL_EVENT = new QName(NAMESPACE, "originalEvent");
    public static final QName QNAME_IS_RESTART_EVENT = new QName(NAMESPACE, "isRestartEvent");
    public static final QName QNAME_COMPLETNESS_LEVELS = new QName(NAMESPACE, "completnessLevels");
    public static final QName QNAME_START_COMPLETNESS_LEVELS = new QName(NAMESPACE, "startCompletnessLevels");
    public static final QName QNAME_STOP_COMPLETNESS_LEVELS = new QName(NAMESPACE, "stopCompletnessLevels");
    public static final QName QNAME_ROLE_ASSIGNEES = new QName(NAMESPACE, "roleAssignees");
    public static final QName QNAME_ELEMENT_TYPES = new QName(NAMESPACE, "elementTypes");
    public static final QName QNAME_REFERENSE_ROLE = new QName(NAMESPACE, "referenceRoleAssoc");

    public static final QName QNAME_CASE_STATUS = new QName(NAMESPACE, "caseStatus");
    public static final QName QNAME_ACTION_CASE_STATUS = new QName(NAMESPACE, "actionCaseStatus");

    public static final QName QNAME_ENTRY_SENTRY = new QName(NAMESPACE, "entrySentry");
    public static final QName QNAME_EXIT_SENTRY = new QName(NAMESPACE, "exitSentry");

    private static final QName QNAME_UE_CONFIRMATION_MESSAGE = new QName(NAMESPACE, "confirmationMessage");
    private static final QName QNAME_UE_ADDITIONAL_DATA_TYPE = new QName(NAMESPACE, "additionalDataType");

    private static final QName QNAME_CASE_TYPE = new QName(NAMESPACE, "caseType");
    private static final QName QNAME_CASE_ECOS_TYPE = new QName(NAMESPACE, "caseEcosType");
    private static final QName QNAME_CASE_ECOS_KIND = new QName(NAMESPACE, "caseEcosKind");
    private static final QName QNAME_CASE_CONDITION = new QName(NAMESPACE, "condition");

    private static final QName QNAME_CONFIRMERS = new QName(NAMESPACE, "confirmers");
    private static final QName QNAME_PERFORMER = new QName(NAMESPACE, "performer");
    private static final QName QNAME_PERFORMERS = new QName(NAMESPACE, "performers");
    private static final QName QNAME_PERFORMERS_ROLES = new QName(NAMESPACE, "performersRoles");
    private static final QName QNAME_CONTROLLER = new QName(NAMESPACE, "controller");

    private static final QName QNAME_ROLE_VARNAME = new QName(NAMESPACE, "roleVarName");
    private static final QName QNAME_ROLE_REFERENSE = new QName(NAMESPACE, "isReferenceRole");
    private static final QName QNAME_ROLE_SCRIPT = new QName(NAMESPACE, "roleScript");

    public static final Map<QName, org.alfresco.service.namespace.QName> ROLES_ATTRIBUTES_MAPPING;
    public static final Map<QName, org.alfresco.service.namespace.QName> CASE_ATTRIBUTES_MAPPING;
    public static final Map<QName, org.alfresco.service.namespace.QName> ROLES_ASSOCS_MAPPING;
    public static final Map<QName, org.alfresco.service.namespace.QName> STATUS_ASSOCS_MAPPING;
    public static final Map<QName, org.alfresco.service.namespace.QName> EVENT_PROPS_MAPPING;

    private static NodeRef caseRolesRoot;

    private static ServiceRegistry serviceRegistry;
    private static DictionaryService dictionaryService;
    private static NodeService nodeService;

    static {
        Map<QName, org.alfresco.service.namespace.QName> mapping = new HashMap<>();
        mapping.put(QNAME_ROLE_VARNAME, ICaseRoleModel.PROP_VARNAME);
        mapping.put(QNAME_ROLE_REFERENSE, ICaseRoleModel.PROP_IS_REFERENCE_ROLE);
        mapping.put(QNAME_ROLE_SCRIPT, ICaseRoleModel.PROP_SCRIPT);
        ROLES_ATTRIBUTES_MAPPING = Collections.unmodifiableMap(mapping);

        mapping = new HashMap<>();
        mapping.put(QNAME_CASE_TYPE, ICaseModel.PROP_CASE_TYPE);
        mapping.put(QNAME_CASE_CONDITION, ICaseModel.PROP_CONDITION);
        mapping.put(QNAME_CASE_ECOS_TYPE, ICaseModel.PROP_CASE_ECOS_TYPE);
        mapping.put(QNAME_CASE_ECOS_KIND, ICaseModel.PROP_CASE_ECOS_KIND);
        CASE_ATTRIBUTES_MAPPING = Collections.unmodifiableMap(mapping);

        mapping = new HashMap<>();
        mapping.put(QNAME_CONFIRMERS, ICaseTaskModel.ASSOC_CONFIRMERS);
        mapping.put(QNAME_PERFORMER, ICaseTaskModel.ASSOC_PERFORMER);
        mapping.put(QNAME_PERFORMERS, ICaseTaskModel.ASSOC_PERFORMERS);
        mapping.put(QNAME_CONTROLLER, ICaseTaskModel.ASSOC_CONTROLLER);
        mapping.put(QNAME_PERFORMERS_ROLES, CasePerformModel.ASSOC_PERFORMERS_ROLES);
        mapping.put(QNAME_REFERENSE_ROLE, ICaseRoleModel.ASSOC_REFERENCE_ROLE);
        ROLES_ASSOCS_MAPPING = Collections.unmodifiableMap(mapping);

        mapping = new HashMap<>();
        mapping.put(QNAME_UE_ADDITIONAL_DATA_TYPE, EventModel.PROP_ADDITIONAL_DATA_TYPE);
        mapping.put(QNAME_UE_CONFIRMATION_MESSAGE, EventModel.PROP_CONFIRMATION_MESSAGE);
        EVENT_PROPS_MAPPING = Collections.unmodifiableMap(mapping);

        mapping = new HashMap<>();
        mapping.put(QNAME_CASE_STATUS, StagesModel.ASSOC_CASE_STATUS);
        mapping.put(QNAME_ACTION_CASE_STATUS, ActionModel.SetCaseStatus.PROP_STATUS);
        STATUS_ASSOCS_MAPPING = Collections.unmodifiableMap(mapping);
    }

    public static String extractIdFromNodeRef(NodeRef nodeRef) {
        return nodeRef.toString().replaceAll("workspace://SpacesStore/", "workspace-SpacesStore-");
    }

    public static NodeRef extractNodeRefFromCmmnId(String id) {
        return new NodeRef(convertCmmnIdToNodeRefString(id));
    }

    public static String convertCmmnIdToNodeRefString(String id) {
        return id.replaceAll("workspace-SpacesStore-", "workspace://SpacesStore/");
    }

    public static String convertNodeRefToId(NodeRef nodeRef) {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        Map<NodeRef, String> idByNodeRef = TransactionalResourceHelper.getMap(TRANSACTION_ID_BY_NODEREF);
        String id = idByNodeRef.get(nodeRef);
        if (id == null) {
            id = getNextDocumentId();
            idByNodeRef.put(nodeRef, id);
        }
        return id;
    }

    public static String getNextDocumentId() {
        return String.format("id-%d", TransactionalResourceHelper.incrementCount(TRANSACTION_ID_COUNTER));
    }

    public static Unmarshaller createUnmarshaller(String packageName) throws JAXBException, SAXException, IOException {
        JAXBContext jaxbContext = JAXBContext.newInstance(packageName);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(getCmmnSchema());
        return unmarshaller;
    }

    public static Marshaller createMarshaller(String packageName) throws JAXBException, SAXException, IOException {
        JAXBContext context = JAXBContext.newInstance(packageName);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setSchema(getCmmnSchema());
        return marshaller;
    }

    public static Serializable convertValueForRepo(org.alfresco.service.namespace.QName propertyName, String value) {

        if (StringUtils.isEmpty(value)) {
            return null;
        }

        PropertyDefinition property = dictionaryService.getProperty(propertyName);
        org.alfresco.service.namespace.QName dataType = property.getDataType().getName();

        if (dataType.equals(DataTypeDefinition.CATEGORY) || dataType.equals(DataTypeDefinition.NODE_REF)) {
            if (!NodeRef.isNodeRef(value)) {
                value = convertCmmnIdToNodeRefString(value);
                if (!NodeRef.isNodeRef(value)) {
                    log.error(String.format("Property '%s' has type '%s' but value isn't a NodeRef. value = '%s'",
                                                                    dataType, propertyName.getPrefixString(), value));
                    return null;
                }
            }
            NodeRef ref = new NodeRef(value);
            if (!nodeService.exists(ref)) {
                log.error(String.format("NodeRef '%s' doesn't exists", value));
                return null;
            }
            return ref;
        } else if (dataType.equals(DataTypeDefinition.QNAME)) {
            return org.alfresco.service.namespace.QName.createQName(value);
        } else if (dataType.equals(DataTypeDefinition.TEXT)) {
            return value;
        } else if (dataType.equals(DataTypeDefinition.BOOLEAN)) {
            return Boolean.valueOf(value);
        }
        log.error(String.format("Type '%s' isn't supported by CaseImportService. Property = '%s' value = '%s'",
                                                                dataType, propertyName.toPrefixString(), value));
        return null;
    }

    public static String convertValueForCmmn(org.alfresco.service.namespace.QName propertyName, Serializable value) {

        if (value == null) {
            return null;
        }

        PropertyDefinition property = dictionaryService.getProperty(propertyName);
        org.alfresco.service.namespace.QName dataType = property.getDataType().getName();

        if (dataType.equals(DataTypeDefinition.CATEGORY) || dataType.equals(DataTypeDefinition.NODE_REF)) {
            return extractIdFromNodeRef((NodeRef)value);
        }

        return value.toString();
    }

    public static String elementsToString(Iterable<? extends TCmmnElement> elements) {
        StringBuilder sb = new StringBuilder();
        for (TCmmnElement element : elements) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(element.getId());
        }
        return sb.toString();
    }

    public static <T> List<T> stringToElements(String str, Map<String, T> mapping) {
        if (StringUtils.isBlank(str)) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>();
        for (String elementStr : str.split(",")) {
            if (mapping.containsKey(elementStr)) {
                result.add(mapping.get(elementStr));
            } else {
                log.error("Element " + elementStr + " not found. str: " + str + " mapping: " + mapping);
            }
        }
        return result;
    }

    public static List<Sentry> criterionToSentries(List<? extends TCriterion> criterion) {
        List<Sentry> result = new ArrayList<>();
        for (TCriterion c : criterion) {
            Object sentry = c.getSentryRef();
            if (sentry instanceof Sentry) {
                result.add((Sentry) sentry);
            }
        }
        return result;
    }

    private static Schema getCmmnSchema() throws JAXBException, SAXException, IOException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        schemaFactory.setResourceResolver(new LSResourceResolver() {
            @Override
            public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
                return new CmmnLSInput(type, namespaceURI, publicId, systemId, baseURI) {
                    @Override
                    protected byte[] getXsdData(String systemId) {
                        ClassPathResource schemaResource = new ClassPathResource(SCHEMA_LOCATION + systemId);
                        try {
                            InputStream in = schemaResource.getInputStream();
                            byte[] result = new byte[in.available()];
                            if (in.read(result) > 0) return result;
                        } catch (IOException e) {
                            log.error("Cannot read schema: " + systemId, e);
                        }
                        return null;
                    }
                };
            }
        });
        StreamSource[] schemaSources = new StreamSource[SCHEMA_FILES.length];
        for (int i = 0; i < SCHEMA_FILES.length; ++i) {
            ClassPathResource schemaResource = new ClassPathResource(SCHEMA_LOCATION + SCHEMA_FILES[i]);
            schemaSources[i] = new StreamSource(schemaResource.getInputStream());
            schemaSources[i].setSystemId(SCHEMA_FILES[i]);
            schemaSources[i].setPublicId(SCHEMA_FILES[i]);
        }
        return schemaFactory.newSchema(schemaSources);
    }

    public static boolean isTask(JAXBElement<? extends TPlanItemDefinition> element) {
        return element.getValue().getClass().equals(TTask.class);
    }

    public static boolean isProcessTask(JAXBElement<? extends TPlanItemDefinition> element) {
        return element.getValue().getClass().equals(TProcessTask.class);
    }

    public static boolean isStage(JAXBElement<? extends TPlanItemDefinition> element) {
        return element.getValue().getClass().equals(Stage.class);
    }

    public static boolean isTimer(JAXBElement<? extends TPlanItemDefinition> element) {
        return element.getValue().getClass().equals(TTimerEventListener.class);
    }

    public static QName convertToXMLQName(org.alfresco.service.namespace.QName alfrescoQName) {
        return new QName(alfrescoQName.getNamespaceURI(), alfrescoQName.getLocalName().replace(':', '.'));
    }

    public static org.alfresco.service.namespace.QName convertFromXMLQName(QName qName) {
        return org.alfresco.service.namespace.QName.createQName(qName.getNamespaceURI(), qName.getLocalPart().replace('.', ':'));
    }

    private static NodeRef getByPath(String path) {
        NamespaceService namespaceService = serviceRegistry.getNamespaceService();
        SearchService searchService = serviceRegistry.getSearchService();

        NodeRef storeRoot = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<NodeRef> nodeRefs = searchService.selectNodes(storeRoot, path, null, namespaceService, false);
        NodeRef result = nodeRefs != null && nodeRefs.size() > 0 ? nodeRefs.get(0) : null;

        if (result == null) {
            log.warn("Node doesn't found! Path: " + path);
        }
        return result;
    }

    public static NodeRef getCaseRolesRoot() {
        if (caseRolesRoot == null) {
            caseRolesRoot = getByPath(CASE_ROLES_ROOT_PATH);
        }
        return caseRolesRoot;
    }

    public static NodeRef getCaseRoleById(String id) {
        if (id == null) {
            return null;
        }
        NodeRef root = getCaseRolesRoot();
        if (root == null) {
            return null;
        }
        List<ChildAssociationRef> roles = nodeService.getChildAssocs(root, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef roleAssoc : roles) {
            String roleId = (String) nodeService.getProperty(roleAssoc.getChildRef(), ICaseRoleModel.PROP_VARNAME);
            if (Objects.equals(id, roleId)) {
                return roleAssoc.getChildRef();
            }
        }
        return null;
    }

    public static void setServiceRegistry(ServiceRegistry serviceRegistry) {
        CMMNUtils.serviceRegistry = serviceRegistry;
        dictionaryService = serviceRegistry.getDictionaryService();
        nodeService = serviceRegistry.getNodeService();
    }
}
