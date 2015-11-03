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

import java.io.InputStream;

import ru.citeck.ecos.utils.AbstractDeployerBean;

public class JournalTypesDeployer extends AbstractDeployerBean {
        
    protected JournalTypesDeployer() {
        super("journals");
    }

    private JournalService journalService;
    
    public void setJournalService(JournalService journalService) {
        this.journalService = journalService;
    }
    
    @Override
    protected void load(String location, InputStream journalsDefinition) {
        journalService.deployJournalTypes(journalsDefinition);
    }
    
}
