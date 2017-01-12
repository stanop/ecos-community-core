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
package ru.citeck.ecos.archive;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.service.CiteckServices;

public interface ArchiveServicePolicies {

	public interface BeforeMovePolicy extends ClassPolicy {
		public static final String NAMESPACE = CiteckServices.CITECK_NAMESPACE;
		public static final QName QNAME = QName.createQName(NAMESPACE, "beforeMoveDocument");
		
		/**
		 * Called before moving document into the archive.
		 * @param targetNodeRef - archived node reference
		 * @param newParentRef - the new parent node reference
		 * @param cause - internal parameter, used in behavior
		 */
		void beforeMoveDocument(
				NodeRef targetNodeRef,
				NodeRef newParentRef,
				String cause);
	}

	public interface OnMovePolicy extends ClassPolicy {
		public static final String NAMESPACE = CiteckServices.CITECK_NAMESPACE;
		public static final QName QNAME = QName.createQName(NAMESPACE, "onMoveDocument");
		
		/**
		 * Called when the node has been moved. 
		 * @param targetNodeRef - archived node reference
		 * @param newChildAssocRef - the child association reference after moving.
		 * @param cause - internal parameter, used in behavior
		 */
		void onMoveDocument(
				NodeRef targetNodeRef,
				ChildAssociationRef newChildAssocRef,
				String cause);
	}
}
