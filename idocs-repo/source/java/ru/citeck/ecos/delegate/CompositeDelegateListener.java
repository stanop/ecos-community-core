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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CompositeDelegateListener implements DelegateListener
{
	private List<DelegateListener> listeners = new ArrayList<DelegateListener>();
	
	public void addDelegateListener(DelegateListener listener) {
		listeners.add(listener);
		Collections.sort(listeners, new Comparator<DelegateListener>() {
			public int compare(DelegateListener a, DelegateListener b) {
				return a.getPriority() - b.getPriority();
			}
		});
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void onRoleMemberAvailable(String roleFullName, String userName) {
		for(DelegateListener listener : listeners) {
			listener.onRoleMemberAvailable(roleFullName, userName);
		}
	}

	@Override
	public void onRoleMemberUnavailable(String roleFullName, String userName) {
		for(DelegateListener listener : listeners) {
			listener.onRoleMemberUnavailable(roleFullName, userName);
		}
	}

	@Override
	public void onRoleDelegateAvailable(String roleFullName, String userName) {
		for(DelegateListener listener : listeners) {
			listener.onRoleDelegateAvailable(roleFullName, userName);
		}
	}

	@Override
	public void onRoleDelegateUnavailable(String roleFullName, String userName) {
		for(DelegateListener listener : listeners) {
			listener.onRoleDelegateUnavailable(roleFullName, userName);
		}
	}

	@Override
	public void onUserDelegateAvailable(String userName, String delegateName) {
		for(DelegateListener listener : listeners) {
			listener.onUserDelegateAvailable(userName, delegateName);
		}
	}

	@Override
	public void onUserDelegateUnavailable(String userName, String delegateName) {
		for(DelegateListener listener : listeners) {
			listener.onUserDelegateUnavailable(userName, delegateName);
		}
	}

	@Override
	public void onUserAvailable(String userName) {
		for(DelegateListener listener : listeners) {
			listener.onUserAvailable(userName);
		}
	}

	@Override
	public void onUserUnavailable(String userName) {
		for(DelegateListener listener : listeners) {
			listener.onUserUnavailable(userName);
		}
	}

}
