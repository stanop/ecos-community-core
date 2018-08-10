package ru.citeck.ecos.cardlet.config;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.cardlet.xml.*;
import ru.citeck.ecos.content.dao.NodeDataReader;
import ru.citeck.ecos.model.CardletModel;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Map;

public class CardletNodeDataReader implements NodeDataReader<Cardlet> {

    private static final String REMOTE_CONTROL_URL = "js/citeck/modules/cardlets/remote/remote";
    private static final String SURF_REGION_URL = "/share/service/citeck/surf/region";

    private NodeService nodeService;
    private ObjectFactory factory = new ObjectFactory();

    @Override
    public Cardlet getData(NodeRef nodeRef) {

        Map<QName, Serializable> props = nodeService.getProperties(nodeRef);

        Cardlet cardlet = factory.createCardlet();
        cardlet.setPosition(createPosition(props));
        QName allowedType = (QName) props.get(CardletModel.PROP_ALLOWED_TYPE);
        cardlet.setAllowedType(allowedType != null ? allowedType.toString() : ContentModel.TYPE_CMOBJECT.toString());

        cardlet.setCondition((String) props.get(CardletModel.PROP_CONDITION));
        cardlet.setControl(createControl(props));
        cardlet.setId((String) props.get(CardletModel.PROP_REGION_ID));

        return cardlet;
    }

    private Control createControl(Map<QName, Serializable> props) {

        String regionId = (String) props.get(CardletModel.PROP_REGION_ID);

        Control control = factory.createControl();
        control.setUrl(REMOTE_CONTROL_URL);

        control.getProp().add(createProp("remoteUrl", SURF_REGION_URL));
        control.getProp().add(createProp("remoteId", String.valueOf(props.get(ContentModel.PROP_NODE_UUID))));

        control.getProp().add(createProp("regionId", regionId));
        control.getProp().add(createProp("htmlid", String.valueOf(props.get(ContentModel.PROP_NODE_DBID))));
        control.getProp().add(createProp("pageid", "card-details"));
        control.getProp().add(createProp("theme", "${state.pageArgs.theme}"));
        control.getProp().add(createProp("nodeRef", "${state.pageArgs.nodeRef}"));
        control.getProp().add(createProp("scope", "page"));

        return control;
    }

    private Property createProp(String name, String value) {
        Property prop = factory.createProperty();
        prop.setName(name);
        prop.setValue(value);
        return prop;
    }

    private Position createPosition(Map<QName, Serializable> props) {

        Position position = factory.createPosition();

        position.setCardMode((String) props.get(CardletModel.PROP_CARD_MODE));
        String column = (String) props.get(CardletModel.PROP_REGION_COLUMN);
        position.setColumn(StringUtils.isNotBlank(column) ? ColumnType.fromValue(column) : ColumnType.LEFT);
        position.setOrder((String) props.get(CardletModel.PROP_REGION_POSITION));

        Integer mobileIndex = (Integer) props.get(CardletModel.PROP_POSITION_INDEX_IN_MOBILE);
        if (mobileIndex == null) {
            mobileIndex = -1;
        }
        position.setMobileOrder(BigInteger.valueOf(mobileIndex));

        return position;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        nodeService = serviceRegistry.getNodeService();
    }
}
