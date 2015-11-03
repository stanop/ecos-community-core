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
package ru.citeck.ecos.form;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.AbstractFilter;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is a form filter of child associations.
 * This filter applies only child association of the persisted node.<br>
 * <br>
 * If child association has different primary parent association than
 * the persisted node, it 'moves' under the persisted node (i.e.
 * child association should have primary parent association - the persisted
 * node).<br>
 * <br>
 * The property {@code skipExceptions} - allows to continue processing
 * child associations if an error occurs.<br>
 * 
 * The property {@code removeSecondaryAssocs} - allows to remove all
 * non primary associations of the child association, if it is {@code true}.
 * 
 * @author Ruslan
 *
 */
public class ChildAssociationsFormFilterImpl
		extends AbstractFilter<Object, NodeRef>
		implements ChildAssociationsFormFilter {
	private static final Log log = LogFactory.getLog(ChildAssociationsFormFilterImpl.class);
	private final Map<String, QName> fieldsChildAssocs = new HashMap<String, QName>();
	private NodeService nodeService;
	private DictionaryService dictionaryService;
	/**
	 * It allows to continue processing child associations if an error occurs.
	 * By default it is {@code false}.
	 */
	private boolean skipExceptions;
	/**
	 * It allows to remove all non primary associations of the child
	 * association, if it is {@code true}, by default it is {@code false}.
	 */
	private boolean removeSecondaryAssocs = false;

	@Override
	public void beforeGenerate(Object item, List<String> fields,
			List<String> forcedFields, Form form, Map<String, Object> context) {
	}

	@Override
	public void afterGenerate(Object item, List<String> fields,
			List<String> forcedFields, Form form, Map<String, Object> context) {
	}

	@Override
	public void beforePersist(Object item, FormData data) {
	}

	@Override
	public void afterPersist(Object item, FormData data, NodeRef persistedObject) {
		if (nodeService.exists(persistedObject)) {
			for (Map.Entry<String, QName> entry : fieldsChildAssocs.entrySet()) {
				String fieldName = entry.getKey();
				QName childAssoc = entry.getValue();
				FieldData fieldData = data.getFieldData(fieldName);
				if (fieldData != null && fieldData.getValue() != null &&
						StringUtils.isNotEmpty(fieldData.getValue().toString())) {
					String childAssocsStr = fieldData.getValue().toString();
					String[] nodeRefsStr = childAssocsStr.split(",");
					for (String nodeRefStr : nodeRefsStr) {
						NodeRef nodeRef = new NodeRef(nodeRefStr);
						if (nodeService.exists(nodeRef)) {
							try {
								move(nodeRef, persistedObject, childAssoc);
							}
							catch (RuntimeException e) {
								if (log.isErrorEnabled()) {
									log.error("Can not filter child association nodeRef=" +
											nodeRef + "; type=" + childAssoc.getPrefixString(), e);
								}
								if (!skipExceptions)
									throw e;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * This implementation checks the input {@param childAssociation}.
	 * It should be child association.
	 */
	@Override
	public void registerChildAssociation(QName childAssociation) {
		if (childAssociation != null) {
			AssociationDefinition ad = dictionaryService.getAssociation(childAssociation);
			if (ad.isChild()) {
				StringBuilder fieldName = new StringBuilder("assoc_");
				fieldName.append(childAssociation.getPrefixString().replace(':', '_'));
				fieldName.append("_added");
				fieldsChildAssocs.put(fieldName.toString(), childAssociation);
				if (log.isDebugEnabled())
					log.debug("Registered child association=" + childAssociation.toPrefixString());
			}
			else {
				if (log.isWarnEnabled()) {
					log.warn("The input association is not child. input=" +
							childAssociation.getPrefixString());
				}
			}
		}
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setSkipExceptions(boolean skipExceptions) {
		this.skipExceptions = skipExceptions;
	}

	public void setRemoveSecondaryAssocs(boolean removeSecondaryAssocs) {
		this.removeSecondaryAssocs = removeSecondaryAssocs;
	}

	private void move(NodeRef target, NodeRef parent, QName childAssociation) {
		boolean shouldMove = false;
		List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(target);
		for (ChildAssociationRef parentAssoc : parentAssocs) {
			if (parentAssoc.isPrimary()) {
				if (!parent.equals(parentAssoc.getParentRef()))
					shouldMove = true;
			}
			else {
				if (removeSecondaryAssocs) {
					nodeService.removeChildAssociation(parentAssoc);
					if (log.isDebugEnabled()) {
						log.debug("The secondary parent associaton is successufully removed from " + 
								target + " parent association=" + parentAssoc.getTypeQName().getPrefixString() + 
								" parent node=" + parentAssoc.getParentRef());
					}
				}
			}
		}
		if (shouldMove) {
			nodeService.moveNode(
					target,
					parent,
					childAssociation,
					nodeService.getPrimaryParent(target).getQName());
			if (log.isDebugEnabled()) {
				log.debug("The node " + target + " successufully moved under " + 
						parent + " child association is " + childAssociation.toPrefixString());
			}
		}
	}

}
