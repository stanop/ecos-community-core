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
 * Search criteria triplet - field, predicate, value.
 *
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class CriteriaTriplet {

    private final String field;

    private final String predicate;

    private final String value;

    public CriteriaTriplet(String field, String predicate, String value) {
        this.field = field;
        this.predicate = predicate;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public String getPredicate() {
        return predicate;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CriteriaTriplet)) return false;

        CriteriaTriplet that = (CriteriaTriplet) o;

        if (!field.equals(that.field)) return false;
        if (!predicate.equals(that.predicate)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        int result = field.hashCode();
        result = 31 * result + predicate.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}
