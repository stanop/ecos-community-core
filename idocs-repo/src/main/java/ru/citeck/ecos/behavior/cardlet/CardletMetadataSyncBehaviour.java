package ru.citeck.ecos.behavior.cardlet;

import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.behavior.base.AbstractBehaviour;
import ru.citeck.ecos.behavior.base.PolicyMethod;
import ru.citeck.ecos.cardlet.config.CardletMetadataExtractor;
import ru.citeck.ecos.cardlet.config.CardletsRegistry;
import ru.citeck.ecos.cardlet.xml.Cardlet;
import ru.citeck.ecos.cardlet.xml.ColumnType;
import ru.citeck.ecos.content.ContentData;
import ru.citeck.ecos.model.CardletModel;
import ru.citeck.ecos.model.ICaseModel;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

public class CardletMetadataSyncBehaviour extends AbstractBehaviour {

    private static final String TXN_DATA_KEY = CardletMetadataSyncBehaviour.class.toString();
    private static final List<QName> FIELDS_TO_SYNC = Arrays.asList(
            CardletModel.PROP_ALLOWED_AUTHORITIES,
            CardletModel.PROP_ALLOWED_TYPE,
            CardletModel.PROP_CARD_MODE,
            CardletModel.PROP_CONDITION,
            CardletModel.PROP_ID,
            CardletModel.PROP_POSITION_INDEX_IN_MOBILE,
            CardletModel.PROP_REGION_COLUMN,
            CardletModel.PROP_REGION_ID,
            CardletModel.PROP_REGION_POSITION
    );

    private NodeService nodeService;
    @Autowired
    private CardletsRegistry registry;
    @Autowired
    private CardletMetadataExtractor metadataExtractor;

    private List<QName> contentToPropsFields = Collections.emptyList();

    @Override
    protected void beforeInit() {
        nodeService = serviceRegistry.getNodeService();
        setClassName(CardletModel.TYPE_CARDLET);
    }

    @PolicyMethod(policy = NodeServicePolicies.OnUpdatePropertiesPolicy.class,
                  frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT,
                  runAsSystem = true)
    public void onUpdateProperties(NodeRef nodeRef,
                                   Map<QName, Serializable> before,
                                   Map<QName, Serializable> after) {

        if (!TransactionalResourceHelper.getSet(TXN_DATA_KEY).add(nodeRef)) {
            return;
        }

        Map<QName, Serializable> changedProps = getChangedProps(before, after);

        if (changedProps.size() > 0) {

            Optional<ContentData<Cardlet>> contentData = registry.getContentData(nodeRef);

            contentData.ifPresent(d -> d.changeData(data -> {
                
                changedProps.forEach((field, value) -> {
                    if (CardletModel.PROP_ALLOWED_AUTHORITIES.equals(field)) {
                        if (value instanceof String) {
                            data.setAuthorities((String) value);
                        } else if (value instanceof Iterable) {
                            @SuppressWarnings("unchecked")
                            Iterable<String> authorities = (Iterable<String>) value;
                            data.setAuthorities(String.join(",", authorities));
                        }
                    } else if (CardletModel.PROP_ALLOWED_TYPE.equals(field)) {
                        data.setAllowedType(stringOrNull(value));
                    } else if (CardletModel.PROP_CARD_MODE.equals(field)) {
                        data.getPosition().setCardMode(stringOrNull(value));
                    } else if (CardletModel.PROP_CONDITION.equals(field)) {
                        data.setCondition(stringOrNull(value));
                    } else if (CardletModel.PROP_ID.equals(field)) {
                        data.setId(stringOrNull(value));
                    } else if (CardletModel.PROP_POSITION_INDEX_IN_MOBILE.equals(field)) {
                        data.getPosition().setMobileOrder(integerOr(value, -1));
                    } else if (CardletModel.PROP_REGION_COLUMN.equals(field)) {
                        data.getPosition().setColumn(ColumnType.fromValue(stringOrNull(value)));
                    } else if (CardletModel.PROP_REGION_ID.equals(field)) {
                        data.setRegionId(stringOrNull(value));
                    } else if (CardletModel.PROP_REGION_POSITION.equals(field)) {
                        data.getPosition().setOrder(stringOrNull(value));
                    }
                });
            }));
        }
    }

    private BigInteger integerOr(Object value, long def) {
        if (value == null) {
            return BigInteger.valueOf(def);
        }
        if (value instanceof String) {
            return BigInteger.valueOf(Long.valueOf((String) value));
        }
        if (value instanceof Integer) {
            int intValue = ((Integer) value);
            return BigInteger.valueOf(intValue);
        }
        if (value instanceof Long) {
            long longValue = ((Long) value);
            return BigInteger.valueOf(longValue);
        }
        return BigInteger.valueOf(def);
    }

    private String stringOrNull(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private Map<QName, Serializable> getChangedProps(Map<QName, Serializable> before,
                                                     Map<QName, Serializable> after) {

        Map<QName, Serializable> result = new HashMap<>();
        for (QName field : FIELDS_TO_SYNC) {
            if (!Objects.equals(before.get(field), after.get(field))) {
                result.put(field, after.get(field));
            }
        }
        return result;
    }

    @PolicyMethod(policy = ContentServicePolicies.OnContentUpdatePolicy.class,
                  frequency = Behaviour.NotificationFrequency.TRANSACTION_COMMIT,
                  runAsSystem = true)
    public void onContentUpdate(NodeRef nodeRef, boolean newContent) {

        TransactionalResourceHelper.getSet(TXN_DATA_KEY).add(nodeRef);

        Optional<ContentData<Cardlet>> contentData = registry.getContentData(nodeRef);
        contentData.flatMap(ContentData::getData).ifPresent(d -> {
            Map<QName, Serializable> metadata = metadataExtractor.getMetadata(d);
            for (QName qname : contentToPropsFields) {
                nodeService.setProperty(nodeRef, qname, metadata.get(qname));
            }
            ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(nodeRef);
            nodeService.setProperty(parentAssoc.getParentRef(), ICaseModel.PROP_LAST_CHANGED_DATE, new Date());
        });
    }

    public void setContentToPropsFields(List<QName> contentToPropsFields) {
        this.contentToPropsFields = contentToPropsFields;
    }
}
