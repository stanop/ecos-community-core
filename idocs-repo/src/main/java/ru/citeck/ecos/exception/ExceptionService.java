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
package ru.citeck.ecos.exception;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import ru.citeck.ecos.service.CiteckServices;

/**
 * It returns {@link ExceptionTranslator} which takes configuration
 * from specified {@code node} and specified property {@code config}.
 * 
 * {@code config} should be text property. Each line of this configuration 
 * defines translation message for input exception. So line contains:
 * exception class, matching text from input exception and translation message.
 * These three components in the line should be divided by ';' (so matched
 * text from input exception should not contain ';').
 * 
 * For example:
 * java.lang.Exception;Expression node.assocs["pecatt:attorney"] is undefined;Не задано доверенное лицо
 * java.lang.Exception;Expression node.assocs["pecatt:organization"] is undefined;Не задано юридическое лицо
 * ...
 * 
 * @author Ruslan
 *
 */
public interface ExceptionService {
	public static final QName NAME = CiteckServices.EXCEPTION_SERVICE;
	public static final String ERROR_CONFIG_PART_DELIMITER = ";";
	public static final String ERROR_CONFIG_LINE_DELIMITER = "\n";

	/**
	 * It returns configured {@link ExceptionTranslator}.
	 * @param node - it is node which contains configuration to translate input exception
	 * @param config - it is property name of configuration.
	 * @return
	 */
	ExceptionTranslator getExceptionTranslator(NodeRef nodeRef, QName config);

	/**
	 * It returns configured {@link ExceptionTranslator}.
	 * @param config - input configuration specified in text format.
	 * @return
	 */
	ExceptionTranslator getExceptionTranslator(String config);

}
