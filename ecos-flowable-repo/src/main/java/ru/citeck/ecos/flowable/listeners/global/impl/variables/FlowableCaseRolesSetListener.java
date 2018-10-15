package ru.citeck.ecos.flowable.listeners.global.impl.variables;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.model.ICaseRoleModel;
import ru.citeck.ecos.role.CaseRoleService;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is flowable task/execution, which fills case roles assignees to execution variable.
 * Variables has patterns {@code VAR_KEY_ROLE_USERS_PATTERN} and {@code VAR_KEY_ROLE_GROUPS_PATTERN},
 * while {@code %s} - varName of role.
 * For example: case_role_initiator_users, if role varName = "initiator".
 * <p>
 * <b>NOTE:</b> If role varName contains '-', its replaced by '_', because flowable can't parse variable with '-'.
 *
 * @author Roman Makarskiy
 */
public class FlowableCaseRolesSetListener extends AbstractFlowableSaveToExecutionListener {

    private static final Log logger = LogFactory.getLog(FlowableCaseRolesSetListener.class);

    private static final String VAR_KEY_ROLE_USERS_PATTERN = "case_role_%s_users";
    private static final String VAR_KEY_ROLE_GROUPS_PATTERN = "case_role_%s_groups";
    private static final String HYPHEN = "-";
    private static final String UNDERSCORE = "_";

    @Autowired
    private CaseRoleService caseRoleService;
    @Autowired
    private DictionaryService dictionaryService;

    @Override
    public boolean saveIsRequired(NodeRef document) {
        if (document == null || !nodeService.exists(document)) {
            return false;
        }

        return nodeService.hasAspect(document, ICaseRoleModel.ASPECT_HAS_ROLES);
    }

    @Override
    public void saveToExecution(String executionId, NodeRef document) {
        caseRoleService.updateRoles(document);

        List<NodeRef> roles = caseRoleService.getRoles(document);
        for (NodeRef role : roles) {
            Set<String> persons = new HashSet<>();
            Set<String> groups = new HashSet<>();

            final String originalRoleName = RepoUtils.getProperty(role, ICaseRoleModel.PROP_VARNAME, nodeService);
            Set<NodeRef> assignees = caseRoleService.getAssignees(document, originalRoleName);

            String variableRoleName = originalRoleName;
            if (originalRoleName.contains(HYPHEN)) {
                variableRoleName = originalRoleName.replace(HYPHEN, UNDERSCORE);
                if (logger.isDebugEnabled()) {
                    logger.debug("Flowable can't parse variable with '-', replacing to '_' role name: " + originalRoleName);
                }
            }

            for (NodeRef assignee : assignees) {
                if (nodeService.exists(assignee)) {
                    QName type = nodeService.getType(assignee);
                    if (dictionaryService.isSubClass(type, ContentModel.TYPE_AUTHORITY_CONTAINER)) {
                        String name = (String) nodeService.getProperty(assignee, ContentModel.PROP_AUTHORITY_NAME);
                        groups.add(name);
                    } else if (dictionaryService.isSubClass(type, ContentModel.TYPE_PERSON)) {
                        String name = (String) nodeService.getProperty(assignee, ContentModel.PROP_USERNAME);
                        persons.add(name);
                    }
                }
            }

            final String roleUserKey = String.format(VAR_KEY_ROLE_USERS_PATTERN, variableRoleName);
            final String roleGroupKey = String.format(VAR_KEY_ROLE_GROUPS_PATTERN, variableRoleName);

            if (logger.isDebugEnabled()) {
                logger.debug("Set case role persons variable: <" + roleUserKey + "> value: <" + persons + ">, " +
                        "executionId: <" + executionId + ">");
                logger.debug("Set case role groups variable: <" + roleGroupKey + "> value: <" + groups + ">, " +
                        "executionId: <" + executionId + ">");
            }

            setVariable(executionId, roleUserKey, persons);
            setVariable(executionId, roleGroupKey, groups);
        }
    }
}
