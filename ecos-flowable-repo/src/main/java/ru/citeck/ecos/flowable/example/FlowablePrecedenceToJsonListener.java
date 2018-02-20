package ru.citeck.ecos.flowable.example;

import org.flowable.engine.common.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Precedence to json execution listener
 */
public class FlowablePrecedenceToJsonListener extends AbstractExecutionListener {

    private Expression var;
    private Expression precedence;

    @Override
    protected void notifyImpl(DelegateExecution execution) {
        String variableName = (String) var.getValue(execution);
        String precedenceLine = (String) precedence.getValue(execution);
        execution.setVariable(variableName, convertPrecedence(precedenceLine));
    }

    public static JSONObject convertPrecedence(String precedence) {
        JSONObject result = new JSONObject();
        JSONArray stages = new JSONArray();
        result.put("stages", stages);
        if (precedence != null && precedence.length() != 0) {
            String[] stageLines = precedence.split("[,]");
            for (String line : stageLines) {
                JSONObject stage = new JSONObject();

                // due date is not supported in old format
                stage.put("dueDate", 0L);
                JSONArray confirmers = new JSONArray();
                stage.put("confirmers", confirmers);
                String[] confirmLines = line.split("[|]");
                for (String confirmer : confirmLines) {
                    if (confirmer.length() != 0) {
                        JSONObject conf = new JSONObject();
                        String[] splittedStrings = confirmer.split("_");
                        conf.put("nodeRef", splittedStrings[0]);
                        // full name is not supported in old format
                        conf.put("fullName", splittedStrings[0]);
                        // 'can cancel' is not supported in old format
                        conf.put("canCancel", false);
                        if (splittedStrings.length > 1) {
                            conf.put("amountHours", getNumberOfHoursForStage(splittedStrings[1]));
                        }
                        confirmers.add(conf);
                    }
                }
                stages.add(stage);
            }
        }
        return result;
    }

    private static double getNumberOfHoursForStage(String timeStage) {
        String time = timeStage.split("/")[0];
        String timeType = timeStage.split("/")[1];
        if ("m".equals(timeType)) {
            return (double)Math.round(Double.parseDouble(time) * 30 * 24 * 1000)/1000;
        } else if ("d".equals(timeType)) {
            return (double)Math.round(Double.parseDouble(time) * 24 * 1000)/1000;
        } else {
            return Double.parseDouble(time);
        }
    }
}
