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

public abstract class AbstractDelegateListener implements DelegateListener
{
	private int priority;
	protected DelegateServiceImpl delegateService;

	public void init() {
		delegateService.addDelegateListener(this);
	}

	public void setDelegateService(DelegateServiceImpl delegateService) {
		this.delegateService = delegateService;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public void onRoleMemberAvailable(String roleFullName, String memberName) {
		// do nothing by default
	}

	@Override
	public void onRoleMemberUnavailable(String roleFullName, String memberName) {
		// do nothing by default
	}

	@Override
	public void onRoleDelegateAvailable(String roleFullName, String delegateName) {
		// do nothing by default
	}

	@Override
	public void onRoleDelegateUnavailable(String roleFullName,
			String delegateName) {
		// do nothing by default
	}

	@Override
	public void onUserDelegateAvailable(String userName, String delegateName) {
		// do nothing by default
	}

	@Override
	public void onUserDelegateUnavailable(String userName, String delegateName) {
		// do nothing by default
	}

	@Override
	public void onUserAvailable(String userName) {
		// do nothing by default
	}

	@Override
	public void onUserUnavailable(String userName) {
		// do nothing by default
	}

}
