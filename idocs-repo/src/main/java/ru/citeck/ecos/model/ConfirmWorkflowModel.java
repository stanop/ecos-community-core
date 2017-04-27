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
package ru.citeck.ecos.model;

import org.alfresco.service.namespace.QName;

public interface ConfirmWorkflowModel {

	public static final String NAMESPACE = "http://www.citeck.ru/model/workflow/confirm/1.0";
	public static final String PREFIX = "wfcf";

	public static final QName TYPE_CONFIRM_DECISION = QName.createQName(NAMESPACE, "confirmDecision");
	public static final QName PROP_CONFIRM_VERSIONS = QName.createQName(NAMESPACE, "confirmVersions");
	public static final QName PROP_CONFIRM_ROLE = QName.createQName(NAMESPACE, "confirmerRole");
	public static final QName PROP_CONFIRM_TASK_ID = QName.createQName(NAMESPACE, "confirmTaskId");

	public static final QName ASPECT_HAS_CONFIRM_OUTCOME = QName.createQName(NAMESPACE, "hasConfirmOutcome");
	public static final QName PROP_CONFIRM_OUTCOME = QName.createQName(NAMESPACE, "confirmOutcome");

	public static final QName ASPECT_HAS_CONFIRM_DECISIONS = QName.createQName(NAMESPACE, "hasConfirmDecisions");
	public static final QName ASSOC_CONFIRM_DECISIONS = QName.createQName(NAMESPACE, "confirmDecisions");

	public static final QName ASPECT_HAS_CONFIRMABLE_VERSION = QName.createQName(NAMESPACE, "hasConfirmableVersion");
	public static final QName PROP_CONFIRMABLE_VERSION = QName.createQName(NAMESPACE, "confirmableVersion");
	public static final QName PROP_CURRENT_VERSION = QName.createQName(NAMESPACE, "currentVersion");

	public static final QName ASPECT_HAS_CONSIDERED_VERSIONS = QName.createQName(NAMESPACE, "hasConsideredVersions");
	public static final QName PROP_CONSIDERED_VERSIONS = QName.createQName(NAMESPACE, "consideredVersions");

	public static final QName ASPECT_CONFIRMED = QName.createQName(NAMESPACE, "confirmed");

	public static final QName ASSOC_CONFIRMERS = QName.createQName(NAMESPACE, "confirmers");
	public static final QName PROP_PRECEDENCE = QName.createQName(NAMESPACE, "precedence");

}
