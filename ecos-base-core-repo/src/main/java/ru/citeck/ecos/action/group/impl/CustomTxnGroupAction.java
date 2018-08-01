package ru.citeck.ecos.action.group.impl;

import org.alfresco.service.transaction.TransactionService;
import ru.citeck.ecos.action.group.ActionStatus;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.impl.TxnGroupAction;
import ru.citeck.ecos.repo.RemoteRef;

import java.util.function.Consumer;
import java.util.function.Function;

public class CustomTxnGroupAction extends TxnGroupAction {

    private Function<RemoteRef, ActionStatus> action;

    public CustomTxnGroupAction(TransactionService transactionService,
                                Consumer<RemoteRef> action,
                                GroupActionConfig config) {
        super(transactionService, config);

        this.action = (ref) -> {
            action.accept(ref);
            return new ActionStatus(ActionStatus.STATUS_OK);
        };
    }

    public CustomTxnGroupAction(TransactionService transactionService,
                                Function<RemoteRef, ActionStatus> action,
                                GroupActionConfig config) {
        super(transactionService, config);

        this.action = action;
    }

    @Override
    protected ActionStatus processImpl(RemoteRef nodeRef) {
        return action.apply(nodeRef);
    }
}
