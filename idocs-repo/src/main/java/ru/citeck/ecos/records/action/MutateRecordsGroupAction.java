package ru.citeck.ecos.records.action;

import lombok.Data;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.action.group.*;
import ru.citeck.ecos.action.group.impl.TxnGroupAction;
import ru.citeck.ecos.commons.data.ObjectData;
import ru.citeck.ecos.commons.json.Json;
import ru.citeck.ecos.icase.CaseStatusService;
import ru.citeck.ecos.records2.RecordMeta;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.records2.request.mutation.RecordsMutation;
import ru.citeck.ecos.security.EcosPermissionService;

import javax.xml.soap.Node;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class MutateRecordsGroupAction implements GroupActionFactory<RecordRef> {

    private static final Pattern ATT_PATTERN = Pattern.compile("^\\.atts?\\(n:\"([^\"]+)\"\\).+");

    public static final String ID = "records-mutation";

    private static final String PARAM_ATTRIBUTES = "attributes";
    private static final String[] MANDATORY_PARAMS = {PARAM_ATTRIBUTES};

    private NodeService nodeService;
    private RecordsService recordsService;
    private CaseStatusService caseStatusService;
    private TransactionService transactionService;
    private EcosPermissionService ecosPermissionService;

    @Autowired
    public MutateRecordsGroupAction(EcosPermissionService ecosPermissionService,
                                    TransactionService transactionService,
                                    GroupActionService groupActionService,
                                    CaseStatusService caseStatusService,
                                    RecordsService recordsService,
                                    NodeService nodeService) {

        this.ecosPermissionService = ecosPermissionService;
        this.transactionService = transactionService;
        this.caseStatusService = caseStatusService;
        this.recordsService = recordsService;
        this.nodeService = nodeService;
        groupActionService.register(this);
    }

    @Override
    public GroupAction<RecordRef> createAction(GroupActionConfig config) {
        return new Action(config);
    }

    @Override
    public String getActionId() {
        return ID;
    }

    @Override
    public String[] getMandatoryParams() {
        return MANDATORY_PARAMS;
    }

    class Action extends TxnGroupAction<RecordRef> {

        private final Config config;
        private final Set<String> skipInStatuses;

        Action(GroupActionConfig config) {
            super(transactionService, config);
            this.config = Json.getMapper().convert(config.getParams(), Config.class);
            if (this.config == null) {
                throw new IllegalArgumentException("Illegal config: " + config);
            }
            if (this.config.skipInStatuses != null) {
                skipInStatuses = Arrays.stream(this.config.skipInStatuses.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
            } else {
                skipInStatuses = Collections.emptySet();
            }
        }

        @Override
        protected ActionStatus processImpl(RecordRef node) {

            NodeRef nodeRef = null;
            if (NodeRef.isNodeRef(node.getId())) {
                nodeRef = new NodeRef(node.getId());
            }

            if (!checkPermissions(nodeRef, config.getAttributes())) {
                return new ActionStatus(ActionStatus.STATUS_PERMISSION_DENIED);
            }

            if (!checkStatus(nodeRef)) {
                return ActionStatus.skipped();
            }

            RecordMeta toMutate = new RecordMeta(node, config.getAttributes().deepCopy());
            RecordsMutation mutation = new RecordsMutation();
            mutation.setRecords(Collections.singletonList(toMutate));

            recordsService.mutate(mutation);

            return ActionStatus.ok();
        }

        private boolean checkStatus(NodeRef nodeRef) {

            if (nodeRef == null || skipInStatuses.isEmpty()) {
                return true;
            }

            if (Boolean.TRUE.equals(config.childMode)) {
                nodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
            }

            String status = caseStatusService.getStatus(nodeRef);
            if (status == null) {
                return true;
            }
            return !skipInStatuses.contains(status);
        }

        private boolean checkPermissions(NodeRef nodeRef, ObjectData attributes) {

            if (nodeRef == null) {
                return true;
            }

            Iterator<String> fields = attributes.fieldNames();

            while (fields.hasNext()) {

                String att = fields.next();

                String simpleAtt;
                Matcher matcher = ATT_PATTERN.matcher(att);
                if (matcher.matches()) {
                    simpleAtt = matcher.group(1);
                } else {
                    int dotIdx = att.indexOf('.');
                    int qIdx = att.indexOf('?');
                    if (qIdx == -1 && dotIdx == -1) {
                        simpleAtt = att;
                    } else if (qIdx != -1 && dotIdx != -1) {
                        simpleAtt = att.substring(0, Math.min(qIdx, dotIdx));
                    } else {
                        simpleAtt = att.substring(0, qIdx == -1 ? dotIdx : qIdx);
                    }
                }
                if (ecosPermissionService.isAttributeProtected(nodeRef, simpleAtt)) {
                    return false;
                }
            }
            return true;
        }
    }

    @Data
    public static class Config {
        private String skipInStatuses;
        private Boolean childMode;
        private ObjectData attributes;
    }
}
