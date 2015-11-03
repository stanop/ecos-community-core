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
package ru.citeck.ecos.constraints.common;

import org.alfresco.repo.dictionary.constraint.AbstractConstraint;
import org.alfresco.service.cmr.dictionary.ConstraintException;

/**
 * @author: Alexander Nemerov
 * @date: 15.09.2014
 */
public class PassportConsentConstraint extends AbstractConstraint {

    private static final String ERR_ILLEGAL_VALUE = "idocs.constraints.passport.consent";

    @Override
    protected void evaluateSingleValue(Object value) {
        if(!value.equals(Boolean.TRUE)) {
            throw new ConstraintException(ERR_ILLEGAL_VALUE);
        }
    }

    @Override
    public String toString() {
        return "PassportConsentConstraint";
    }
}
