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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

/**
 * This is a kind of DataBundle, in which content can be read several times.
 * It is initialized from a file, and every time an input stream is requested, it is constructed from scratch. 
 * 
 * @author Sergey Tiunov
 *
 */
class DataBundleBackup extends DataBundle
{
	private File backupFile;
	
	/**
	 * Create Data Bundle backup from backup file and model.
	 * 
	 * @param backupFile
	 * @param model
	 */
	/*package*/ DataBundleBackup(File backupFile, Map<String,Object> model) 
	{
		init(null, model);
		this.backupFile = backupFile;
	}
	
	@Override
	public InputStream getInputStream() {
		try {
			return new FileInputStream(backupFile);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

}
