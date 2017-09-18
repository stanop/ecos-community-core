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
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import ru.citeck.ecos.model.CounterModel;
import ru.citeck.ecos.node.NodeInfo;
import ru.citeck.ecos.node.NodeInfoFactory;
import ru.citeck.ecos.node.TemplateNodeInfo;
import ru.citeck.ecos.server.utils.Utils;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.utils.RepoUtils;
import ru.citeck.ecos.exception.ExceptionService;
import ru.citeck.ecos.exception.ExceptionTranslator;

import java.util.*;

public class EnumerationServiceImpl implements EnumerationService 
{
    private static final String FREEMARKER_PROCESSOR = "freemarker";
    
    private ServiceRegistry serviceRegistry;
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private SearchService searchService;
    private CounterService counterService;
    private TemplateService templateService;
    private ExceptionService exceptionService;
    private NodeInfoFactory nodeInfoFactory;
    
    @Override
    public NodeRef getTemplate(String templateName) {

        ParameterCheck.mandatoryString("templateName", templateName);

        StringBuilder query = new StringBuilder(String.format("TYPE:\"%s\"", CounterModel.TYPE_AUTONUMBER_TEMPLATE));
        String[] nameTokens = templateName.split("-");
        for (String nameToken : nameTokens) {
            query.append(String.format(" AND @%s:\"%s\"", ContentModel.PROP_NAME, nameToken));
        }

        ResultSet resultSet = null;
        List<NodeRef> resultList = Collections.emptyList();
        try {
            resultSet = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                                            SearchService.LANGUAGE_FTS_ALFRESCO, query.toString());
            if (resultSet != null) {
                resultList = resultSet.getNodeRefs();
            }
        } finally {
            if (resultSet != null) resultSet.close();
        }

