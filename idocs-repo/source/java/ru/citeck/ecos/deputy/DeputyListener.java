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

public interface DeputyListener {
	
	public int getPriority();
	
	public void onRoleMemberAvailable(String roleFullName, String memberName);
	public void onRoleMemberUnavailable(String roleFullName, String memberName);
	public void onRoleDeputyAvailable(String roleFullName, String deputyName);
	public void onRoleDeputyUnavailable(String roleFullName, String deputyName);
	public void onUserDeputyAvailable(String userName, String deputyName);
	public void onUserDeputyUnavailable(String userName, String deputyName);
	public void onUserAvailable(String userName);
	public void onUserUnavailable(String userName);

}
