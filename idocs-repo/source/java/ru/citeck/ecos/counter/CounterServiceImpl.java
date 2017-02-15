/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.counter;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.VmShutdownListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionException;
import ru.citeck.ecos.model.CounterModel;

public class CounterServiceImpl implements CounterService {

    private static final Log logger = LogFactory.getLog(CounterServiceImpl.class);

    private NodeService nodeService;
    private NodeRef counterRoot;
    private JobLockService jobLockService;
    private TransactionService transactionService;

    @Override
    public void setCounterLast(final String counterName, final int value) {
        LockExecutor executor = new LockExecutor<Void>(counterName, new LockExecutorCode<Void>() {
            @Override
            public Void execute() {
                NodeRef counter = getCounter(counterName, true);
                setCounter(counter, value);
                return null;
            }
        });
        try {
            executor.execute();
        } catch (JobExecutionException | EnumerationException e) {
            logger.error("setCounterLast something goes wrong", e);
        }
    }

    @Override
    public Integer getCounterLast(final String counterName) {
        NodeRef counter = getCounter(counterName, false);
        if (counter == null) {
            return null;
        }
        return getCounter(counter);
    }

    @Override
    public Integer getCounterNext(final String counterName, final boolean increment) {
        LockExecutor executor = new LockExecutor<Integer>(counterName, new LockExecutorCode<Integer>() {
            @Override
            public Integer execute() {
                return transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Integer>() {
                    @Override
                    public Integer execute() throws Throwable {
                        NodeRef counter = getCounter(counterName, increment);
                        if (counter == null) {
                            return null;
                        }
                        int value = getCounter(counter) + 1;
                        if (increment) {
                            setCounter(counter, value);
                        }
                        return value;
                    }
                }, false, true);
            }
        });
        Integer result = null;
        try {
            result = (Integer) executor.execute();
        } catch (JobExecutionException | EnumerationException e) {
            logger.error("getCounterNext goes wrong ", e);
        }
        return result;
    }

    private NodeRef getCounter(String counterName, boolean createIfAbsent) {
        NodeRef counter = nodeService.getChildByName(counterRoot, ContentModel.ASSOC_CONTAINS, counterName);
        if (counter == null && createIfAbsent) {
            ChildAssociationRef counterRef = nodeService.createNode(counterRoot, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, counterName), CounterModel.TYPE_COUNTER);
            counter = counterRef.getChildRef();
            nodeService.setProperty(counter, ContentModel.PROP_NAME, counterName);
        }
        return counter;
    }

    private int getCounter(NodeRef counter) {
        return (Integer) nodeService.getProperty(counter, CounterModel.PROP_VALUE);
    }

    private void setCounter(NodeRef counter, int value) {
        nodeService.setProperty(counter, CounterModel.PROP_VALUE, value);
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setCounterRoot(String counterRoot) {
        this.counterRoot = new NodeRef(counterRoot);
    }

    public void setJobLockService(JobLockService jobLockService) {
        this.jobLockService = jobLockService;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    private class LockExecutor<T> {
        private static final long LOCK_TTL = 30000L;

        private final QName lockQName;
        private final LockExecutorCode code;

        public LockExecutor(String lockString, LockExecutorCode<T> code) {
            this.lockQName = QName.createQName(null, lockString);
            this.code = code;
        }

        public T execute() throws JobExecutionException, EnumerationException {
            String lockToken = null;
            try {
                lockToken = jobLockService.getLock(lockQName, LOCK_TTL, 500, 60);
                jobLockService.refreshLock(lockToken, lockQName, LOCK_TTL);
                return doWork(lockToken);
            } catch (VmShutdownListener.VmShutdownException e) {
                // Aborted
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("   Job %s aborted.", lockQName.getLocalName()));
                }
            } finally {
                releaseLock(lockToken);
            }
            return null;
        }

        private synchronized T doWork(String lockToken) throws EnumerationException {
            try {
                return (T) code.execute();
            } finally {
                releaseLock(lockToken);
            }
        }


        /**
         * Release the lock after the job completes
         */
        private void releaseLock(String lockToken) {
            if (lockToken != null) {
                jobLockService.releaseLock(lockToken, lockQName);
            }
        }
        // else: We can't release without a token
    }
}
