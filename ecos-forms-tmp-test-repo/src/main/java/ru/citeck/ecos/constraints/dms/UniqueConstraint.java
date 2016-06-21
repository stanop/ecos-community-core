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

import org.alfresco.model.ContentModel;
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
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.model.DmsModel;


public class UniqueConstraint extends NodeRefAbstractConstraint {


    private static final String ERR_NON_STRING = "d_dictionary.constraint.string_length.non_string";
    private static final String ERR_DUPLICATE = "dms.unique.constraint.duplicate";
    private static SearchService searchService;
    private static NodeService nodeService;

    @Override
    protected void evaluateSingleValue(Object value, NodeRef nodeRef) {
        // do nothing
    }

    public void evaluateSingleValue(QName field, Object value, NodeRef nodeRef){


        // ensure that the value can be converted to a String
        String checkValue;
        try {
            checkValue = DefaultTypeConverter.INSTANCE.convert(String.class,
                    value);
        } catch (TypeConversionException e) {
            throw new ConstraintException(ERR_NON_STRING, value);
        }
        if (checkValue != null && checkValue.length() > 0) {

            String query = "=@" + field + ":\"" + checkValue.replaceAll("[\\\\\"]", "\\\\$0") +"\" AND NOT ASPECT:\"" + ContentModel.ASPECT_WORKING_COPY + "\"";
            ResultSet resultSet =null;
            try {
                resultSet = searchService.query(
                        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                        SearchService.LANGUAGE_FTS_ALFRESCO, query);
                for (ResultSetRow row : resultSet) {
                    //Find the object with the same INN and BIK
                    //If found check may be this is our current object
                    //If no, then we have a violation
                    NodeRef rowRef=row.getNodeRef();
                    if (nodeRef==null || rowRef==null || !rowRef.equals(nodeRef)) {
                        throw new ConstraintException(ERR_DUPLICATE, field.toString(),
                                checkValue);
                    }

                }
            } finally {
                if (resultSet != null) {
                    resultSet.close();
                }
            }

        }

    }

    public SearchService getSearchService() {
        return searchService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
