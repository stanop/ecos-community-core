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
package ru.citeck.ecos.processor.report;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import ru.citeck.ecos.attr.NodeAttributeService;
import ru.citeck.ecos.processor.AbstractDataBundleLine;
import ru.citeck.ecos.processor.DataBundle;
import ru.citeck.ecos.service.AlfrescoServices;
import ru.citeck.ecos.service.CiteckServices;
import ru.citeck.ecos.template.TemplateNodeService;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import org.alfresco.repo.template.TemplateNode;
import org.alfresco.repo.template.BaseContentNode.TemplateContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.ClassUtils;
import ru.citeck.ecos.utils.DictionaryUtils;

/**
 * Create list with report data to output
 *
 * @author Alexey Moiseev <alexey.moiseev@citeck.ru>
 */
public class ReportProducer extends AbstractDataBundleLine {
	
	private final static String NAMESPACE_BEGIN = "" + QName.NAMESPACE_BEGIN;
	
	private static final String NODES = "nodes";
	private static final String REPORT_COLUMNS = "reportColumns";
	private static final String COLUMN_ATTR = "attribute";
	private static final String COLUMN_DATE_FORMAT = "dateFormat";
	private static final String ROW_NUM = "rowNum";
	private static final String DEFAULT_DATE_FORMAT = "dd.MM.yyyy HH:mm";
	
	public static final String DATA_TYPE_ATTR = "type";
	public static final String DATA_VALUE_ATTR = "value";
	
	private TemplateNodeService templateNodeService;
	private NodeAttributeService nodeAttributeService;
	private NamespaceService namespaceService;
	private MessageService messageService;
	private DictionaryService dictionaryService;

	@Override
	public void init() {
		super.init();
		nodeAttributeService = (NodeAttributeService)serviceRegistry.getService(CiteckServices.NODE_ATTRIBUTE_SERVICE);
		messageService = (MessageService)serviceRegistry.getService(AlfrescoServices.MESSAGE_SERVICE);
		namespaceService = serviceRegistry.getNamespaceService();
		dictionaryService = serviceRegistry.getDictionaryService();
	}

	@SuppressWarnings("unchecked")
	@Override
    public DataBundle process(DataBundle input) {
        Map<String, Object> model = input.needModel();
        
        HashMap<String, Object> newModel = new HashMap<String, Object>();
        newModel.putAll(model);
        
        List<Map<String, String>> reportColumns = (List<Map<String, String>>) model.get(REPORT_COLUMNS);
		List<NodeRef> nodes = (List<NodeRef>) model.get(NODES);
		newModel.put("reportData", produceReportData(reportColumns, nodes));
        
        return new DataBundle(newModel);
    }
    
    private List<List<Map<String, Object>>> produceReportData(List<Map<String, String>> reportColumns, List<NodeRef> nodes) {
    	List<List<Map<String, Object>>> res = new ArrayList<List<Map<String, Object>>>();

    	if ((reportColumns != null) && (reportColumns.size() > 0) && (nodes != null) && (nodes.size() > 0)) {
			int i = 0;
			for (NodeRef node : nodes) {
				if (node != null) {
					List<Map<String, Object>> row = new ArrayList<Map<String, Object>>();

					for (Map<String, String> col : reportColumns) {
						Map<String, Object> data = new HashMap<String, Object>();

						// default type
						data.put(DATA_TYPE_ATTR, "String");

						String colAttribute = col.get(COLUMN_ATTR);
						String colDateFormat = col.get(COLUMN_DATE_FORMAT);

						if (colAttribute != null) {
							if (colAttribute.equals(ROW_NUM)) {
								data.put(DATA_TYPE_ATTR, "Integer");
								data.put(DATA_VALUE_ATTR, i + 1);
							} else {
								QName colAttrQName = QName.resolveToQName(namespaceService, colAttribute);
								Object value = nodeAttributeService.getAttribute(node, colAttrQName);
								data.put(DATA_VALUE_ATTR, getFormattedValue(colAttrQName, value, colDateFormat, ""));
							}
						}

						row.add(data);
					}

					res.add(row);
					i++;
				}
			}
		}
    	return res;
    }
    
