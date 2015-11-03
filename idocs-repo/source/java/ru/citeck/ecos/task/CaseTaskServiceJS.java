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
package ru.citeck.ecos.task;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import ru.citeck.ecos.utils.JavaScriptImplUtils;

import java.util.List;

/**
 * @author Maxim Strizhov
 */
public class CaseTaskServiceJS extends AlfrescoScopableProcessorExtension {
    private static final Log log = LogFactory.getLog(CaseTaskServiceJS.class);

    private CaseTaskService caseTaskService;

    public void setCaseTaskService(CaseTaskService caseTaskService) {
        this.caseTaskService = caseTaskService;
    }

    public ScriptNode[] getTasks(Object nodeRef) {
        NodeRef nRef = getNodeRef(nodeRef);
        List<NodeRef> stages = caseTaskService.getTasks(nRef);
        return JavaScriptImplUtils.wrapNodes(stages, this);
    }

    public void startTask(Object nodeRef) {
        NodeRef ref = getNodeRef(nodeRef);
        caseTaskService.startTask(ref);
    }

    private NodeRef getNodeRef(Object object) {
        if(object == null)
            return null;
        if(object instanceof NodeRef)
            return (NodeRef) object;
        if(object instanceof String)
            return new NodeRef((String) object);
        if(object instanceof ScriptNode)
            return ((ScriptNode) object).getNodeRef();
        throw new IllegalArgumentException("Can not convert from " + object.getClass() + " to NodeRef");
    }
}
