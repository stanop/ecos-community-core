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

import ru.citeck.ecos.utils.SQLUtils;

import java.io.Closeable;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wraps PreparedStatement and provides named parameters to it.
 *
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class NamedPreparedStatement implements Closeable {

    private final PreparedStatement statement;

    private final Map<String, Integer> indexMap;

    public NamedPreparedStatement(Connection connection, String query) throws SQLException {
        indexMap = new HashMap<String, Integer>();
        String parsedQuery = parseQuery(query, indexMap);
        statement = connection.prepareStatement(parsedQuery);
    }

    private String parseQuery(String rawQuery, Map<String, Integer> indexMap) {
        StringBuffer parsedQuery = new StringBuffer();
        Pattern paramPattern = Pattern.compile("[:][\\S]*");
        Matcher matcher = paramPattern.matcher(rawQuery);
        int parameterIndex = 1;
        while (matcher.find()) {
            String parameterName = matcher.group().replace(":", "");
            indexMap.put(parameterName, parameterIndex++);
            matcher.appendReplacement(parsedQuery, "?");
        }
        matcher.appendTail(parsedQuery);
        return parsedQuery.toString();
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        return statement.getParameterMetaData();
    }

    public void setParameterValue(String name, String value) throws SQLException {
        if (!indexMap.containsKey(name)) {
            throw new IllegalArgumentException("No such parameter with name " + name);
        }
        Integer index = indexMap.get(name);
        int type = statement.getParameterMetaData().getParameterType(index);
        switch (type) {
            case Types.VARCHAR:
            case Types.CHAR:
                statement.setString(index, value);
                break;
            case Types.DOUBLE:
                statement.setDouble(index, Double.parseDouble(value));
                break;
            case Types.FLOAT:
                statement.setFloat(index, Float.parseFloat(value));
                break;
            case Types.DATE:
                statement.setDate(index, Date.valueOf(value));
                break;
            case Types.TIMESTAMP:
                statement.setTimestamp(index, Timestamp.valueOf(value));
                break;
            case Types.INTEGER:
                statement.setInt(index, Integer.parseInt(value));
                break;
            case Types.BOOLEAN:
                statement.setBoolean(index, Boolean.parseBoolean(value));
                break;
            case Types.BIGINT:
                statement.setLong(index, Long.parseLong(value));
                break;
            default:
                throw new IllegalArgumentException("Unsupported parameter type - " + type);
        }
    }

    public ResultSet executeQuery() throws SQLException {
        return statement.executeQuery();
    }

    @Override
    public void close() {
        SQLUtils.closeQuietly(statement);
    }
}
