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
package ru.citeck.ecos.processor;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateService;

/**
 * Build Default Model - inserts default template model (company home, user home, person, ...) into data bundle.
 * 
 * @author Sergey Tiunov
 *
 */
public class BuildDefaultModel extends AbstractDataBundleLine
{
	private TemplateService templateService;
	private Repository repositoryHelper;
	
	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}

	public void init() {
		this.templateService = serviceRegistry.getTemplateService();
	}

	@Override
	public DataBundle process(DataBundle input) {
		Map<String,Object> model = input.getModel();
		
		NodeRef person = repositoryHelper.getPerson();
		NodeRef companyHome = repositoryHelper.getCompanyHome();
		NodeRef userHome = repositoryHelper.getUserHome(person);
		
		Map<String,Object> defaultModel = templateService.buildDefaultModel(person, companyHome, userHome, null, null);
		
		Map<String,Object> newModel = new HashMap<String,Object>(model.size() + defaultModel.size());
		newModel.putAll(model);
		newModel.putAll(defaultModel);
		
		return new DataBundle(input, newModel);
	}

}
