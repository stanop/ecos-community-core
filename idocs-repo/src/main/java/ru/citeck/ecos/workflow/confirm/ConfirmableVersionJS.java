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
package ru.citeck.ecos.workflow.confirm;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;

public class ConfirmableVersionJS extends BaseScopableProcessorExtension 
{
	private ConfirmHelper impl;
	
	public void saveConfirmable(DelegateExecution execution) {
		impl.saveConfirmableVersion(execution);
	}

    public void saveCurrent(DelegateExecution execution) {
        impl.saveCurrentVersion(execution);
    }

    public boolean isConfirmable(DelegateExecution execution) {
        return !impl.isConfirmableVersion(execution);
    }

    public boolean isLatestVersionConfirmedByAll(DelegateExecution execution) {
        return !impl.isLatestVersionConfirmedByAll(execution);
    }

    public boolean isChanged(DelegateExecution execution) {
        return !impl.isCurrentVersion(execution);
    }
	
	public ConfirmHelper getImpl() {
		return impl;
	}

	public void setImpl(ConfirmHelper impl) {
		this.impl = impl;
	}
	
}
