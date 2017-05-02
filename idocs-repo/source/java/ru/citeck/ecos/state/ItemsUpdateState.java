package ru.citeck.ecos.state;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * @author Pavel Simonov
 */
public final class ItemsUpdateState {

    private static final Log LOG = LogFactory.getLog(ItemsUpdateState.class);

    private static final long DEFAULT_TIMEOUT = 100000;

    private final Map<Object, ItemData> items = new HashMap<>();

    public void startUpdate(Object key, Object item) {
        startUpdate(key, item, DEFAULT_TIMEOUT);
    }

    public void startUpdate(Object key, Object item, long timeout) {
        synchronized (items) {
            if (key == null) {
                LOG.warn("'key' is a mandatory parameter");
                return;
            }
            if (item == null) {
                LOG.warn("'item' is a mandatory parameter");
                return;
            }
            if (timeout < 0) {
                throw new IllegalArgumentException("Timeout can't be less than zero. Value = " + timeout);
            }
            ItemData data = items.get(item);
            if (data == null) {
                data = new ItemData();
                items.put(item, data);
            }
            data.keys.add(key);
            data.timeout = timeout;
            data.lastUpdate = System.currentTimeMillis();
        }
    }

    public void endUpdate(Object key, Object item) {
        endUpdate(key, item, false);
    }

    public void endUpdate(Object key, Object item, boolean afterTransaction) {
        endUpdate(key, item, afterTransaction, true);
    }

    public void endUpdate(final Object key, final Object item, boolean afterTransaction, final boolean afterRollback) {
        if (!afterTransaction) {
            synchronized (items) {
                ItemData data = items.get(item);
                if (data != null) {
                    data.keys.remove(key);
                    if (data.keys.size() == 0) {
                        items.remove(item);
                    }
                }
            }
        } else {
            AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
                @Override
                public void afterCommit() {
                    endUpdate(key, item, false);
                }
                @Override
                public void afterRollback() {
                    if (afterRollback) {
                        endUpdate(key, item, false);
                    }
                }
            });
        }
    }

    public boolean isPendingUpdate(Object item) {
        synchronized (items) {
            ItemData data = items.get(item);
            if (data == null) {
                return false;
            }
            long timeDelta = System.currentTimeMillis() - data.lastUpdate;
            if (Math.abs(timeDelta) > data.timeout) {
                items.remove(item);
                data = null;
            }
            return data != null;
        }
    }

    public void reset() {
        synchronized (items) {
            items.clear();
        }
    }

    private static class ItemData {
        long lastUpdate = 0L;
        long timeout;
        Set<Object> keys = new HashSet<>();
    }
}