        for (NodeRef ref : resultList) {
            Object name = nodeService.getProperty(ref, ContentModel.PROP_NAME);
            if (templateName.equals(name)) {
                return ref;
            }
        }
        return null;
    }

    @Override
    public boolean isTemplate(NodeRef nodeRef) {
        if(!nodeService.exists(nodeRef)) {
            throw new InvalidNodeRefException(nodeRef);
        }
        return dictionaryService.isSubClass(
                nodeService.getType(nodeRef), 
                CounterModel.TYPE_AUTONUMBER_TEMPLATE);
    }
    
    @Override
    public String getNumber(NodeRef template, NodeInfo nodeInfo) 
            throws EnumerationException 
    {
        Map<String,Object> model = new HashMap<String,Object>(1);
        model.put(KEY_NODE, newTemplateNode(nodeInfo));
        return getNumber(template, model);
    }

    @Override
    public String getNumber(NodeRef template, NodeRef nodeRef) 
            throws EnumerationException 
    {
        Map<String,Object> model = new HashMap<String,Object>(1);
        model.put(KEY_NODE, nodeRef);
        return getNumber(template, model);
    }
    
    @Override
    public String getNumber(NodeRef template, NodeInfo nodeInfo, String count)
            throws EnumerationException {
        Map<String,Object> model = new HashMap<String,Object>(1);
        model.put(KEY_NODE, newTemplateNode(nodeInfo));
        model.put(KEY_COUNT, count);
        return getNumber(template, model);
    }

    @Override
    public String getNumber(NodeRef template, NodeRef nodeRef, String count)
            throws EnumerationException {
        Map<String,Object> model = new HashMap<String,Object>(1);
        model.put(KEY_NODE, nodeRef);
        model.put(KEY_COUNT, count);
        return getNumber(template, model);
    }



    @Override
    public String getNumber(NodeRef template, Map<String,Object> model) 
            throws EnumerationException
    {
        if(template == null) {
            throw new IllegalArgumentException("Template is a mandatory parameter");
        }
        if(!nodeService.exists(template)) {
            throw new InvalidNodeRefException("Template does not exist: " + template, template);
        }
        if(!isTemplate(template)) {
            throw new IllegalArgumentException("Specified node is not a template: " + template);
        }
        
        try {
            
            model = prepareModel(template, model);
            
            String commonTemplate = getTemplatePart(template, CounterModel.PROP_COMMON_TEMPLATE, "");
            String numberTemplate = getTemplatePart(template, CounterModel.PROP_NUMBER_TEMPLATE, null);

            // generate number from number template
            return templateService.processTemplateString(FREEMARKER_PROCESSOR, 
                    commonTemplate + numberTemplate, model);
            
        } catch(Exception e) {
            ExceptionTranslator translator = exceptionService.getExceptionTranslator(
                    template,
                    CounterModel.PROP_ERROR_MESSAGE_CONFIG);
            String configuredMessage = translator.translateException(e);
            throw new EnumerationException(configuredMessage, e);
        }
    }

    private Map<String, Object> prepareModel(NodeRef template, Map<String, Object> initialModel) throws EnumerationException {
        Map<String,Object> model = new HashMap<String,Object>(initialModel.size()+1);
        model.putAll(initialModel);
        model.put(KEY_TEMPLATE, template);
        
        // special processing for 'node' entry
        Object nodeModel = model.get(KEY_NODE);
        if(nodeModel != null) {
            if(nodeModel instanceof TemplateNode || nodeModel instanceof TemplateNodeInfo) {
                // everything is cool
            } else if(nodeModel instanceof NodeRef) {
                nodeModel = newTemplateNode((NodeRef) nodeModel);
            } else if(nodeModel instanceof String) {
                nodeModel = newTemplateNode(new NodeRef((String) nodeModel));
            } else if(nodeModel instanceof ScriptNode) {
                nodeModel = newTemplateNode(((ScriptNode)nodeModel).getNodeRef());
            } else if(nodeModel instanceof Map) {
                Map<QName, Object> attributes = RepoUtils.convertStringMapToQNameMap((Map<?,?>) nodeModel, namespaceService);
                NodeInfo nodeInfo = nodeInfoFactory.createNodeInfo(attributes);
                nodeModel = new TemplateNodeInfo(nodeInfo, serviceRegistry, null);
            }
            model.put(KEY_NODE, nodeModel);
        }
        
        // special processing for 'count' entry
        Object count = model.get(KEY_COUNT);
        if(count == null) {
            
            String commonTemplate = getTemplatePart(template, CounterModel.PROP_COMMON_TEMPLATE, "");
            String counterTemplate = getTemplatePart(template, CounterModel.PROP_COUNTER_TEMPLATE, null);
            String initialValueTemplate = getTemplatePart(template, CounterModel.PROP_INITIAL_VALUE_TEMPLATE, "1");
            
            String counterName = templateService.processTemplateString(FREEMARKER_PROCESSOR, 
                    commonTemplate + counterTemplate, model);
            
            // if counter is new and we have initialValueTemplate
            // we should apply it
            if(counterService.getCounterLast(counterName) == null) {
        
                String initialValueString = templateService.processTemplateString(FREEMARKER_PROCESSOR, 
                        commonTemplate + initialValueTemplate, model);
        
                // parse this into integer
                int initialValue;
                try {
                    initialValue = Integer.parseInt(initialValueString);
                } catch(NumberFormatException e) {
                    throw new EnumerationException("Initial value evaluated to non-integer: '" + 
                            initialValueString + "' (template " + initialValueTemplate + ")", e);
                }
        
                // set the value to initialValue - 1, so that next value = initialValue
                counterService.setCounterLast(counterName, initialValue - 1);
            }
        
            // get next number from counter
            count = counterService.getCounterNext(counterName, true);
            model.put(KEY_COUNT, count);
        }
        
        return model;
    }

    private String getTemplatePart(NodeRef template, QName partName, String defaultValue) {
        String templatePart = (String) nodeService.getProperty(template, partName);
        templatePart = templatePart != null ? Utils.restoreFreemarkerVariables(templatePart) : defaultValue;
        if(templatePart == null) {
            throw new IllegalStateException("Template part '" + partName.getLocalName() + "' is not specified " + 
                    "in autonumber template '" + template + "'");
        }
        return templatePart;
    }

    private TemplateNode newTemplateNode(NodeRef nodeRef) {
        return new TemplateNode(nodeRef, serviceRegistry, null);
    }

    private Object newTemplateNode(NodeInfo nodeInfo) {
        return new TemplateNodeInfo(nodeInfo, serviceRegistry, null);
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        this.nodeService = serviceRegistry.getNodeService();
        this.searchService = serviceRegistry.getSearchService();
        this.templateService = serviceRegistry.getTemplateService();
        this.dictionaryService = serviceRegistry.getDictionaryService();
        this.namespaceService = serviceRegistry.getNamespaceService();
        this.counterService = (CounterService) serviceRegistry.getService(CiteckServices.COUNTER_SERVICE);
        this.exceptionService = (ExceptionService)serviceRegistry.getService(CiteckServices.EXCEPTION_SERVICE);
        this.nodeInfoFactory = (NodeInfoFactory) serviceRegistry.getService(CiteckServices.NODE_INFO_FACTORY);
    }

}
