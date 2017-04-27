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

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ProcessorHelper {
	
	private NodeService nodeService;
	private ContentService contentService;

	public void setServiceRegistry(ServiceRegistry services) {
		this.nodeService = services.getNodeService();
		this.contentService = services.getContentService();
	}

	public NodeRef getNodeRef(Object something) {
		
		if(something == null) {
			return null;
		}
		
		if(something instanceof NodeRef) {
			return (NodeRef) something;
		}
		
		if(something instanceof String) {
			return new NodeRef(something.toString());
		}
		
		if(something instanceof ScriptNode) {
			return ((ScriptNode)something).getNodeRef();
		}
		
		throw new IllegalStateException("Can not transform " + something.getClass().toString() + " to NodeRef");
	}
	
	public NodeRef getExistingNodeRef(Object something) {
		NodeRef nodeRef = getNodeRef(something);
		return nodeService.exists(nodeRef) ? nodeRef : null;
	}
	
	public NodeRef needNodeRef(Object something) {
		NodeRef nodeRef = getNodeRef(something);
		if(nodeRef == null) {
			throw new IllegalStateException("Caller needs nodeRef, but it is null");
		}
		return nodeRef;
	}
	
	public NodeRef needExistingNodeRef(Object something) {
		NodeRef nodeRef = needNodeRef(something);
		if(!nodeService.exists(nodeRef)) {
			throw new IllegalArgumentException("Node does not exist: " + nodeRef);
		}
		return nodeRef;
	}
	
	public Map<String,Object> getContentProperties(ContentAccessor accessor) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(ProcessorConstants.KEY_ENCODING, accessor.getEncoding());
		model.put(ProcessorConstants.KEY_MIMETYPE, accessor.getMimetype());
		return model;
	}
	
	public void putContentProperties(ContentAccessor accessor, Map<String,Object> model) {
		accessor.setEncoding((String) model.get(ProcessorConstants.KEY_ENCODING));
		accessor.setMimetype((String) model.get(ProcessorConstants.KEY_MIMETYPE));
	}
	
	public ContentReader getContentReader(DataBundle input) {
		
		InputStream inputStream = input.getInputStream();
		if(inputStream == null) {
			return null;
		}
		
		OutputStream outputStream = null;
		try {
			ContentWriter tempWriter = contentService.getTempWriter();
			this.putContentProperties(tempWriter, input.getModel());

			outputStream = tempWriter.getContentOutputStream();
			byte[] buffer = new byte[1000];
			int size = 0;
			while(true) {
				size = inputStream.read(buffer);
				if(size <= 0) break;
				outputStream.write(buffer, 0, size);
			}
			outputStream.flush();
			outputStream.close();

			return tempWriter.getReader();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);
		}
	}
	
	public DataBundle getDataBundle(ContentReader reader, Map<String,Object> model) {

		InputStream inputStream = null;
		Map<String,Object> newModel = DataBundle.emptyModel();
		newModel.putAll(model);
		if(reader != null && reader.exists()) {
			inputStream = reader.getContentInputStream();
			newModel.putAll(getContentProperties(reader));
		}
		
		return new DataBundle(inputStream, newModel);
	}

	public DataBundle backupDataBundle(DataBundle dataBundle) {
		
		if(dataBundle instanceof DataBundleBackup) {
			return dataBundle;
		}
		
		InputStream inputStream = dataBundle.getInputStream();
		
		// data bundle without input stream does not need to be backed up 
		if(inputStream == null) {
			return dataBundle;
		}
		
		File backupFile = TempFileProvider.createTempFile("", "");
		FileOutputStream outputStream = null;
		
		try {
			outputStream = new FileOutputStream(backupFile);
			
			byte[] buffer = new byte[1000];
			while(true) {
				int size = inputStream.read(buffer);
				if(size <= 0) break;
				outputStream.write(buffer, 0, size);
			}
			
			outputStream.flush();
			
			return new DataBundleBackup(backupFile, dataBundle.getModel());
			
		} catch (Exception e) {
			throw new RuntimeException("Could not create backup for input file", e);
		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);
		}
	}

}
