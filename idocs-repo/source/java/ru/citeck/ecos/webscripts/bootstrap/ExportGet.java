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
package ru.citeck.ecos.webscripts.bootstrap;

import java.io.File;
import java.io.IOException;

import org.alfresco.repo.exporter.ACPExportPackageHandler;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.view.ExportPackageHandler;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.Location;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Simple web-script to export any path into ACP.
 * @author Sergey Tiunov
 */
public class ExportGet extends AbstractWebScript
{
	
	private static final String PARAM_STORE = "store";
	private static final String PARAM_PATH = "path";

	private ExporterService exporterService;
	private MimetypeService mimetypeService;

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException 
	{
		
		StoreRef store = null;
		if(req.getParameter(PARAM_STORE) != null) {
			store = new StoreRef(req.getParameter(PARAM_STORE));
		} else {
			store = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
		}
		
		String path = req.getParameter(PARAM_PATH);
		if(path == null) {
			throw new WebScriptException("Parameter " + PARAM_PATH + " should be specified");
		}
		
		Location location = new Location(store);
		location.setPath(path);
		ExporterCrawlerParameters parameters = new ExporterCrawlerParameters();
		parameters.setExportFrom(location);

		String packageName = "archive";
		File contentDir = new File(packageName);
		File dataFile = new File(packageName);
		
		// setup an ACP Package Handler to export to an ACP file format
		ExportPackageHandler handler = new ACPExportPackageHandler(res.getOutputStream(), dataFile, contentDir, mimetypeService);
		
		// now export (note: we're not interested in progress in the example)
		exporterService.exportView(handler, parameters, null);
		
		res.setHeader("Content-disposition", "attachment; filename=archive.acp");
		
	}

	/**
	 * @return the exporterService
	 */
	public ExporterService getExporterService() {
		return exporterService;
	}

	/**
	 * @param exporterService the exporterService to set
	 */
	public void setExporterService(ExporterService exporterService) {
		this.exporterService = exporterService;
	}

	/**
	 * @return the mimetypeService
	 */
	public MimetypeService getMimetypeService() {
		return mimetypeService;
	}

	/**
	 * @param mimetypeService the mimetypeService to set
	 */
	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}
	
}
