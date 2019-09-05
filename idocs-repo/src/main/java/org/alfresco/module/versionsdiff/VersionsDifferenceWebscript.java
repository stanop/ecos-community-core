/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright (C) 2012 Marco Scapoli
 *
 * This file is part of Versions Difference Alfresco Plug-in.
 *
 *  Versions Difference Alfresco Plug-in is free software: you can redistribute
 *  it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Versions Difference Alfresco Plug-in is distributed in the hope
 *  that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Versions Difference Alfresco Plug-in.
 *  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Author  Marco Scapoli  <rianko@gmail.com>
 *  File    VersionsDifferenceWebscript.java
 **/

package org.alfresco.module.versionsdiff;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.version.VersionDifferenceUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class VersionsDifferenceWebscript extends DeclarativeWebScript {

    private static Log logger = LogFactory.getLog(VersionsDifferenceWebscript.class);

    private ServiceRegistry serviceRegistry;
    private VersionDifferenceUtils versionDifferenceUtils;

    // for Spring injection
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        if (null == req) {
            logger.error("VersionsDifferenceWebscript.java: The request URL is not well formatted");
            throw new WebScriptException("VersionsDifferenceWebscript.java: The request URL is not well formatted");
        } else {
            Map<String, Object> model = new HashMap<>();

            NodeRef selectVersRef = getArgsVersRef(req);
            NodeRef lastVersRef = getArgsNodeRef(req);

            LinkedList<String[]> diffObjList = versionDifferenceUtils.getDiff(selectVersRef, lastVersRef);

            model.put("result", diffObjList);
            return model;
        }
    }

    private ContentService getContentService() {
        return this.serviceRegistry.getContentService();
    }

    private MimetypeService getMimetypeService() {
        return this.serviceRegistry.getMimetypeService();
    }

    private FileFolderService getFileFolderService() {
        return this.serviceRegistry.getFileFolderService();
    }

    /**
     * Get the nodeRef string args from url and put it in a NodeRef object
     *
     * @param req WebScriptRequest req for get the nodeRef string from URL querystring
     * @return a NodeRef to the passed URL args nodeRef
     */
    private NodeRef getArgsNodeRef(WebScriptRequest req) {
        if (null == req) {
            logger.error("Parameter req in WebScriptRequest cannot be of type null");
            throw new WebScriptException("Parameter req in WebScriptRequest cannot be of type null");
        } else {
            String nodeRefStr = req.getParameter("nodeRef");
            if (StringUtils.isBlank(nodeRefStr)) {
                throw new WebScriptException("URL args nodeRef cannot be blank");
            } else {
                return new NodeRef(nodeRefStr);
            }
        }
    }

    /**
     * Get the versRef string args from url and put it in a NodeRef object
     *
     * @param req WebScriptRequest req for get the versRef string from URL querystring
     * @return a NodeRef to the passed URL args versRef
     */
    private NodeRef getArgsVersRef(WebScriptRequest req) {
        if (null == req) {
            logger.error("Parameter req in WebScriptRequest cannot be of type null");
            throw new WebScriptException("Parameter req in WebScriptRequest cannot be of type null");
        } else {
            String versRefStr = req.getParameter("versRef");
            if (StringUtils.isBlank(versRefStr)) {
                throw new WebScriptException("URL args nodeRef cannot be blank");
            } else {
                return new NodeRef(versRefStr);
            }
        }
    }

    /**
     * Get the Filename of a passed NodeRef
     *
     * @param nodeRef of the Filename to retrieve
     * @return a String containing the NodeRef Filename
     */
    protected String getFilename(NodeRef nodeRef) {
        if (null == nodeRef) {
            throw new WebScriptException("URL args nodeRef cannot be null");
        }
        return getFileFolderService().getFileInfo(nodeRef).getName();
    }

    /**
     * Get the Mimetype of a passed NodeRef
     *
     * @param nodeRef of the Mimetype to retrieve
     * @return a String containing the NodeRef Mimetype
     */
    protected String guessMimetype(NodeRef nodeRef) {
        if (null == nodeRef) {
            throw new WebScriptException("URL args nodeRef cannot be null");
        }
        String filename = getFilename(nodeRef);
        return getMimetypeService().guessMimetype(filename);
    }

    @Autowired
    public void setVersionDifferenceUtils(VersionDifferenceUtils versionDifferenceUtils) {
        this.versionDifferenceUtils = versionDifferenceUtils;
    }
}