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

/**
 * Compares a node value against regex
 *
 * @author: Sergey Tiunov
 */
public class RegexComparator implements Comparator
{
    private Boolean caseInsensitive = true;
    private String value = null;

    /**
     * Setter for static string value to compare to
     *
     * @param value
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    @Override
    public boolean compare(Object nodeValue)
    {
        if (nodeValue == null)
        {
            return false;
        }
        
        return nodeValue.toString().matches(this.value);
    }
}
