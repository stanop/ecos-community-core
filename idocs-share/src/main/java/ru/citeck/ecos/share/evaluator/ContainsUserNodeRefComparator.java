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
package ru.citeck.ecos.share.evaluator;

import org.alfresco.web.evaluator.Comparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;

/**
 * Compares: authorized user.nodeRef and given nodeRef
 * @author deathNC
 */
public class ContainsUserNodeRefComparator implements Comparator {

    private static final Log log = LogFactory.getLog(ContainsUserNodeRefComparator.class);

    @Override
    public boolean compare(Object argument) {
        if (argument == null) return false;
        try {
            String nodeRef = null;
            if (argument instanceof JSONArray) {
                JSONArray arr = (JSONArray) argument;
                nodeRef = (String)arr.get(0);
            } else {
                nodeRef = argument.toString();
            }
            if (nodeRef == null) return false;
            final RequestContext rc = ThreadLocalRequestContext.getRequestContext();
            String userNodeRef = rc.getUser().getProperty("nodeRef").toString().toLowerCase();
            return nodeRef.toLowerCase().contains(userNodeRef);
        } catch (Throwable throwable) {
            if (log.isErrorEnabled()) {
                log.error(throwable);
            }
            return false;
        }
    }

}