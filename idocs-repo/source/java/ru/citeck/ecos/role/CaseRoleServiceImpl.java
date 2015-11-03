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
package ru.citeck.ecos.role;

import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.model.ICaseRoleModel;
import ru.citeck.ecos.task.CaseTaskService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Maxim Strizhov
 */
public class CaseRoleServiceImpl implements CaseRoleService {
    private static final Log log = LogFactory.getLog(CaseTaskService.class);
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private DictionaryService dictionaryService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    public void init() {
    }

    @Override
    public List<NodeRef> getRoles(NodeRef nodeRef) {
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, ICaseRoleModel.ASSOC_ROLES, RegexQNamePattern.MATCH_ALL);
        List<NodeRef> result = new ArrayList<>(assocs.size());
        for (ChildAssociationRef assoc : assocs) {
            result.add(assoc.getChildRef());
        }
        return result;
    }
}
