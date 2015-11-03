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
package ru.citeck.ecos.confirm;

import java.util.List;

/**
 * Author: alexander.nemerov
 * Date: 25.09.13
 */
public class ConfirmConfigService {

    private String recordSeparator;
    private String partSeparator;
    private String keyPart;
    private List<String> parts;
    private List<String> allowedDecisions;

    public String getRecordSeparator() {
        return recordSeparator;
    }

    public void setRecordSeparator(String recordSeparator) {
        this.recordSeparator = recordSeparator;
    }

    public String getPartSeparator() {
        return partSeparator;
    }

    public void setPartSeparator(String partSeparator) {
        this.partSeparator = partSeparator;
    }

    public String getKeyPart() {
        return keyPart;
    }

    public void setKeyPart(String keyPart) {
        this.keyPart = keyPart;
    }

    public List<String> getParts() {
        return parts;
    }

    public void setParts(List<String> parts) {
        this.parts = parts;
    }

    public List<String> getAllowedDecisions() {
        return allowedDecisions;
    }

    public void setAllowedDecisions(List<String> allowedDecisions) {
        this.allowedDecisions = allowedDecisions;
    }
}
