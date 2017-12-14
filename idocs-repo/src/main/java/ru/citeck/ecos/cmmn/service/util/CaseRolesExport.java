package ru.citeck.ecos.cmmn.service.util;

import lombok.Setter;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import ru.citeck.ecos.cmmn.CMMNUtils;
import ru.citeck.ecos.cmmn.model.CaseRoles;
import ru.citeck.ecos.cmmn.model.Role;
import ru.citeck.ecos.model.ICaseModel;
import ru.citeck.ecos.model.ICaseRoleModel;
import ru.citeck.ecos.model.ICaseTemplateModel;
import ru.citeck.ecos.utils.RepoUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static ru.citeck.ecos.model.ICaseTemplateModel.ASSOC_ELEMENT_CONFIG;

/**
 * @author deathNC
 */
public class CaseRolesExport {

    private static final Logger logger = Logger.getLogger(CaseRolesExport.class);
    private final NodeService nodeService;
    private final DictionaryService dictionaryService;
    private final CMMNUtils utils;

    public CaseRolesExport(NodeService nodeService, DictionaryService dictionaryService, CMMNUtils utils) {
        this.nodeService = nodeService;
        this.dictionaryService = dictionaryService;
        this.utils = utils;
    }

    public CaseRoles getRoles(NodeRef caseNodeRef) {
        CaseRoles caseRoles = new CaseRoles();
        if (nodeService.exists(caseNodeRef)) {
            List<NodeRef> elementTypes = RepoUtils.getChildrenByAssoc(caseNodeRef, ICaseTemplateModel.ASSOC_ELEMENT_TYPES, nodeService);
            for(NodeRef elementType : elementTypes) {
                List<NodeRef> elementConfig = RepoUtils.getTargetAssoc(elementType, ASSOC_ELEMENT_CONFIG, nodeService);
                if (elementConfig != null && !elementConfig.isEmpty()) {
                    QName configQName = (QName) nodeService.getProperty(elementConfig.get(0), ICaseModel.PROP_ELEMENT_TYPE);
                    if (configQName.equals(ICaseRoleModel.TYPE_ROLE)) {
                        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(elementType, ICaseTemplateModel.ASSOC_INTERNAL_ELEMENTS, RegexQNamePattern.MATCH_ALL);
                        for (ChildAssociationRef assoc : assocs) {
                            caseRoles.getRole().add(getRole(assoc.getChildRef()));
                        }
                    }
                }
            }

        }
        return caseRoles;
    }

    private Role getRole(NodeRef roleRef) {
        Role role = new Role();
        role.setId(utils.convertNodeRefToId(roleRef));
        role.setName((String) nodeService.getProperty(roleRef, ContentModel.PROP_TITLE));
        role.getOtherAttributes().put(CMMNUtils.QNAME_NODE_TYPE, nodeService.getType(roleRef).toString());

        for (Map.Entry<javax.xml.namespace.QName, QName> entry : CMMNUtils.ROLES_ATTRIBUTES_MAPPING.entrySet()) {
            Serializable value = nodeService.getProperty(roleRef, entry.getValue());
            if (value != null) {
                role.getOtherAttributes().put(entry.getKey(), utils.convertValueForCmmn(entry.getValue(), value));
            }
        }

        List<AssociationRef> assigneesRefs = nodeService.getTargetAssocs(roleRef, ICaseRoleModel.ASSOC_ASSIGNEES);
        String[] assignees = new String[assigneesRefs.size()];
        for (int i = 0; i < assignees.length; i++) {
            NodeRef assigneeRef = assigneesRefs.get(i).getTargetRef();
            assignees[i] = RepoUtils.getAuthorityName(assigneeRef, nodeService, dictionaryService);
        }
        role.getOtherAttributes().put(CMMNUtils.QNAME_ROLE_ASSIGNEES, StringUtils.join(assignees, ','));

        List<AssociationRef> referenseRoles = nodeService.getTargetAssocs(roleRef, ICaseRoleModel.ASSOC_REFERENCE_ROLE);
        if (referenseRoles != null && !referenseRoles.isEmpty()) {
            NodeRef referenceRole = referenseRoles.get(0).getTargetRef();
            role.getOtherAttributes().put(CMMNUtils.QNAME_REFERENSE_ROLE, (String) nodeService.getProperty(referenceRole, ICaseRoleModel.PROP_VARNAME));
        }

        return role;
    }
}
