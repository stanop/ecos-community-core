package ru.citeck.ecos.behavior.authority;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.behavior.base.AbstractBehaviour;
import ru.citeck.ecos.behavior.base.PolicyMethod;
import ru.citeck.ecos.personal.PersonalDocumentsService;
import ru.citeck.ecos.utils.RepoUtils;

import java.util.ArrayList;
import java.util.List;

public class CreatePersonalDocumentsForCreatedPersonBehaviour extends AbstractBehaviour
                                                              implements NodeServicePolicies.OnCreateNodePolicy {

    private final static Logger logger = Logger.getLogger(CreatePersonalDocumentsForCreatedPersonBehaviour.class);

    private final static String PERSONAL_DOCUMENTS_DIR_CREATION = "Personal documents directory creation for user: ";

    @Autowired
    private PersonalDocumentsService personalDocumentsService;

    private List<String> skipPersons = new ArrayList<>();

    @Override
    protected void beforeInit() {
        setClassName(ContentModel.TYPE_PERSON);
    }

    @Override
    @PolicyMethod(policy = NodeServicePolicies.OnCreateNodePolicy.class,
            frequency = Behaviour.NotificationFrequency.EVERY_EVENT, runAsSystem = true)
    public void onCreateNode(ChildAssociationRef childAssocRef) {
        String personName = RepoUtils.getProperty(childAssocRef.getChildRef(), ContentModel.PROP_USERNAME, String.class,
                nodeService);

        if (StringUtils.isNoneBlank(personName) && !skipPersons.contains(personName)) {
            logger.info(PERSONAL_DOCUMENTS_DIR_CREATION + personName);
            personalDocumentsService.ensureDirectory(personName);
        }
    }

    public void setSkipPersons(List<String> skipPersons) {
        this.skipPersons = skipPersons;
    }

}