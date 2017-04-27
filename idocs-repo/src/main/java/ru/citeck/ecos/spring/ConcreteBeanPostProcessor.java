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
package ru.citeck.ecos.spring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextResourcePatternResolver;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ConcreteBeanPostProcessor implements BeanPostProcessor, Ordered, ApplicationContextAware, ServletContextAware, InitializingBean {
    
    private static final Log logger = LogFactory.getLog(ConcreteBeanPostProcessor.class);
    
    private List<String> configLocations;
    private Map<String, List<BeanProcessor>> concreteProcessors = new HashMap<String, List<BeanProcessor>>();
    private int order = 0;

    private ApplicationContext applicationContext;
    private ServletContext servletContext;

    private static interface Converter {
        Object convert(Object bean, String beanName, BeanProcessor processor);
    }
    
    private static Converter beforeConverter = new Converter() {
        @Override
        public Object convert(Object bean, String beanName, BeanProcessor processor) {
            return processor.postProcessBeforeInitialization(bean);
        }
    };
    
    private static Converter afterConverter = new Converter() {
        @Override
        public Object convert(Object bean, String beanName, BeanProcessor processor) {
            return processor.postProcessAfterInitialization(bean);
        }
    };
    
    @Override
    public void afterPropertiesSet() throws Exception {
        Set<String> processorNames = readProcessorNames();
        logger.info("Found " + processorNames.size() + " bean processors");
        for(String processorName : processorNames) {
            BeanProcessor processor = applicationContext.getBean(processorName, BeanProcessor.class);
            String beanName = processor.getBeanName();
            List<BeanProcessor> processorsForName = concreteProcessors.get(beanName);
            if(processorsForName == null) {
                processorsForName = new LinkedList<BeanProcessor>();
                concreteProcessors.put(beanName, processorsForName);
            }
            processorsForName.add(processor);
        }
    }

    private Set<String> readProcessorNames() throws IOException {
        Set<String> processorNames = new LinkedHashSet<String>();
        ResourcePatternResolver resolver = new ServletContextResourcePatternResolver(servletContext);
        for(String configLocation : configLocations) {
            Resource[] configs = resolver.getResources(configLocation);
            for(Resource config : configs) {
                if(!config.exists()) {
                    continue;
                }
                File configFile = config.getFile();
                try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                    
                    for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                        String processorName = line.trim();
                        if(!processorName.isEmpty()) {
                            processorNames.add(line.trim());
                        }
                    }
                } catch(IOException e) {
                    logger.error("Could not read bean processors from file " + configFile, e);
                }
            }
        }
        return processorNames;
    }

    private Object process(Object bean, String beanName, Converter converter) {
        List<BeanProcessor> processors = concreteProcessors.get(beanName);
        if(processors != null) {
            for(BeanProcessor processor : processors) {
                if(processor.getSupportedBeanClass().isInstance(bean)) {
                    bean = converter.convert(bean, beanName, processor);
                } else {
                    logger.warn("Bean id=" + beanName + " appeared to be not of type " + processor.getSupportedBeanClass() + ", whereas " + processor + " was registered on it");
                }
            }
        }
        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return process(bean, beanName, beforeConverter);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return process(bean, beanName, afterConverter);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setConfigLocations(List<String> configLocations) {
        this.configLocations = configLocations;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

}
