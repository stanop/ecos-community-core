/*
 * Copyright (C) 2008-2018 Citeck LLC.
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
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.locks.LockUtils;
import ru.citeck.ecos.model.CounterModel;

public class CounterServiceImpl implements CounterService {

    private NodeService nodeService;
    private NodeRef counterRoot;
    private TransactionService transactionService;
    private LockUtils lockUtils;

    @Override
    public void setCounterLast(final String counterName, final long value) {
        lockUtils.doWithLock(
            counterName,
            () -> transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
                NodeRef counter = getCounter(counterName, true);
                setCounter(counter, value);
                return null;
            }, false, true));
    }

    @Override
    public Long getCounterLast(final String counterName) {
        NodeRef counter = getCounter(counterName, false);
        if (counter == null) {
            return null;
        }
        return getCounter(counter);
    }

    @Override
    public Long getCounterNext(final String counterName, final boolean increment) {
        return lockUtils.doWithLock(
            counterName,
            () -> transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
                NodeRef counter = getCounter(counterName, increment);
                if (counter == null) {
                    return null;
                }
                long value = getCounter(counter) + 1;
                if (increment) {
                    setCounter(counter, value);
                }
                return value;
            }, false, true));
    }

    private NodeRef getCounter(String counterName, boolean createIfAbsent) {
        NodeRef counter = nodeService.getChildByName(counterRoot, ContentModel.ASSOC_CONTAINS, counterName);
        if (counter == null && createIfAbsent) {
            ChildAssociationRef counterRef = nodeService.createNode(counterRoot, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, counterName), CounterModel.TYPE_COUNTER);
            counter = counterRef.getChildRef();
            nodeService.setProperty(counter, ContentModel.PROP_NAME, counterName);
        }
        return counter;
    }

    private long getCounter(NodeRef counter) {
        return (Long) nodeService.getProperty(counter, CounterModel.PROP_VALUE);
    }

    private void setCounter(NodeRef counter, long value) {
        nodeService.setProperty(counter, CounterModel.PROP_VALUE, value);
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setCounterRoot(String counterRoot) {
        this.counterRoot = new NodeRef(counterRoot);
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Autowired
    public void setLockUtils(LockUtils lockUtils) {
        this.lockUtils = lockUtils;
    }
}
