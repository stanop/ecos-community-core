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
package ru.citeck.ecos.deputy;

public interface AvailabilityService {

	/**
	 * Get specified user availability.
	 * 
	 * @param userName - user to query availability
	 * @return true if user is available, false if user is not available
	 */
	public abstract boolean getUserAvailability(String userName);

	/**
	 * Set specified user availability.
	 * 
	 * @param userName - user to set availability
	 * @param availability - true if user is available, false if user is not available
	 */
	public abstract void setUserAvailability(String userName,
			boolean availability);

	/**
	 * @see #getUserAvailability(String)
	 */
	public abstract boolean getCurrentUserAvailability();

	/**
	 * @see #setUserAvailability(String, boolean)
	 */
	public abstract void setCurrentUserAvailability(boolean availability);

}
