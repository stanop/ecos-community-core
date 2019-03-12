package ru.citeck.ecos.user;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.AlfrescoMissingQNamesModel;
import ru.citeck.ecos.model.DeputyModel;
import ru.citeck.ecos.model.EcosModel;
import ru.citeck.ecos.model.OrgStructModel;
import ru.citeck.ecos.node.NodeInfo;
import ru.citeck.ecos.node.NodeInfoFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class UserAutoCreationServiceImpl implements UserAutoCreationService {

    private static final String DEFAULT_EMAIL = "";
    private static final String DEFAULT_PASSWORD = "12345";
    private static final String DEFAULT_PRESET = "user-dashboard";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm'Z'";
    private final List<QName> MANDATORY_PROPS = fillCheckedMandatoryProps();

    private CyrillicUserNameGenerator cyrillicUserNameGenerator;
    private NodeInfoFactory nodeInfoFactory;

    @Override
    public NodeRef createPerson(Map<QName, Object> properties) {
        return createPerson(properties, null);
    }

    @Override
    public NodeRef createPerson(Map<QName, Object> properties, NodeRef group) {
        for (QName mandatoryProp : MANDATORY_PROPS) {
            if (properties.get(mandatoryProp) == null) {
                throw new IllegalArgumentException("Error on user creation, mandatory property is missing: " + mandatoryProp.getLocalName());
            }
        }
        if (properties.get(ContentModel.PROP_USERNAME) == null) {
            String firstName = properties.get(ContentModel.PROP_FIRSTNAME).toString();
            String lastName = properties.get(ContentModel.PROP_LASTNAME).toString();
            String middleName = properties.get(AlfrescoMissingQNamesModel.PROP_MIDDLE_NAME).toString();
            properties.put(ContentModel.PROP_USERNAME, cyrillicUserNameGenerator.getUserName(lastName,firstName,middleName));
        }
        Date birthDate = (Date) properties.get(EcosModel.PROP_BIRTH_DATE);
        if (birthDate != null) {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            properties.put(EcosModel.PROP_BIRTH_DATE, df.format(birthDate));
        }
        properties.putIfAbsent(EcosModel.PROP_PASS, DEFAULT_PASSWORD);
        properties.putIfAbsent(ContentModel.PROP_EMAIL, DEFAULT_EMAIL);
        properties.putIfAbsent(EcosModel.PROP_IS_PERSON_DISABLED, false);
        properties.putIfAbsent(ContentModel.PROP_SIZE_CURRENT, 0);
        properties.putIfAbsent(OrgStructModel.PROP_SHOW_HINTS, true);
        properties.putIfAbsent(OrgStructModel.PROP_PRESET, DEFAULT_PRESET);
        properties.putIfAbsent(DeputyModel.PROP_AVAILABLE, true);
        NodeInfo personInfo = nodeInfoFactory.createNodeInfo(properties);
        personInfo.setType(ContentModel.TYPE_PERSON);
        if (group != null) {
            personInfo.setParent(group);
        }
        return nodeInfoFactory.persist(personInfo, false);
    }

    private List<QName> fillCheckedMandatoryProps () {
        List<QName> mandatoryProps = new ArrayList<>();
        mandatoryProps.add(ContentModel.PROP_FIRSTNAME);
        mandatoryProps.add(ContentModel.PROP_LASTNAME);
        mandatoryProps.add(AlfrescoMissingQNamesModel.PROP_MIDDLE_NAME);
        return mandatoryProps;
    }

    public void setCyrillicUserNameGenerator(CyrillicUserNameGenerator cyrillicUserNameGenerator) {
        this.cyrillicUserNameGenerator = cyrillicUserNameGenerator;
    }

    public void setNodeInfoFactory(NodeInfoFactory nodeInfoFactory) {
        this.nodeInfoFactory = nodeInfoFactory;
    }
}
