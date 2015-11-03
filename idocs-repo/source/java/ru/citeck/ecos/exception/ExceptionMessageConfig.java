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

import org.apache.commons.lang.StringUtils;

final class ExceptionMessageConfig {
	private final String exception;
	private final String exceptionMessage;
	private final String errorMessage;
	private final Class<?> exceptionClass;

	/**
	 * It constructs exception message by specified line of input configuration.
	 * @param line - input line
	 * @throws IllegalArgumentException  - it throws this if input line is not appropriate to {@link ExceptionService}
	 * @throws ClassNotFoundException - it throws this if input exception class can not be found.
	 */
	public ExceptionMessageConfig(String line) throws IllegalArgumentException, ClassNotFoundException {
		line = line.trim();
		int first = line.indexOf(ExceptionService.ERROR_CONFIG_PART_DELIMITER);
		if (first <= 0)
			throw new IllegalArgumentException("Can not parse input config line=" + line);
		int last = line.indexOf(ExceptionService.ERROR_CONFIG_PART_DELIMITER, first + 1);
		if (last <= 0)
			throw new IllegalArgumentException("Can not parse input config line=" + line);
		this.exception = line.substring(0, first);
		this.exceptionClass = Class.forName(exception);
		this.exceptionMessage = line.substring(first+1, last);
		this.errorMessage = line.substring(last+1);
	}

	/**
	 * If input exception correspond to exception specified in configuration
	 * line, it returns corresponded message, if not, it returns {@code null}
	 * @param e - input exception
	 * @return
	 */
	public String getMessage(Throwable e) {
		if(!exceptionClass.isInstance(e)) {
			return null;
		}
		if(e.getMessage() == null) {
			return null;
		}
		if((StringUtils.isNotEmpty(exceptionMessage) && e.getMessage().indexOf(exceptionMessage) == -1) ||
				(StringUtils.isEmpty(exceptionMessage) && StringUtils.isEmpty(e.getMessage()))) {
			return null;
		}
		return errorMessage;
	}

}
