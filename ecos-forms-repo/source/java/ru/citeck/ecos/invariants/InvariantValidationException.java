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
package ru.citeck.ecos.invariants;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public class InvariantValidationException extends InvariantsRuntimeException {

    private static final long serialVersionUID = 5231928288648874918L;
    
    private NodeRef nodeRef;
    private QName attributeName;
    private InvariantDefinition violatedInvariant;

    public InvariantValidationException(NodeRef nodeRef, QName attributeName, InvariantDefinition violatedInvariant) {
        super("Invariant validation failed on node " + nodeRef + ", attribute " + attributeName + ": " + violatedInvariant.getDescription());
        this.nodeRef = nodeRef;
        this.attributeName = attributeName;
        this.violatedInvariant = violatedInvariant;
    }

    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public QName getAttributeName() {
        return attributeName;
    }

    public InvariantDefinition getViolatedInvariant() {
        return violatedInvariant;
    }
    
}
