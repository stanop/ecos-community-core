package ru.citeck.ecos.action.group;

import org.alfresco.service.transaction.TransactionService;
import ru.citeck.ecos.repo.RemoteRef;

import java.util.function.Consumer;
import java.util.function.Function;

public class CustomTxnGroupAction extends TxnGroupAction {

    private Function<RemoteRef, GroupActionResult> action;

    CustomTxnGroupAction(TransactionService transactionService,
                         Consumer<RemoteRef> action,
                         GroupActionConfig config) {
        super(transactionService, config);

        this.action = (ref) -> {
            action.accept(ref);
            return new GroupActionResult(GroupActionResult.STATUS_OK);
        };
    }

    CustomTxnGroupAction(TransactionService transactionService,
                         Function<RemoteRef, GroupActionResult> action,
                         GroupActionConfig config) {
        super(transactionService, config);

        this.action = action;
    }

    @Override
    protected GroupActionResult processImpl(RemoteRef nodeRef) {
        return action.apply(nodeRef);
    }
}
