package ru.citeck.ecos.webscripts.node;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import ru.citeck.ecos.dto.ChildAssociationDto;

import java.io.Serializable;
import java.util.*;

/**
 * Document child associations get web script
 */
public class NodeChildAssociationsGet extends DeclarativeWebScript {

    private static final String DELIMITER = ",";

    /**
     * Request params
     */
    private static final String NODE_REF = "nodeRef";
    private static final String ASSOC_TYPE = "assocType";
    private static final String CHILD_NAMES = "childNames";

    /**
     * Services
     */
    private NodeService nodeService;

    @Autowired
    private ServiceRegistry serviceRegistry;


    /**
     * Execute implementation
     * @param req Http-request
     * @param status Status
     * @param cache Cache
     * @return Map of attributes
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        String nodeRefUuid = req.getParameter(NODE_REF);
        String assocType = req.getParameter(ASSOC_TYPE);
        String childNamesValue = req.getParameter(CHILD_NAMES);
        String childNames[] = new String[]{};
        if (!StringUtils.isEmpty(childNamesValue)) {
            childNames = childNamesValue.split(DELIMITER);
        }
        /** Load node reference */
        NodeRef nodeRef = new NodeRef(nodeRefUuid);
        if (!nodeService.exists(nodeRef) || StringUtils.isEmpty(assocType)) {
            return new HashMap<>();
        }
        Map<String, Object> result = new HashMap<>();
        result.put("nodeExists", true);
        /** Load association */
        QName assocQName = parseQName(assocType);
        List<ChildAssociationRef> associations = getChildAssociations(nodeRef, assocQName);
        result.put("childAssociations", buildChildAssociations(nodeRef, associations, childNames));
        /** Load associations of child associations */
        return result;
    }

    /**
     * Build child associations
     * @param topNodeRef Top node reference
     * @param associations Child associations
     * @param childNames Child names
     * @return List of child associations data transfer object
     */
    private List<ChildAssociationDto> buildChildAssociations(NodeRef topNodeRef, List<ChildAssociationRef> associations,
                                                             String childNames[]) {
        List<ChildAssociationDto> childAssociationDtos = new ArrayList<>(associations.size());
        Set<QName> childQNames = parseQNames(childNames);
        /** Build associations */
        for (ChildAssociationRef childAssociationRef : associations) {
            NodeRef childNodeRef = childAssociationRef.getChildRef();
            if (childNodeRef == null) {
                continue;
            }
            ChildAssociationDto associationDto = new ChildAssociationDto();
            associationDto.setParentRef(topNodeRef.toString());
            associationDto.setNodeRef(childNodeRef.toString());
            associationDto.setProperties(transformProperties(nodeService.getProperties(childNodeRef)));
            /** Child associations */
            List<ChildAssociationRef> childChildAssociationRefs = getChildAssociations(childNodeRef, childQNames);
            List<AbstractMap.SimpleEntry<QName, ChildAssociationDto>> dtoAccos = new ArrayList<>();
            for (ChildAssociationRef childChildAssocRef : childChildAssociationRefs) {
                NodeRef childChildRef = childChildAssocRef.getChildRef();
                if (childChildRef == null) {
                    continue;
                }
                ChildAssociationDto childChildDto = new ChildAssociationDto();
                childChildDto.setNodeRef(childChildRef.toString());
                childChildDto.setParentRef(childNodeRef.toString());
                childChildDto.setProperties(transformProperties(nodeService.getProperties(childChildRef)));
                dtoAccos.add(new AbstractMap.SimpleEntry<QName, ChildAssociationDto>(
                        childChildAssocRef.getTypeQName(),
                        childChildDto
                ));
            }
            associationDto.setChildAssociations(dtoAccos);
            childAssociationDtos.add(associationDto);
        }
        return childAssociationDtos;
    }

    /**
     * Parse qnames
     * @param qnames Raw qnames
     * @return Set of qnames
     */
    private Set<QName> parseQNames(String[] qnames) {
        if (qnames == null) {
            return Collections.emptySet();
        } else {
            Set<QName> result = new HashSet<>();
            for (String rawQName : qnames) {
                QName qName = parseQName(rawQName);
                if (qName != null) {
                    result.add(parseQName(rawQName));
                }
            }
            return result;
        }
    }

    /**
     * Parse raw qname
     * @param rawQName Raw qname value
     * @return QName
     */
    private QName parseQName(String rawQName) {
        try {
            return QName.createQName(rawQName, serviceRegistry.getNamespaceService());
        } catch (NamespaceException e) {
            return null;
        }
    }

    /**
     * Get child associations
     * @param nodeRef Node reference
     * @param qName QName
     * @return List of associations
     */
    private List<ChildAssociationRef> getChildAssociations(NodeRef nodeRef, QName qName) {
        if (qName == null) {
            return Collections.emptyList();
        }
        List<ChildAssociationRef> result = new ArrayList<>();
        for (ChildAssociationRef childAssociation : nodeService.getChildAssocs(nodeRef)) {
            if (childAssociation.getTypeQName().equals(qName)) {
                result.add(childAssociation);
            }
        }
        return result;
    }

    /**
     * Get child associations
     * @param nodeRef Node reference
     * @param qNames QNames
     * @return List of associations
     */
    private List<ChildAssociationRef> getChildAssociations(NodeRef nodeRef, Set<QName> qNames) {
        if (CollectionUtils.isEmpty(qNames)) {
            return nodeService.getChildAssocs(nodeRef);
        }
        List<ChildAssociationRef> result = new ArrayList<>();
        for (ChildAssociationRef childAssociation : nodeService.getChildAssocs(nodeRef)) {
            for (QName qName : qNames) {
                if (childAssociation.getTypeQName().equals(qName)) {
                    result.add(childAssociation);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Transform properties to list of entries (dirty-dirty hack for freemarker)
     * @param properties Map of parameters
     * @return List of entries
     */
    private List<AbstractMap.SimpleEntry<QName, Serializable>> transformProperties(Map<QName, Serializable> properties) {
        List<AbstractMap.SimpleEntry<QName, Serializable>> result = new ArrayList<>(properties.size());
        for (QName qName : properties.keySet()) {
            result.add(new AbstractMap.SimpleEntry<QName, Serializable>(qName, properties.get(qName)));
        }
        return result;
    }

    /** Services setters */

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
