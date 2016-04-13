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
package ru.citeck.ecos.journals;

import java.util.Collection;

import org.alfresco.service.ServiceRegistry;

import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

public class JournalServiceJS extends AlfrescoScopableProcessorExtension {
    
    private JournalService impl;
    private ServiceRegistry serviceRegistry;
    
    public JournalTypeJS getJournalType(String id) {
        JournalType type = impl.getJournalType(id);
        return type == null ? null : new JournalTypeJS(type, serviceRegistry);
    }
    
    public JournalTypeJS[] getAllJournalTypes() {
        Collection<JournalType> journalTypes = impl.getAllJournalTypes();
        JournalTypeJS[] result = new JournalTypeJS[journalTypes.size()];
        int i = 0;
        for(JournalType type : journalTypes) {
            result[i++] = new JournalTypeJS(type, serviceRegistry);
        }
        return result;
    }

    public void setJournalService(JournalService impl) {
        this.impl = impl;
    }
    
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
    
}
