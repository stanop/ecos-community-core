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
package ru.citeck.ecos.delegate;

import java.util.List;

/**
 * Extended interface for Java implementation of DelegateService.
 *  
 * Delegate Service employs Delegate Listeners (@see DelegateListener) to add custom behaviour 
 *  whenever events important for delegation occur.
 * These events can be result of activity of DelegateService or other services.
 * When it is DelegateService, it should call Delegate Listeners immediately.
 * When it is other services, some mechanism (listener) should call DelegateService listener method to activate DelegateListeners.
 * DelegateService should be abstract towards the implementation of listener of necessary events.
 * All that is important is that this listeners should call corresponding methods of DelegateService.
 * 
 * @author Sergey Tiunov
 *
 */
public interface DelegateService extends DelegateServiceGeneric<List<String>, List<String>, List<String>> 
{
	
	/////////////////////////////////////////////////////////////////
	//                    LISTENER INTERFACE                       //
	/////////////////////////////////////////////////////////////////

	/**
	 * This should be called, whenever user availability is changed.
	 * 
	 * This is intended to be called by some listener, that listens to corresponding event.
	 * Delegate Service should transmit this event to registered DelegateListeners.
	 * 
	 * @param userName
	 */
	public void userAvailabilityChanged(String userName);
	
	/**
	 * This should be called, whenever user is added or removed from/to group.
	 * 
	 * This is intended to be called by some listener, that listens to corresponding event.
	 * Delegate Service should transmit this event to registered DelegateListeners.
	 * 
	 * @param userName
	 * @param groupFullName
	 */
	public void userMembershipChanged(String userName, String groupFullName);
	
}
