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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * It implements {@link ExceptionTranslator} .
 * 
 * @author Ruslan
 *
 */
class ExceptionTranslatorImpl implements ExceptionTranslator {
	private static final Log log = LogFactory.getLog(ExceptionTranslatorImpl.class);
	private final Collection<ExceptionMessageConfig> configs;

	public ExceptionTranslatorImpl(String config) {
		if (StringUtils.isBlank(config)) {
			configs = Collections.emptyList();
		} else {
			String[] lines = config.split(ExceptionService.ERROR_CONFIG_LINE_DELIMITER);
			configs = new ArrayList<>(lines.length);
			for(int i = 0; i < lines.length; i++) {
				try {
					ExceptionMessageConfig m = new ExceptionMessageConfig(lines[i]);
					configs.add(m);
				} catch(Throwable e) {
					if (log.isErrorEnabled())
						log.error("Skipping wrong error config line: " + lines[i], e);
				}
			}
		}
		if (configs.size() == 0 && log.isWarnEnabled())
			log.warn("Internal config is empty! input config=" + config);
	}

	/**
	 * This implementation returns message from input exception if
	 * there is no corresponded description of the one exception.
	 */
	@Override
	public String translateException(Throwable t) {
		String configuredMessage = t.getMessage();
		for (ExceptionMessageConfig config : configs) {
			String message = config.getMessage(t);
			if(message != null) {
				configuredMessage = message;
				break;
			}
		}
		return configuredMessage;
	}

}
