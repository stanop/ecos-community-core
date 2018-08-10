package ru.citeck.ecos.cardlet.config;

import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import ru.citeck.ecos.cardlet.xml.Cardlet;
import ru.citeck.ecos.content.metadata.MetadataExtractor;
import ru.citeck.ecos.model.CardletModel;

import java.io.Serializable;
import java.util.*;

public class CardletMetadataExtractor implements MetadataExtractor<Cardlet> {

    @Override
    public Map<QName, Serializable> getMetadata(Cardlet cardlet) {

        Map<QName, Serializable> metadata = new HashMap<>();

        metadata.put(CardletModel.PROP_ID, cardlet.getId());
        metadata.put(CardletModel.PROP_ALLOWED_TYPE, cardlet.getAllowedType());
        metadata.put(CardletModel.PROP_CONDITION, cardlet.getCondition());
        metadata.put(CardletModel.PROP_POSITION_INDEX_IN_MOBILE, cardlet.getPosition().getMobileOrder());
        metadata.put(CardletModel.PROP_REGION_POSITION, cardlet.getPosition().getOrder());
        metadata.put(CardletModel.PROP_REGION_COLUMN, cardlet.getPosition().getColumn().value());
        metadata.put(CardletModel.PROP_CARD_MODE, cardlet.getPosition().getCardMode());
        metadata.put(CardletModel.PROP_REGION_ID, cardlet.getId());

        String authorities = cardlet.getAuthorities();
        if (StringUtils.isNotBlank(authorities)) {
            List<String> authoritiesList = Arrays.asList(authorities.split(","));
            metadata.put(CardletModel.PROP_ALLOWED_AUTHORITIES, new ArrayList<>(authoritiesList));
        } else {
            metadata.put(CardletModel.PROP_ALLOWED_AUTHORITIES, new ArrayList<>(0));
        }

        return metadata;
    }
}
