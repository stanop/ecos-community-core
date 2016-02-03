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
package ru.citeck.ecos.surf.bootstrap;


import org.springframework.beans.factory.BeanNameAware;
import org.springframework.extensions.config.*;
import org.springframework.extensions.config.source.UrlConfigSource;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class ModuleShareConfigDeployer implements BeanNameAware, ConfigDeployer {

    private String beanName;

    protected ConfigService configService;
    protected List<ModuleShareConfig> moduleConfigs;

    /**
     * Method called by ConfigService when the configuration files
     * represented by this ConfigDeployer need to be initialised.
     */
    public List<ConfigDeployment> initConfig() {
        List<String> configs = getModuleConfigs();
        List<ConfigDeployment> deployed = null;
        if (configService != null && configs != null && configs.size() != 0) {
            UrlConfigSource configSource = new UrlConfigSource(configs);
            deployed = configService.appendConfig(configSource);
        }
        return deployed;
    }

    private List<String> getModuleConfigs() {
        List<String> configs = new LinkedList<String>();
        for (ModuleShareConfig moduleShareConfig : moduleConfigs) {
            configs.addAll(
                    moduleShareConfig.getConfigs()
            );
        }
        return configs;
    }

    public void register() {
        if (configService == null) {
            throw new ConfigException("Config service must be provided");
        }
        configService.addDeployer(this);
    }

    public String getSortKey() {
        return this.beanName;
    }

    public void setBeanName(String name) {
        this.beanName = name;
    }

    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    public void setModuleConfigs(List<ModuleShareConfig> moduleConfigs) {
        this.moduleConfigs = moduleConfigs;
    }
}
