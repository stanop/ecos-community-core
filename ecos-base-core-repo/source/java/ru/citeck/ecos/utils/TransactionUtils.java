package ru.citeck.ecos.utils;

public class TransactionUtils {
    
    public static void doBeforeCommit(final Runnable runnable) {
        new DeferBeforeCommit(runnable).run();
    }
    
    public static void doAfterBehaviours(final Runnable runnable) {
        doBeforeCommit(new DeferBeforeCommit(runnable));
    }
    
}
