package ru.citeck.ecos.cardlet.config;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import ru.citeck.ecos.cardlet.xml.Cardlet;
import ru.citeck.ecos.content.ContentData;
import ru.citeck.ecos.content.RepoContentDAO;
import ru.citeck.ecos.content.RepoContentDAOImpl;
import ru.citeck.ecos.content.dao.ContentDAO;
import ru.citeck.ecos.model.CardletModel;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class CardletsRegistry implements RepoContentDAO<Cardlet> {

    private RepoContentDAOImpl<Cardlet> repoContentDAO;

    public List<Cardlet> getCardlets(List<QName> types, Collection<String> authorities, String mode) {

        Map<String, Cardlet> cardlets = new HashMap<>();

        Map<QName, Serializable> keys = new HashMap<>(2);
        if (mode != null) {
            keys.put(CardletModel.PROP_CARD_MODE, mode);
        }
        for (QName type : types) {
            keys.put(CardletModel.PROP_ALLOWED_TYPE, type);
            List<ContentData<Cardlet>> typeCardlets = repoContentDAO.getContentData(keys);
            for (ContentData<Cardlet> cardletData : typeCardlets) {
                if (cardletData.getData().isPresent()) {
                    Cardlet cardlet = cardletData.getData().get();
                    String id = cardlet.getId();
                    cardlets.putIfAbsent(id, cardlet);
                }
            }
        }

        return cardlets.values().stream()
                .filter(cardlet -> {
                    String cardletAuth = cardlet.getAuthorities();
                    return checkAuthority(authorities, cardletAuth);
                })
                .collect(Collectors.toList());
    }

    private boolean checkAuthority(Collection<String> current, String allowed) {
        if (StringUtils.isBlank(allowed)) {
            return true;
        }
        String[] allowedArr = allowed.split(",");
        for (String authority : allowedArr) {
            if (current.contains(authority)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ContentData<Cardlet>> getContentData(Map<QName, Serializable> keys, boolean ignoreWithoutData) {

        List<ContentData<Cardlet>> result;

        String cardletId = (String) keys.get(CardletModel.PROP_ID);

        if (StringUtils.isNotBlank(cardletId)) {

            Map<QName, Serializable> idKey = new HashMap<>();
            idKey.put(CardletModel.PROP_ID, cardletId);
            result = repoContentDAO.getContentData(idKey, ignoreWithoutData);

            if (result.isEmpty()) {
                Map<QName, Serializable> keysWithoutId = new HashMap<>();
                keys.forEach((k, v) -> {
                    if (!CardletModel.PROP_ID.equals(k)) {
                        keysWithoutId.put(k, v);
                    }
                });
                result = repoContentDAO.getContentData(keysWithoutId, ignoreWithoutData);
            }
        } else {
            result = repoContentDAO.getContentData(keys, ignoreWithoutData);
        }

        return result;
    }

    @Override
    public Optional<ContentData<Cardlet>> getContentData(NodeRef nodeRef) {
        return repoContentDAO.getContentData(nodeRef);
    }

    @Override
    public NodeRef createNode(Map<QName, Serializable> properties) {
        return repoContentDAO.createNode(properties);
    }

    @Override
    public ContentDAO<Cardlet> getContentDAO() {
        return repoContentDAO.getContentDAO();
    }

    @Override
    public void clearCache() {
        repoContentDAO.clearCache();
    }

    public void setRepoContentDAO(RepoContentDAOImpl<Cardlet> repoContentDAO) {
        this.repoContentDAO = repoContentDAO;
    }
}
