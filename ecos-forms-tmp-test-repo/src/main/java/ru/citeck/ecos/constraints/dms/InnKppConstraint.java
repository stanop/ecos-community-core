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
package ru.citeck.ecos.constraints.dms;

import org.alfresco.repo.dictionary.constraint.NodeRefAbstractConstraint;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;

import ru.citeck.ecos.model.DmsModel;

public class InnKppConstraint extends NodeRefAbstractConstraint {

	private static final String ERR_NON_STRING = "d_dictionary.constraint.string_length.non_string";
	private static final String ERR_DUPLICATE = "dms.constraint.innkpp.duplicate";
	private static SearchService searchService;
	private static NodeService nodeService;
	
	private static Boolean enabled;

	@Override
	protected void evaluateSingleValue(Object value,NodeRef nodeRef) {
		if(Boolean.FALSE.equals(enabled)) return;
		
		// ensure that the value can be converted to a String
		String checkValue;
		try { 
			checkValue = DefaultTypeConverter.INSTANCE.convert(String.class,
					value);
		} catch (TypeConversionException e) {
			throw new ConstraintException(ERR_NON_STRING, value);
		}
		
		String amount=(String)nodeService.getProperty(nodeRef, DmsModel.PROP_KPP);

		if (checkValue != null && checkValue.length() > 0) {
			String query = "@dms\\:INN:\"" + checkValue + "\" AND @dms\\:KPP:\"" + amount + "\"";
			ResultSet resultSet =null;
			try {
				resultSet = searchService.query(
						StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
						SearchService.LANGUAGE_LUCENE, query);
				for (ResultSetRow row : resultSet) {
					//Find the object with the same INN and BIK
					//If found check may be this is our current object
					//If no, then we have a violation
					NodeRef rowRef=row.getNodeRef();
					if (nodeRef==null || rowRef==null || !rowRef.equals(nodeRef)) {
						throw new ConstraintException(ERR_DUPLICATE,
								checkValue+" "+amount);
					}
					
				}
			} finally {
				if (resultSet != null) {
					resultSet.close();
				}
			}

		}

	}

	public void setSearchService(SearchService searchService) {
		InnKppConstraint.searchService = searchService;
	}
	
	public void setNodeService(NodeService nodeService) {
		InnKppConstraint.nodeService = nodeService;
	}

	public void setEnabled(Boolean enabled) {
		InnKppConstraint.enabled = enabled;
	}

}
