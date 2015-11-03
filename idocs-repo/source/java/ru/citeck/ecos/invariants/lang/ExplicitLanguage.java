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
package ru.citeck.ecos.invariants.lang;

import java.util.Map;

import ru.citeck.ecos.invariants.AbstractInvariantLanguage;

public class ExplicitLanguage extends AbstractInvariantLanguage {

    private static final String NAME = "explicit";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isValueSupported(Object value) {
        return true;
    }

    @Override
    public Object evaluate(Object expression, Map<String, Object> model) {
        return expression;
    }

    @Override
    public String toString() {
        return NAME + " invariant language";
    }

}
