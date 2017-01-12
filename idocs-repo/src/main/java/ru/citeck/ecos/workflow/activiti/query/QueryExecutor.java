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
package ru.citeck.ecos.workflow.activiti.query;

import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.db.DbSqlSessionFactory;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandContextFactory;
import org.alfresco.repo.workflow.activiti.AlfrescoProcessEngineConfiguration;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.*;

/**
 * Executes query.
 *
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public class QueryExecutor {

    private AlfrescoProcessEngineConfiguration engineConfig;

    private Configuration configuration;

    private List<String> mappers;

    private List<QueryParameterConverter> converters;

    public void setProcessEngineConfiguration(AlfrescoProcessEngineConfiguration engineConfig) {
        this.engineConfig = engineConfig;
    }

    public void setMappers(List<String> mappers) {
        this.mappers = mappers;
    }

    public void setConverters(List<QueryParameterConverter> converters) {
        this.converters = converters;
    }

    public List<?> execute(String statement, Object query) {
        SqlSession session = getSqlSession();
        Map<String, Object> parameters = new HashMap<String, Object>();
        if (statementExists(statement)) {
            for (QueryParameterConverter converter : converters) {
                if (converter.canConvert(query)) {
                    parameters.putAll(converter.convert(query));
                }
            }
        }
        return session.selectList(statement, parameters);
    }

    private SqlSession getSqlSession() {
        CommandContextFactory contextFactory = engineConfig.getCommandContextFactory();
        CommandContext commandContext = contextFactory.createCommandContext(new TaskQueryImpl());
        DbSqlSessionFactory dbSqlSessionFactory = commandContext.getDbSqlSession().getDbSqlSessionFactory();
        SqlSessionFactory sqlSessionFactory = dbSqlSessionFactory.getSqlSessionFactory();
        SqlSession sqlSession = sqlSessionFactory.openSession();
        configureSession(sqlSession);

        return sqlSession;
    }

    private void configureSession(SqlSession sqlSession) {
        configuration = sqlSession.getConfiguration();
        for (String mapper : mappers) {
            try {
                Class<?> mapperClass = Class.forName(mapper);
                registerMapper(sqlSession, mapperClass);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Cannot find mapper class", e);
            }
        }
    }

    private void registerMapper(SqlSession sqlSession, Class<?> mapperClass) {
        try {
            configuration.getMapper(mapperClass, sqlSession);
        } catch (BindingException e) {
            configuration.addMapper(mapperClass);
        }
    }

    private boolean statementExists(String statement) {
        Collection<String> statementNames = configuration.getMappedStatementNames();
        return statementNames.contains(statement);
    }
}
