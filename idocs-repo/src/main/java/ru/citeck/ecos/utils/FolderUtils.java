package ru.citeck.ecos.utils;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.Pair;

import java.util.concurrent.ConcurrentHashMap;

public class FolderUtils {

    private static final ConcurrentHashMap<Object,Boolean> nameCache = new ConcurrentHashMap<>(100);


    public static String makeUniqueName(NodeRef parent, NodeRef childRef, NodeService nodeService) {

        String baseName = (String) nodeService.getProperty(childRef, ContentModel.PROP_NAME);
        String name = makeUniqueName(parent, baseName, nodeService);

        if (!baseName.equals(name)) {
            nodeService.setProperty(childRef, ContentModel.PROP_NAME, name);
        }

        return name;
    }

    public static String makeUniqueName(NodeRef parent, String baseName, NodeService nodeService) {

        String name = baseName;

        int counter = 1;
        NodeRef child = nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, name);

        while (child != null) {

            Object key;
            do {
                name = String.format("%s (%d)", baseName, counter++);
                key = new Pair<>(parent, name);
            } while (nameCache.putIfAbsent(key, true) != null);

            final Object finalKey = key;
            AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
                @Override
                public void afterCommit() {
                    nameCache.remove(finalKey);
                }
                @Override
                public void afterRollback() {
                    nameCache.remove(finalKey);
                }
            });

            child = nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, name);
        }

        return name;
    }
}
