package ru.citeck.ecos.action.group;

import org.alfresco.service.transaction.TransactionService;
import ru.citeck.ecos.repo.RemoteNodeRef;

import java.util.function.Consumer;
import java.util.function.Function;

public class CustomTxnGroupAction extends TxnGroupAction {

    private Function<RemoteNodeRef, GroupActionResult> action;

    CustomTxnGroupAction(TransactionService transactionService,
                         Consumer<RemoteNodeRef> action,
                         GroupActionConfig config) {
        super(transactionService, config);

        this.action = (ref) -> {
            action.accept(ref);
            return new GroupActionResult(GroupActionResult.STATUS_OK);
        };
    }

    CustomTxnGroupAction(TransactionService transactionService,
                         Function<RemoteNodeRef, GroupActionResult> action,
                         GroupActionConfig config) {
        super(transactionService, config);

        this.action = action;
    }

    @Override
    protected GroupActionResult processImpl(RemoteNodeRef nodeRef) {
        return action.apply(nodeRef);
    }
}
