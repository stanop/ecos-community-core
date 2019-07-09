package ru.citeck.ecos.utils;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import ru.citeck.ecos.model.EcosModel;

import java.util.LinkedList;
import java.util.List;

public class PersonUtils {

    public static void excludeDisabledUsers(List<NodeRef> users, NodeService nodeService) {
        if (CollectionUtils.isEmpty(users)) {
            return;
        }

        List<NodeRef> excludedUsers = new LinkedList<>();
        for (NodeRef user : users) {
            boolean isPersonDisabled = (boolean) nodeService.getProperty(user, EcosModel.PROP_IS_PERSON_DISABLED);
            if (BooleanUtils.isTrue(isPersonDisabled)) {
                excludedUsers.add(user);
            }
        }

        users.removeAll(excludedUsers);
    }
}
