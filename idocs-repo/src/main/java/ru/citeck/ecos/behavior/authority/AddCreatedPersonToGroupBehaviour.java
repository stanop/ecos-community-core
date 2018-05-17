package ru.citeck.ecos.behavior.authority;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.behavior.base.AbstractBehaviour;
import ru.citeck.ecos.behavior.base.PolicyMethod;
import ru.citeck.ecos.utils.RepoUtils;

public class AddCreatedPersonToGroupBehaviour extends AbstractBehaviour implements NodeServicePolicies.OnCreateNodePolicy {

    private final static Logger logger = Logger.getLogger(AddCreatedPersonToGroupBehaviour.class);

    @Autowired
    private AuthorityService authorityService;

    private String groupFullName;

    @Override
    protected void beforeInit() {
        setClassName(ContentModel.TYPE_PERSON);
    }

    @Override
    @PolicyMethod(policy = NodeServicePolicies.OnCreateNodePolicy.class,
            frequency = Behaviour.NotificationFrequency.EVERY_EVENT, runAsSystem = true)
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        if (!authorityService.authorityExists(groupFullName)) {
            logger.error("Cannot add person to group <" + groupFullName
                    + ">, because group does not exists");
            return;
        }

        String personName = RepoUtils.getProperty(childAssocRef.getChildRef(), ContentModel.PROP_USERNAME, String.class,
                nodeService);
        if (StringUtils.isNoneBlank(personName)) {
            authorityService.addAuthority(groupFullName, personName);
        }
    }

    public void setGroupFullName(String groupFullName) {
        this.groupFullName = groupFullName;
    }
}