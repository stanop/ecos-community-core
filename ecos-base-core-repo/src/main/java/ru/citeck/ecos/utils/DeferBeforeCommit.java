package ru.citeck.ecos.utils;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;

public class DeferBeforeCommit implements Runnable {

    private Runnable work;
    
    public DeferBeforeCommit(Runnable work) {
        this.work = work;
    }
    
    @Override
    public void run() {
        AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
            @Override
            public void beforeCommit(boolean readOnly) {
                work.run();
            }
        });
    }

}
