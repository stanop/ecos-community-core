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
package ru.citeck.ecos.spring.aop;
import org.alfresco.util.PropertyCheck;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.springframework.beans.factory.InitializingBean;

public class LoggingInterceptor implements MethodInterceptor, InitializingBean {

    private Log logger = null;
    private Level enterLevel = Level.DEBUG;
    private Level exitLevel = Level.DEBUG;
    private Level exceptionLevel = Level.WARN;
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if(isLevelAvailable(logger, enterLevel)) {
            log(logger, enterLevel, "Entering " + invocation.getMethod());
        }
        try {
            Object result = invocation.proceed();
            if(isLevelAvailable(logger, exitLevel)) {
                log(logger, exitLevel, "Exiting " + invocation.getMethod() + " with result: " + result);
            }
            return result;
        } catch(Exception e) {
            if(isLevelAvailable(logger, exceptionLevel)) {
                log(logger, exceptionLevel, "Failed " + invocation.getMethod(), e);
            }
            throw e;
        } finally {
        }
    }
    
    private static boolean isLevelAvailable(Log logger, Level level) {
        switch(level.toInt()) {
        case Level.TRACE_INT: return logger.isTraceEnabled();
        case Level.DEBUG_INT: return logger.isDebugEnabled();
        case Level.INFO_INT : return logger.isInfoEnabled();
        case Level.WARN_INT : return logger.isWarnEnabled();
        case Level.ERROR_INT: return logger.isErrorEnabled();
        case Level.FATAL_INT: return logger.isFatalEnabled();
        case Level.ALL_INT: return true;
        case Level.OFF_INT: return false;
        default: return false;
        }
    }
    
    private static void log(Log logger, Level level, String message) {
        log(logger, level, message, null);
    }
    
    private static void log(Log logger, Level level, String message, Exception ex) {
        switch(level.toInt()) {
        case Level.TRACE_INT: logger.trace(message, ex); return;
        case Level.DEBUG_INT: logger.debug(message, ex); return;
        case Level.ALL_INT: 
        case Level.INFO_INT : logger.info (message, ex); return;
        case Level.WARN_INT : logger.warn (message, ex); return;
        case Level.ERROR_INT: logger.error(message, ex); return;
        case Level.FATAL_INT: logger.fatal(message, ex); return;
        case Level.OFF_INT: return;
        default: logger.warn("Unknown Level: " + level);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        PropertyCheck.mandatory(this, "logger", logger);
    }

    public void setLoggerName(String loggerName) {
        this.logger = LogFactory.getLog(loggerName);
    }

    public void setEnterLevel(String enterLevel) {
        this.enterLevel = Level.toLevel(enterLevel);
    }

    public void setExitLevel(String exitLevel) {
        this.exitLevel = Level.toLevel(exitLevel);
    }

    public void setExceptionLevel(String exceptionLevel) {
        this.exceptionLevel = Level.toLevel(exceptionLevel);
    }

}
