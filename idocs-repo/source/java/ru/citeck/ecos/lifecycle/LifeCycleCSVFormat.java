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
package ru.citeck.ecos.lifecycle;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleAction;
import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleCondition;
import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleEvent;
import ru.citeck.ecos.lifecycle.LifeCycleDefinition.LifeCycleTransition;

public class LifeCycleCSVFormat extends LifeCycleAbstractFormat {

    public static final String NAME = "csv";

    private static final CellProcessor[] CELL_PROCESSORS = new CellProcessor[] {
        new NotNull(), // fromState
        new NotNull(), // event
        new Optional(), // toState
        new Optional(), // transitionCondition
        new Optional() // action
    };

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public LifeCycleDefinition parseLifeCycleDefinition(InputStream lifeCycleDefinitionStream) throws IOException {

        InputStreamReader reader = null;

        try {
            reader = new InputStreamReader(lifeCycleDefinitionStream, "UTF-8");

            ICsvMapReader mapReader = null;

            LifeCycleDefinition result = new LifeCycleDefinition();

            try {
                mapReader = new CsvMapReader(
                        reader,
                        CsvPreference.STANDARD_PREFERENCE
                );

                final String[] headers = mapReader.getHeader(true);
                Map<String, Object> recordMap;

                while ((recordMap = mapReader.read(headers, CELL_PROCESSORS)) != null) {
                    LifeCycleTransition transition = new LifeCycleTransition();

                    if (recordMap.get(LifeCycleConstants.FROM_STATE) != null)
                    	transition.setFromState((String) recordMap.get(LifeCycleConstants.FROM_STATE));

                    if (recordMap.get(LifeCycleConstants.EVENT) != null) {
                    	LifeCycleEvent lcEvent = new LifeCycleEvent((String) recordMap.get(LifeCycleConstants.EVENT));
                    	transition.setEvent(lcEvent);
                    }

                    if (recordMap.get(LifeCycleConstants.TO_STATE) != null)
                    	transition.setToState((String) recordMap.get(LifeCycleConstants.TO_STATE));

                    if ((recordMap.get(LifeCycleConstants.TRANSITION_CONDITION) != null) &&
                    		(!((String) recordMap.get(LifeCycleConstants.TRANSITION_CONDITION)).isEmpty())) {
                        LifeCycleCondition lcCondition = new LifeCycleCondition(LifeCycleConstants.VAL_JAVASCRIPT);
                        lcCondition.setParam(LifeCycleConstants.VAL_CODE, (String) recordMap.get(LifeCycleConstants.TRANSITION_CONDITION));
                    	transition.getConditionList().add(lcCondition);
                    }

                    if ((recordMap.get(LifeCycleConstants.ACTION) != null) &&
                    		(!((String) recordMap.get(LifeCycleConstants.ACTION)).isEmpty())) {
                        LifeCycleAction lcAction = new LifeCycleAction(LifeCycleConstants.VAL_JAVASCRIPT);
                        lcAction.setParam(LifeCycleConstants.VAL_CODE, (String) recordMap.get(LifeCycleConstants.ACTION));
                    	transition.getActionList().add(lcAction);
                    }

                    result.getTransitionList().add(transition);
                }

            } finally {
                if (mapReader != null)
                    mapReader.close();
            }

            return result;
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    @Override
    public String serializeLifeCycleDefinition(LifeCycleDefinition lcd) {
        // TODO Auto-generated method stub
        return null;
    }
}
