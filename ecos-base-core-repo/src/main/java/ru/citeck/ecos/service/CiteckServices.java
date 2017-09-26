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
package ru.citeck.ecos.service;

import org.alfresco.service.namespace.QName;

public interface CiteckServices {

	public String CITECK_NAMESPACE = "http://www.citeck.ru";

	public QName GRANT_PERMISSION_SERVICE = QName.createQName(CITECK_NAMESPACE, "GrantPermissionService");
	public QName CONFISCATE_SERVICE = QName.createQName(CITECK_NAMESPACE, "ConfiscateService");
	public QName AVAILABILITY_SERVICE = QName.createQName(CITECK_NAMESPACE, "AvailabilityService");
	public QName DEPUTY_SERVICE = QName.createQName(CITECK_NAMESPACE, "DeputyService");
	public QName ORG_STRUCT_SERVICE = QName.createQName(CITECK_NAMESPACE, "OrgStructService");
	public QName ORG_META_SERVICE = QName.createQName(CITECK_NAMESPACE, "OrgMetaService");
	public QName COUNTER_SERVICE = QName.createQName(CITECK_NAMESPACE, "CounterService");
	public QName ENUMERATION_SERVICE = QName.createQName(CITECK_NAMESPACE, "EnumerationService");
	public QName CONFIRM_SERVICE = QName.createQName(CITECK_NAMESPACE, "ConfirmService");
	public QName CARD_TEMPLATE_SERVICE = QName.createQName(CITECK_NAMESPACE, "CardTemplateService");
	public QName CARDLET_SERVICE = QName.createQName(CITECK_NAMESPACE, "CardletService");
	public QName EXCEPTION_SERVICE = QName.createQName(CITECK_NAMESPACE, "ExceptionService");
	public QName ADVANCED_WORKFLOW_SERVICE = QName.createQName(CITECK_NAMESPACE, "advancedWorkflowService");
	public QName WORKFLOW_MIRROR_SERVICE = QName.createQName(CITECK_NAMESPACE, "workflowMirrorService");
	public QName HISTORY_SERVICE = QName.createQName(CITECK_NAMESPACE, "historyService");
	public QName CRITERIA_SEARCH_SERVICE = QName.createQName(CITECK_NAMESPACE, "criteriaSearchService");
	public QName LIFECYCLE_SERVICE = QName.createQName(CITECK_NAMESPACE, "lifeCycleService");
	public QName CASE_ACTIVITY_SERVICE = QName.createQName(CITECK_NAMESPACE, "caseActivityService");
	public QName JOURNAL_SERVICE = QName.createQName(CITECK_NAMESPACE, "journalService");

	public QName NODE_ATTRIBUTE_SERVICE = QName.createQName(CITECK_NAMESPACE, "nodeAttributeService");
	public QName INVARIANT_SERVICE = QName.createQName(CITECK_NAMESPACE, "invariantService");

    public QName NODE_INFO_FACTORY = QName.createQName(CITECK_NAMESPACE, "NodeInfoFactory");
	public QName CASE_ROLE_SERVICE = QName.createQName(CITECK_NAMESPACE, "caseRoleService");
	public QName CASE_STATUS_SERVICE = QName.createQName(CITECK_NAMESPACE, "caseStatusService");

}
