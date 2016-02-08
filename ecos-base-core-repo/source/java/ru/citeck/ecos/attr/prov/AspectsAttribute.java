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
package ru.citeck.ecos.attr.prov;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.attr.SingleAttributeProvider;
import ru.citeck.ecos.model.AttributeModel;
import ru.citeck.ecos.node.NodeInfo;
import ru.citeck.ecos.utils.DictionaryUtils;

public class AspectsAttribute extends SingleAttributeProvider<List<?>> {

    public AspectsAttribute() {
        super(AttributeModel.ATTR_ASPECTS);
    }

    @Override
    public List<QName> getValue(NodeRef nodeRef) {
        return asList(nodeService.getAspects(nodeRef));
    }
    
    @Override
    protected void setValue(NodeRef nodeRef, List<?> names) {
        if(names == null) return;
        List<QName> oldAspects = getValue(nodeRef);
        List<QName> newAspects = super.convertToQNameList(names);
        for(QName aspect : newAspects) {
            if(oldAspects.contains(aspect)) continue;
            nodeService.addAspect(nodeRef, aspect, null);
        }
        
        List<QName> allAspects = DictionaryUtils.expandClassNames(newAspects, dictionaryService);
        
        for(QName aspect : oldAspects) {
            if(allAspects.contains(aspect)) continue;
            nodeService.removeAspect(nodeRef, aspect);
        }
    }

    @Override
    protected void setValue(NodeInfo nodeInfo, List<?> names) {
        if(names == null) return;
        List<QName> aspects = convertToQNameList(names);
        nodeInfo.setAspects(aspects);
    }

    @Override
    public Class<?> getAttributeValueType(QName attributeName) {
        return QName.class;
    }

}
