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
package ru.citeck.ecos.behavior.common.documentlibrary;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *
 */
public class CopyToAction extends BaseScopableProcessorExtension {

    private List<CopyToActionStrategy> strategies = Collections.emptyList();


    public String evaluate(String sourceNodeRef, String destinationNodeRef) {
        NodeRef source = new NodeRef(sourceNodeRef), destination = new NodeRef(destinationNodeRef);
        ParameterCheck.mandatory("Source Node", source);
        ParameterCheck.mandatory("Destination Node", destination);
        for(CopyToActionStrategy strategy : strategies) {
            if (strategy.evaluate(source, destination)) {
                return "true";
            }
        }
        return "false";
    }

    public String copy(String sourceNodeRef, String destinationNodeRef) {
        NodeRef source = new NodeRef(sourceNodeRef), destination = new NodeRef(destinationNodeRef);
        ParameterCheck.mandatory("Source Node", source);
        ParameterCheck.mandatory("Destination Node", destination);
        String targetNodeRef = "";
        for(CopyToActionStrategy strategy : strategies) {
            if (strategy.evaluate(source, destination)) {
                targetNodeRef = strategy.copy(source, destination);
                break;
            }
        }
        return targetNodeRef;
    }

    public List<CopyToActionStrategy> getStrategies() {
        return strategies;
    }

    public void setStrategies(List<CopyToActionStrategy> strategies) {
        this.strategies = strategies;
    }
}
