package ru.citeck.ecos.ssg.records;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.action.group.*;
import ru.citeck.ecos.action.group.impl.TxnGroupAction;
import ru.citeck.ecos.records2.RecordMeta;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.RecordsService;
import ru.citeck.ecos.records2.request.mutation.RecordsMutation;

import java.util.*;

@Component
public class MutateRecordsGroupAction implements GroupActionFactory<RecordRef> {

    public static final String ID = "records-mutation";

    private static final String PARAM_ATTRIBUTES = "attributes";
    private static final String[] MANDATORY_PARAMS = {PARAM_ATTRIBUTES};

    private TransactionService transactionService;
    private RecordsService recordsService;

    @Autowired
    public MutateRecordsGroupAction(TransactionService transactionService,
                                    GroupActionService groupActionService,
                                    RecordsService recordsService) {
        this.transactionService = transactionService;
        this.recordsService = recordsService;
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

        private RecordMeta meta = new RecordMeta();

        Action(GroupActionConfig config) {
            super(transactionService, config);
            meta.setAttributes((ObjectNode) config.getParams().get(PARAM_ATTRIBUTES));
        }

        @Override
        protected ActionStatus processImpl(RecordRef node) {

            RecordMeta toMutate = new RecordMeta(meta, node);
            RecordsMutation mutation = new RecordsMutation();
            mutation.setRecords(Collections.singletonList(toMutate));

            recordsService.mutate(mutation);

            return ActionStatus.ok();
        }
    }
}
