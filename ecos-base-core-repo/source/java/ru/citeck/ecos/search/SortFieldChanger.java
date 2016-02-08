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
package ru.citeck.ecos.search;

import java.util.Map;

/**
 * @author Alexander Nemerov <alexander.nemerov@citeck.ru>
 * date: 08.05.14
 */
public class SortFieldChanger {

    private Map<String, String> changeFieldMap;

    public String getSortField(String userSortField) {
        if(changeFieldMap.containsKey(userSortField)) {
            return changeFieldMap.get(userSortField);
        } else {
            return userSortField;
        }
    }

    public void setChangeFieldMap(Map<String, String> changeFieldMap) {
        this.changeFieldMap = changeFieldMap;
    }
}
