package ru.citeck.ecos.action.group.impl;

import org.alfresco.service.transaction.TransactionService;
import ru.citeck.ecos.action.group.ActionStatus;
import ru.citeck.ecos.action.group.GroupActionConfig;

import java.util.function.Consumer;
import java.util.function.Function;

public class CustomTxnGroupAction<T> extends TxnGroupAction<T> {

    private Function<T, ActionStatus> action;

    public CustomTxnGroupAction(TransactionService transactionService,
                                Consumer<T> action,
                                GroupActionConfig config) {
        super(transactionService, config);

        this.action = (ref) -> {
            action.accept(ref);
            return new ActionStatus(ActionStatus.STATUS_OK);
        };
    }

    public CustomTxnGroupAction(TransactionService transactionService,
                                Function<T, ActionStatus> action,
                                GroupActionConfig config) {
        super(transactionService, config);

        this.action = action;
    }

    @Override
    protected ActionStatus processImpl(T nodeRef) {
        return action.apply(nodeRef);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        CustomTxnGroupAction that = (CustomTxnGroupAction) o;

        return action.equals(that.action);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + action.hashCode();
        return result;
    }
}
