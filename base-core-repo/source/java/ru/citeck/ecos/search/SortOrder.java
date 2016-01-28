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

/**
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public enum SortOrder {
    ASCENDING("asc"), DESCENDING("desc");

    private final String value;

    private SortOrder(String value) {
        this.value = value;
    }

    public static SortOrder forName(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Null enum argument");
        }
        for (SortOrder sortOrder : values()) {
            if (value.equalsIgnoreCase(sortOrder.getValue())) {
                return sortOrder;
            }
        }
        throw new IllegalArgumentException("Unknown enum argument");
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.equals(SortOrder.ASCENDING) ? "true" : "false";
    }

    public Boolean toBoolean() {
        return this.equals(SortOrder.ASCENDING);
    }
}
