/*
 * Copyright (C) 2008-2017 Citeck LLC.
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
package ru.citeck.ecos.webscripts.invariants;

import java.util.*;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import ru.citeck.ecos.invariants.InvariantDefinition;
import ru.citeck.ecos.invariants.InvariantService;
import ru.citeck.ecos.utils.DictionaryUtils;
import ru.citeck.ecos.utils.RepoUtils;

public class InvariantsGet extends DeclarativeWebScript {

    final private static Logger logger = Logger.getLogger(InvariantsGet.class);

    private static final String PARAM_TYPE = "type";
    private static final String PARAM_NODEREF = "nodeRef";
    private static final String PARAM_BASE_REF = "baseRef";
    private static final String PARAM_ROOT_ATTRIBUTE_NAME = "rootAttributeName";
    private static final String PARAM_ASPECTS = "aspects";
    private static final String PARAM_ATTRIBUTES = "attributes";
    private static final String PARAM_MODE = "mode";
    private static final String PARAM_INLINE_EDIT = "inlineEdit";
    private static final String MODEL_INVARIANTS = "invariants";
    private static final String MODEL_CLASS_NAMES = "classNames";
    private static final String MODEL_MODEL = "model";
    private static final String MODE_EDIT = "edit";

    private InvariantService invariantService;
    private NamespacePrefixResolver prefixResolver;
    private ServiceRegistry serviceRegistry;
    private NodeService nodeService;
    private DictionaryService dictionaryService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        String typeParam = req.getParameter(PARAM_TYPE);
        String nodeRefParam = req.getParameter(PARAM_NODEREF);
        String baseRefParam = req.getParameter(PARAM_BASE_REF);
        String rootAttributeNameParam = req.getParameter(PARAM_ROOT_ATTRIBUTE_NAME);
        String aspectsParam = req.getParameter(PARAM_ASPECTS);
        String attributesParam = req.getParameter(PARAM_ATTRIBUTES);
        String modeParam = req.getParameter(PARAM_MODE);
        String inlineEditParam = req.getParameter(PARAM_INLINE_EDIT);

        // Change current form's mode to "edit", when inlineEdit parameter is true.
        if (StringUtils.isNotEmpty(inlineEditParam)) {
            try {
                boolean isInlineEdit = Boolean.parseBoolean(inlineEditParam);
                if (isInlineEdit) {
                    modeParam = MODE_EDIT;
                }
            } catch (Exception e) {
                logger.warn("Incorrect inlineEdit parameter's value: " + inlineEditParam);
            }
        }

        Set<QName> classNames = new LinkedHashSet<>();

        NodeRef nodeRef = null;
        NodeRef baseRef = null;
        QName rootAttributeName = null;

        if (StringUtils.isNotEmpty(typeParam)) {
            QName type = QName.createQName(typeParam, prefixResolver);
            classNames.add(type);
        } else if (StringUtils.isNotEmpty(nodeRefParam)) {
            if (!NodeRef.isNodeRef(nodeRefParam)) {
                status.setCode(Status.STATUS_BAD_REQUEST, "Parameter '" + PARAM_NODEREF
                        + "' should contain nodeRef");
                return null;
            }
            nodeRef = new NodeRef(nodeRefParam);
            classNames.add(nodeService.getType(nodeRef));
            classNames.addAll(nodeService.getAspects(nodeRef));
        }

        if (StringUtils.isNotEmpty(aspectsParam)) {
            classNames.addAll(splitQNames(aspectsParam));
        }

        if (StringUtils.isNotEmpty(baseRefParam) && NodeRef.isNodeRef(baseRefParam)) {
            baseRef = new NodeRef(baseRefParam);
        }

        if (StringUtils.isNotEmpty(rootAttributeNameParam)) {
            rootAttributeName = QName.createQName(rootAttributeNameParam, prefixResolver);
        }

        List<QName> attributeNames = null;
        if (attributesParam != null) {
            attributeNames = attributesParam.isEmpty() ? Collections.emptyList() : splitQNames(attributesParam);
        }

        List<InvariantDefinition> invariants = invariantService.getInvariants(classNames, attributeNames, nodeRef,
                baseRef, rootAttributeName, modeParam);

        Map<String, Object> model = new HashMap<>();
        model.put(MODEL_INVARIANTS, invariants);
        model.put(MODEL_CLASS_NAMES, DictionaryUtils.expandClassNames(classNames, dictionaryService));
        model.put(MODEL_MODEL, RepoUtils.buildDefaultModel(serviceRegistry));
        return model;
    }

    private List<QName> splitQNames(String qnamesString) {
        List<QName> qnames = null;
        if (qnamesString != null && !qnamesString.isEmpty()) {
            String[] qnamesArray = qnamesString.split(",");
            qnames = new ArrayList<>(qnamesArray.length);
            for (String qname : qnamesArray) {
                qnames.add(QName.createQName(qname, prefixResolver));
            }
        }
        return qnames;
    }

    public void setInvariantService(InvariantService invariantService) {
        this.invariantService = invariantService;
    }

    public void setPrefixResolver(NamespacePrefixResolver prefixResolver) {
        this.prefixResolver = prefixResolver;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        nodeService = serviceRegistry.getNodeService();
        dictionaryService = serviceRegistry.getDictionaryService();
    }

}
