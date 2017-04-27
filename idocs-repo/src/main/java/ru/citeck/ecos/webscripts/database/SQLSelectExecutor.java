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
package ru.citeck.ecos.webscripts.database;

import org.alfresco.repo.content.MimetypeMap;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import ru.citeck.ecos.utils.SQLUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Executes select SQL queries on the specified data source.
 *
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class SQLSelectExecutor extends AbstractWebScript {

    private final Logger logger = Logger.getLogger(SQLSelectExecutor.class);

    public static final String RESULT = "result";

    private DataSource dataSource;

    private String queryTemplate;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setQueryTemplate(String queryTemplate) {
        this.queryTemplate = queryTemplate;
    }

    @Override
    public void execute(WebScriptRequest webScriptRequest, WebScriptResponse webScriptResponse) throws IOException {
        JSONObject json = executeQuery(getParametersMap(webScriptRequest));
        webScriptResponse.setContentType(MimetypeMap.MIMETYPE_JSON);
        webScriptResponse.setContentEncoding("UTF-8");
        webScriptResponse.getWriter().write(json.toString());
    }

    private Map<String, String> getParametersMap(WebScriptRequest request) {
        String[] names = request.getParameterNames();
        Map<String, String> parametersMap = new HashMap<String, String>();
        for (String name : names) {
            String value = request.getParameter(name);
            parametersMap.put(name, value);
        }
        return parametersMap;
    }

    public JSONObject executeQuery(Map<String, String> parameters) {
        Connection connection = null;
        NamedPreparedStatement query = null;
        ResultSet result = null;
        try {
            connection = dataSource.getConnection();
            query = new NamedPreparedStatement(connection, queryTemplate);
            formatQuery(query, parameters);
            result = query.executeQuery();
            JSONObject json = new JSONObject();
            json.put(RESULT, convertToJSON(result));
            return json;
        } catch (SQLException ex) {
            throw new WebScriptException("Unable to execute SQL: " + ex.getMessage());
        } catch (JSONException ex) {
            throw new WebScriptException("Unable to serialize JSON: " + ex.getMessage());
        } finally {
            SQLUtils.closeQuietly(result);
            SQLUtils.closeQuietly(query);
            SQLUtils.closeQuietly(connection);
        }
    }

    private void formatQuery(NamedPreparedStatement query, Map<String, String> parameters) throws SQLException {
        int queryParametersNumber = query.getParameterMetaData().getParameterCount();
        if (queryParametersNumber > 0) {
            for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                try {
                    query.setParameterValue(parameter.getKey(), parameter.getValue());
                } catch (IllegalArgumentException ex) {
                    logger.warn("Unknown parameter with name - " + parameter.getKey());
                }
            }
        }
    }

    private List<JSONObject> convertToJSON(ResultSet resultSet) throws SQLException, JSONException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        ArrayList<JSONObject> rows = new ArrayList<JSONObject>();
        while (resultSet.next()) {
            JSONObject row = new JSONObject();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                Object columnValue = resultSet.getObject(i);
                row.put(columnName, columnValue);
            }
            rows.add(row);
        }
        return rows;
    }
}
