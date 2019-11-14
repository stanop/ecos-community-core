/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.domain.solr.ibatis;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.solr.AclEntity;
import org.alfresco.repo.domain.solr.NodeParametersEntity;
import org.alfresco.repo.domain.solr.SOLRDAO;
import org.alfresco.repo.domain.solr.SOLRTrackingParameters;
import org.alfresco.repo.solr.Acl;
import org.alfresco.repo.solr.AclChangeSet;
import org.alfresco.repo.solr.NodeParameters;
import org.alfresco.repo.solr.Transaction;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;

/**
 * DAO support for SOLR web scripts.
 *
 * @since 4.0
 */
public class SOLRDAOImpl implements SOLRDAO {
    private static final String SELECT_CHANGESETS_SUMMARY = "alfresco.solr.select_ChangeSets_Summary";
    private static final String SELECT_ACLS_BY_CHANGESET_IDS = "alfresco.solr.select_AclsByChangeSetIds";
    private static final String SELECT_TRANSACTIONS = "alfresco.solr.select_Txns_custom";
    private static final String SELECT_NODES = "alfresco.solr.select_Txn_Nodes";

    private static final String SOLR_TRANSACTIONS_LIMIT_KEY = "alfresco.solr.transactions.query.limit";
    private static final int DEFAULT_SOLR_TRANSACTIONS_LIMIT = 5000;

    private SqlSessionTemplate template;
    private QNameDAO qnameDAO;

    @Autowired
    @Qualifier("global-properties")
    private Properties globalProperties;

    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.template = sqlSessionTemplate;
    }

    public void setQNameDAO(QNameDAO qnameDAO) {
        this.qnameDAO = qnameDAO;
    }

    /**
     * Initialize
     */
    public void init() {
        PropertyCheck.mandatory(this, "template", template);
        PropertyCheck.mandatory(this, "qnameDAO", qnameDAO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AclChangeSet> getAclChangeSets(Long minAclChangeSetId, Long fromCommitTime, Long maxAclChangeSetId,
                                               Long toCommitTime, int maxResults) {
        if (maxResults <= 0 || maxResults == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Maximum results must be a reasonable number.");
        }

        // We simulate an ID for the sys:deleted type
        Pair<Long, QName> deletedTypeQNamePair = qnameDAO.getQName(ContentModel.TYPE_DELETED);
        Long deletedTypeQNameId = deletedTypeQNamePair == null ? -1L : deletedTypeQNamePair.getFirst();

        SOLRTrackingParameters params = new SOLRTrackingParameters(deletedTypeQNameId);
        params.setFromIdInclusive(minAclChangeSetId);
        params.setFromCommitTimeInclusive(fromCommitTime);
        params.setToIdExclusive(maxAclChangeSetId);
        params.setToCommitTimeExclusive(toCommitTime);

        return template.selectList(SELECT_CHANGESETS_SUMMARY, params, new RowBounds(0, maxResults));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Acl> getAcls(List<Long> aclChangeSetIds, Long minAclId, int maxResults) {

        if (aclChangeSetIds == null || aclChangeSetIds.size() == 0) {
            throw new IllegalArgumentException("'aclChangeSetIds' must contain IDs.");
        }
        if (aclChangeSetIds.size() > 512) {
            throw new IllegalArgumentException("'aclChangeSetIds' cannot have more than 512 entries.");
        }

        // We simulate an ID for the sys:deleted type
        Pair<Long, QName> deletedTypeQNamePair = qnameDAO.getQName(ContentModel.TYPE_DELETED);
        Long deletedTypeQNameId = deletedTypeQNamePair == null ? -1L : deletedTypeQNamePair.getFirst();

        SOLRTrackingParameters params = new SOLRTrackingParameters(deletedTypeQNameId);
        params.setIds(aclChangeSetIds);
        params.setFromIdInclusive(minAclId);

        List<Acl> source;
        if (maxResults <= 0 || maxResults == Integer.MAX_VALUE) {
            source = template.selectList(SELECT_ACLS_BY_CHANGESET_IDS, params);
        } else {
            source = template.selectList(SELECT_ACLS_BY_CHANGESET_IDS, params, new RowBounds(0, maxResults));
        }
        // Add any unlinked shared ACLs from defining nodes to index them now
        TreeSet<Acl> sorted = new TreeSet<Acl>(source);
        HashSet<Long> found = new HashSet<Long>();
        for (Acl acl : source) {
            found.add(acl.getId());
        }

        for (Acl acl : source) {
            if (acl.getInheritedId() != null) {
                if (!found.contains(acl.getInheritedId())) {
                    AclEntity shared = new AclEntity();
                    shared.setId(acl.getInheritedId());
                    shared.setAclChangeSetId(acl.getAclChangeSetId());
                    shared.setInheritedId(acl.getInheritedId());
                    sorted.add(shared);
                }
            }
        }

        ArrayList<Acl> answer = new ArrayList<Acl>();
        answer.addAll(sorted);
        return answer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Transaction> getTransactions(Long minTxnId, Long fromCommitTime, Long maxTxnId,
                                             Long toCommitTime, int maxResults) {

        int limit = getTransactionsLimit();
        if (maxResults > limit) {
            maxResults = limit;
        }

        if (maxResults <= 0 || maxResults == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Maximum results must be a reasonable number.");
        }

        // We simulate an ID for the sys:deleted type
        Pair<Long, QName> deletedTypeQNamePair = qnameDAO.getQName(ContentModel.TYPE_DELETED);
        Long deletedTypeQNameId = deletedTypeQNamePair == null ? -1L : deletedTypeQNamePair.getFirst();

        SOLRTrackingParameters params = new SOLRTrackingParameters(deletedTypeQNameId);
        params.setFromIdInclusive(minTxnId);
        params.setFromCommitTimeInclusive(fromCommitTime);
        params.setToIdExclusive(maxTxnId);
        params.setToCommitTimeExclusive(toCommitTime);
        params.setLimit(maxResults);

        return template.selectList(SELECT_TRANSACTIONS, params, new RowBounds(0, maxResults));
    }

    private int getTransactionsLimit() {
        if (globalProperties == null) {
            return DEFAULT_SOLR_TRANSACTIONS_LIMIT;
        }

        String rawLimit = globalProperties.getProperty(SOLR_TRANSACTIONS_LIMIT_KEY);
        if (StringUtils.isBlank(rawLimit)) {
            return DEFAULT_SOLR_TRANSACTIONS_LIMIT;
        }

        return NumberUtils.toInt(rawLimit, DEFAULT_SOLR_TRANSACTIONS_LIMIT);
    }

    /**
     * {@inheritDoc}
     */
    public List<Node> getNodes(NodeParameters nodeParameters) {
        NodeParametersEntity params = new NodeParametersEntity(nodeParameters, qnameDAO);

        if (nodeParameters.getMaxResults() != 0 && nodeParameters.getMaxResults() != Integer.MAX_VALUE) {
            return template.selectList(
                    SELECT_NODES, params,
                    new RowBounds(0, nodeParameters.getMaxResults()));
        } else {
            return template.selectList(SELECT_NODES, params);
        }
    }
}
