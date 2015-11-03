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
package ru.citeck.ecos.server.utils;

import org.springframework.extensions.webscripts.WebScriptRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Utils {

    public static String encodeContentDispositionForDownload(WebScriptRequest request, String fileName,
			String fileExtension, boolean isInline) throws UnsupportedEncodingException {
		if (fileName == null)
			throw new IllegalArgumentException("Value of the \"filename\" parameter cannot be null!");
		
		if(fileExtension != null && fileExtension.length() > 0) {
			// Removing file extension from the name if any
			int dot_index = fileName.lastIndexOf(".");
			if (dot_index > -1) {
				fileName = fileName.substring(0, dot_index) + "." + fileExtension;
			}
		}
			
		String contentDisposition = isInline ? "inline; " : "attachment; ";
		String agent = request.getHeader("USER-AGENT").toLowerCase();

        String fName = URLEncoder.encode(fileName, "UTF-8");
        if (agent != null && agent.contains("firefox")) {
            fName = fName.replace("+", "%20");
        } else {
            fName = fName.replace('+', ' ');
        }
        if (agent != null && !agent.contains("msie"))
        {
            fName = fName.replace("%28", "(");
            fName = fName.replace("%29", ")");
            fName = fName.replace("%21", "!");
            fName = fName.replace("%40", "@");
            fName = fName.replace("%23", "#");
            fName = fName.replace("%24", "$");
            fName = fName.replace("%25", "%");
            fName = fName.replace("%5E", "^");
            fName = fName.replace("%26", "&");
            fName = fName.replace("%3D", "=");
            fName = fName.replace("%2B", "+");
            fName = fName.replace("%3A", ":");
            fName = fName.replace("%3B", ";");
            fName = fName.replace("%22", "\"");
            fName = fName.replace("%27", "'");
            fName = fName.replace("%5C", "\\");
            fName = fName.replace("%2F", "/");
            fName = fName.replace("%3F", "?");
            fName = fName.replace("%3C", "<");
            fName = fName.replace("%3E", ">");
            fName = fName.replace("%5B", "[");
            fName = fName.replace("%5D", "]");
            fName = fName.replace("%7B", "{");
            fName = fName.replace("%7D", "}");
            fName = fName.replace("%60", "`");
        }

        if (agent != null && !agent.contains("firefox")) {
            contentDisposition += "filename=\"" + fName + "\"";
        } else {
            contentDisposition += "filename*=UTF-8''" + fName;
        }

//		if (agent != null && agent.indexOf("opera") == -1 && agent.indexOf("msie") != -1)
//			// IE
//			contentDisposition += "filename=\"" + toUTF8String(fileName + "." + fileExtension) + "\"";
//		else if (agent.indexOf("opera") != -1)
//			// Opera
//			contentDisposition += "filename=" + toUTF8String(fileName);
//		else
//			// Firefox and others
//			contentDisposition += "filename=\"" + MimeUtility.encodeText(fileName, "UTF8", "B") + "\"";
		return contentDisposition;
	}

    public static String restoreFreemarkerVariables(String template) {
        return template != null ? template.replace("#{","${") : null;
    }
}