	@SuppressWarnings("rawtypes")
	private String getFormattedValue(QName property, Object value, String dateFormat, String oldValue) {
    	String res = oldValue;

    	if ((dateFormat == null) || (dateFormat.isEmpty())){
			dateFormat = DEFAULT_DATE_FORMAT;
		}

    	SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

    	if (value != null) {
    		if (value instanceof Boolean) {
    			boolean bValue = (Boolean) value;
    			if (bValue)
    				res = I18NUtil.getMessage("label.yes");
    			else
    				res = I18NUtil.getMessage("label.no");
    		} else if (ClassUtils.isPrimitiveOrWrapper(value.getClass())) {
    			res = String.valueOf(value);
    		} else if (value instanceof String) {
    			res = getLabel(property, (String) value);
    		} else if (value instanceof List) {
    			for (Object o : (List) value) {
					res = getFormattedValue(property, o, dateFormat, res);
				}
    		} else if (value instanceof Date) {
				res = sdf.format((Date) value);
    		} else if (value instanceof TemplateNode) {
				res = getNodeAsString((TemplateNode) value);
			} else if (value instanceof NodeRef) {
				res = getNodeAsString(new TemplateNode((NodeRef)value, serviceRegistry, null));
    		} else if (value instanceof QName) {
    			res = shortQName(value.toString());
    		} else if (value instanceof TemplateContentData) {
				TemplateContentData data = (TemplateContentData) value;
				res = "/api/node/" + (data.getUrl() != null ? data.getUrl().replaceFirst("/d/d/", "") : "");
    		} else if (value.toString() != null) {
    			res = value.toString();
    		}
    	}
    	return res;
    }

	private String getLabel(QName property, String value) {
		if (dictionaryService.getProperty(property) == null) return value;

		Set<ConstraintDefinition> constraints =
				DictionaryUtils.getAllConstraintsForProperty(property, serviceRegistry.getDictionaryService());

		for (ConstraintDefinition constraintDefinition : constraints) {

			Constraint constraint = constraintDefinition.getConstraint();
			if(!(constraint instanceof ListOfValuesConstraint)) continue;

			ListOfValuesConstraint constraintList = (ListOfValuesConstraint) constraint;
			List<String> allowedValues = constraintList.getAllowedValues();

			if(!allowedValues.contains(value)) continue;

			return constraintList.getDisplayLabel(value, messageService);
		}
		return value;
	}
    
    private String getNodeAsString(TemplateNode node) {
    	String result = "";
    	
    	if (node.hasPermission("Read")) {
    		if (node.getTypeShort().equals("cm:person")) {
    			Map<String, Serializable> properties = node.getProperties();
    					
    			if (properties != null) {
    				if (properties.get("cm:lastName") != null)
    					result += properties.get("cm:lastName");
    				
    				if (properties.get("cm:firstName") != null) {
    					result += (result.length() > 0) ? " " : "";
    					result += properties.get("cm:firstName");
    				}
    				
    				if (properties.get("cm:middleName") != null) {
    					result += (result.length() > 0) ? " " : "";
    					result += properties.get("cm:middleName");
    				}
    			}
    		} else if (node.getName() != null)
    			result = node.getName();
    			
    	}
    	
    	return result;
    }

    private String shortQName(String s) {
		QName qname = QName.resolveToQName(serviceRegistry.getNamespaceService(), s);
		return qname.toPrefixString(namespaceService);
    }

	public TemplateNodeService getTemplateNodeService() {
		return templateNodeService;
	}

	public void setTemplateNodeService(TemplateNodeService templateNodeService) {
		this.templateNodeService = templateNodeService;
	}
	
}