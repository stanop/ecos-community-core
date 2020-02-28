package ru.citeck.ecos.utils;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.activity.dto.CaseActivity;
import ru.citeck.ecos.icase.activity.service.alfresco.AlfrescoCaseActivityDelegate;
import ru.citeck.ecos.model.ActivityModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component("alfActivityUtils")
public class AlfActivityUtils {

    private AlfrescoCaseActivityDelegate alfrescoCaseActivityDelegate;
    private NodeService nodeService;

    public List<CaseActivity> getActivities(NodeRef caseRef, QName assocQName, boolean recurse) {

        List<NodeRef> children = RepoUtils.getChildrenByAssoc(caseRef, assocQName, nodeService);
        if (CollectionUtils.isEmpty(children)) {
            return new ArrayList<>();
        }

        List<Pair<NodeRef, Integer>> indexedChildren = new ArrayList<>(children.size());
        for (NodeRef childRef : children) {
            Integer index = (Integer) nodeService.getProperty(childRef, ActivityModel.PROP_INDEX);
            indexedChildren.add(new Pair<>(childRef, index != null ? index : 0));
        }

        indexedChildren.sort(Comparator.comparingInt(Pair::getSecond));

        List<CaseActivity> result = new ArrayList<>(indexedChildren.size());
        for (Pair<NodeRef, Integer> child : indexedChildren) {
            String childActivityId = child.getFirst().toString();
            CaseActivity childActivity = alfrescoCaseActivityDelegate.getActivity(childActivityId);
            result.add(childActivity);
        }

        if (recurse) {
            for (NodeRef childRef : children) {
                result.addAll(getActivities(childRef, assocQName, true));
            }
        }

        return result;
    }

    @Autowired
    public void setAlfrescoCaseActivityDelegate(AlfrescoCaseActivityDelegate alfrescoCaseActivityDelegate) {
        this.alfrescoCaseActivityDelegate = alfrescoCaseActivityDelegate;
    }

    @Autowired
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
