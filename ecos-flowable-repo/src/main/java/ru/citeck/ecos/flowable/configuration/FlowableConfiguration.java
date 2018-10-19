package ru.citeck.ecos.flowable.configuration;

import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.service.ServiceDescriptorRegistry;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.workflow.DefaultWorkflowPropertyHandler;
import org.alfresco.repo.workflow.WorkflowObjectFactory;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.flowable.engine.*;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.parse.BpmnParseHandler;
import org.flowable.variable.api.types.VariableType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.citeck.ecos.flowable.constants.FlowableConstants;
import ru.citeck.ecos.flowable.converters.FlowableNodeConverter;
import ru.citeck.ecos.flowable.handlers.ProcessBpmnParseHandler;
import ru.citeck.ecos.flowable.handlers.UserTaskBpmnParseHandler;
import ru.citeck.ecos.flowable.services.FlowableTaskTypeManager;
import ru.citeck.ecos.flowable.services.impl.FlowableTaskTypeManagerImpl;
import ru.citeck.ecos.flowable.services.impl.ModelMapper;
import ru.citeck.ecos.flowable.utils.FlowableWorkflowPropertyHandlerRegistry;
import ru.citeck.ecos.flowable.variable.FlowableEcosPojoTypeHandler;
import ru.citeck.ecos.workflow.variable.handler.EcosPojoTypeHandler;
import ru.citeck.ecos.icase.CaseStatusServiceJS;
import ru.citeck.ecos.icase.completeness.CaseCompletenessServiceJS;

import javax.sql.DataSource;
import java.util.*;

/**
 * Flowable configuration
 */
@Configuration
public class FlowableConfiguration {

    private static final Logger logger = Logger.getLogger(FlowableConfiguration.class);

    /**
     * Constants
     */
    private static final String FLOWABLE_ENGINE_NAME = "flowable";

    /**
     * Properties constants
     */
    private static final String FLOWABLE_DB_URL = "flowable.db.url";
    private static final String FLOWABLE_DB_USERNAME = "flowable.db.username";
    private static final String FLOWABLE_DB_PASSWORD = "flowable.db.password";
    private static final String FLOWABLE_DRIVER_CLASS_NAME = "flowable.db.driver.class.name";
    private static final String FLOWABLE_DBCP_MIN_IDLE = "flowable.db.dbcp.min.idle";
    private static final String FLOWABLE_DBCP_MAX_IDLE = "flowable.db.dbcp.max.idle";
    private static final String FLOWABLE_DBCP_MAX_ACTIVE = "flowable.db.dbcp.max.active";
    private static final String FLOWABLE_DBCP_MAX_OPEN_PREPARED_STATEMENTS = "flowable.db.dbcp.max.open.prepared.statements";


    /**
     * Mail properties constants
     */
    private static final String FLOWABLE_MAIL_SERVER_HOST = "flowable.mail.server.host";
    private static final String FLOWABLE_MAIL_SERVER_PORT = "flowable.mail.server.port";
    private static final String FLOWABLE_MAIL_SERVER_USERNAME = "flowable.mail.server.username";
    private static final String FLOWABLE_MAIL_SERVER_PASSWORD = "flowable.mail.server.password";
    private static final String FLOWABLE_MAIL_SERVER_DEFAULT_FROM = "flowable.mail.server.default.from";
    private static final String FLOWABLE_MAIL_SERVER_USE_TLS = "flowable.mail.server.use.tls";
    private static final String FLOWABLE_MAIL_SERVER_USE_SSL = "flowable.mail.server.use.ssl";


    private static final String BEAN_KEY_COMPLETENESS_SERVICE_JS = "caseCompletenessServiceJS";
    private static final String BEAN_KEY_CASE_STATUS_SERVICE_JS = "caseStatusServiceJS";

    /**
     * Application context provider
     */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Global properties
     */
    @Autowired
    @Qualifier("global-properties")
    private Properties properties;

    /**
     * Flowable data source bean
     *
     * @return Flowable data source
     */
    @Bean(name = "flowableDataSource")
    public DataSource flowableDataSource() {

        String dbUrl = properties.getProperty(FLOWABLE_DB_URL);

        if (StringUtils.isBlank(dbUrl)) {
            return null;
        }

        String driverClassName = properties.getProperty(FLOWABLE_DRIVER_CLASS_NAME);
        String username = properties.getProperty(FLOWABLE_DB_USERNAME);
        String password = properties.getProperty(FLOWABLE_DB_PASSWORD);
        int minIdle = Integer.parseInt(properties.getProperty(FLOWABLE_DBCP_MIN_IDLE));
        int maxIdle = Integer.parseInt(properties.getProperty(FLOWABLE_DBCP_MAX_IDLE));
        int maxActive = Integer.parseInt(properties.getProperty(FLOWABLE_DBCP_MAX_ACTIVE));
        int maxOpenPreparedStatements = Integer.parseInt(properties.getProperty(
                FLOWABLE_DBCP_MAX_OPEN_PREPARED_STATEMENTS));

        String msg = "Connection to flowable data source with parameters:\n" +
                "dbUrl: " + dbUrl + "\n" +
                "driverClassName: " + driverClassName + "\n" +
                "username: " + username + "\n" +
                "minIdle: " + minIdle + "\n" +
                "maxIdle: " + maxIdle + "\n" +
                "maxActive: " + maxActive + "\n" +
                "maxOpenPreparedStatements: " + maxOpenPreparedStatements;
        logger.info(msg);

        try {
            BasicDataSource dataSource = new BasicDataSource();

            dataSource.setDriverClassName(driverClassName);
            dataSource.setUrl(dbUrl);
            dataSource.setUsername(username);
            dataSource.setPassword(password);

            dataSource.setMinIdle(minIdle);
            dataSource.setMaxIdle(maxIdle);
            dataSource.setMaxActive(maxActive);
            dataSource.setMaxOpenPreparedStatements(maxOpenPreparedStatements);

            return dataSource;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Flowable process engine configuration
     *
     * @param dataSource         Flowable data source
     * @param descriptorRegistry Service descriptor registry
     * @return Process engine configuration
     */
    @Bean(name = "flowableEngineConfiguration")
    public ProcessEngineConfiguration flowableEngineConfiguration(@Qualifier("flowableDataSource") DataSource dataSource,
                                                                  @Qualifier("workflow.variable.EcosPojoTypeHandler")
                                                                  EcosPojoTypeHandler<?> ecosPojoTypeHandler,
                                                                  ServiceDescriptorRegistry descriptorRegistry) {
        if (dataSource != null) {
            StandaloneProcessEngineConfiguration engineConfiguration = new StandaloneProcessEngineConfiguration();
            engineConfiguration.setDataSource(dataSource);
            engineConfiguration.setAsyncExecutorActivate(false);
            engineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

            Set<Class<?>> customMybatisMappers = new HashSet<>();
            customMybatisMappers.add(ModelMapper.class);
            engineConfiguration.setCustomMybatisMappers(customMybatisMappers);

            engineConfiguration.setBeans(getEngineBeans(descriptorRegistry));

            setMailConfiguration(engineConfiguration);
            // Listeners and handlers
            List<BpmnParseHandler> parseHandlers = new ArrayList<>(2);
            parseHandlers.add(new ProcessBpmnParseHandler());
            parseHandlers.add(new UserTaskBpmnParseHandler());
            engineConfiguration.setPreBpmnParseHandlers(parseHandlers);

            List<VariableType> types = engineConfiguration.getCustomPreVariableTypes();
            types = types != null ? new ArrayList<>(types) : new ArrayList<>();
            types.add(new FlowableEcosPojoTypeHandler(ecosPojoTypeHandler));
            engineConfiguration.setCustomPreVariableTypes(types);

            return engineConfiguration;
        } else {
            return null;
        }
    }

    private Map<Object, Object> getEngineBeans(ServiceDescriptorRegistry descriptorRegistry) {
        Map<Object, Object> beans = new HashMap<>();

        CaseCompletenessServiceJS caseCompletenessServiceJS = applicationContext.getBean(BEAN_KEY_COMPLETENESS_SERVICE_JS,
                CaseCompletenessServiceJS.class);
        CaseStatusServiceJS caseStatusServiceJS = applicationContext.getBean(BEAN_KEY_CASE_STATUS_SERVICE_JS,
                CaseStatusServiceJS.class);

        beans.put(FlowableConstants.SERVICE_REGISTRY_BEAN_KEY, descriptorRegistry);
        beans.put(FlowableConstants.COMPLETENESS_SERVICE_JS_KEY, caseCompletenessServiceJS);
        beans.put(FlowableConstants.CASE_STATUS_SERVICE_JS_KEY, caseStatusServiceJS);

        return beans;
    }

    /**
     * Set mail configuration
     *
     * @param processEngineConfiguration Process engine configuration
     */
    private void setMailConfiguration(StandaloneProcessEngineConfiguration processEngineConfiguration) {
        String mailHost = properties.getProperty(FLOWABLE_MAIL_SERVER_HOST);
        if (mailHost != null) {
            processEngineConfiguration.setMailServerHost(mailHost);
        }

        String mailPort = properties.getProperty(FLOWABLE_MAIL_SERVER_PORT);
        if (mailPort != null) {
            processEngineConfiguration.setMailServerPort(Integer.valueOf(mailPort));
        }

        String mailUsername = properties.getProperty(FLOWABLE_MAIL_SERVER_USERNAME);
        if (mailUsername != null) {
            processEngineConfiguration.setMailServerUsername(mailUsername);
        }

        String mailPassword = properties.getProperty(FLOWABLE_MAIL_SERVER_PASSWORD);
        if (mailPassword != null) {
            processEngineConfiguration.setMailServerPassword(mailPassword);
        }

        String mailDefaultFrom = properties.getProperty(FLOWABLE_MAIL_SERVER_DEFAULT_FROM);
        if (mailDefaultFrom != null) {
            processEngineConfiguration.setMailServerDefaultFrom(mailDefaultFrom);
        }

        String mailUseTLS = properties.getProperty(FLOWABLE_MAIL_SERVER_USE_TLS);
        if (mailUseTLS != null) {
            processEngineConfiguration.setMailServerUseTLS(Boolean.valueOf(mailUseTLS));
        }
        String mailUseSSL = properties.getProperty(FLOWABLE_MAIL_SERVER_USE_SSL);
        if (mailUseSSL != null) {
            processEngineConfiguration.setMailServerUseSSL(Boolean.valueOf(mailUseSSL));
        }
    }

    /**
     * Flowable engine bean
     *
     * @param flowableEngineConfiguration Flowable engine configuration
     * @return Flowable engine
     */
    @Bean(name = "flowableEngine")
    public ProcessEngine flowableEngine(ProcessEngineConfiguration flowableEngineConfiguration) {
        if (flowableEngineConfiguration != null) {
            return flowableEngineConfiguration.buildProcessEngine();
        } else {
            return null;
        }

    }

    /**
     * Flowable service bean
     *
     * @param processEngine Process engine
     * @return service
     */
    @Bean(name = "flowableRepositoryService")
    public RepositoryService flowableRepositoryService(ProcessEngine processEngine) {
        if (processEngine != null) {
            return processEngine.getRepositoryService();
        } else {
            return null;
        }
    }

    /**
     * Flowable runtime service bean
     *
     * @param processEngine Process engine
     * @return Runtime service
     */
    @Bean(name = "flowableRuntimeService")
    public RuntimeService flowableRuntimeService(ProcessEngine processEngine) {
        if (processEngine != null) {
            return processEngine.getRuntimeService();
        } else {
            return null;
        }
    }

    /**
     * Flowable task service bean
     *
     * @param processEngine Process engine
     * @return Task service
     */
    @Bean(name = "flowableTaskService")
    public TaskService flowableTaskService(ProcessEngine processEngine) {
        if (processEngine != null) {
            return processEngine.getTaskService();
        } else {
            return null;
        }
    }

    /**
     * Flowable history service bean
     *
     * @param processEngine Process engine
     * @return History service
     */
    @Bean(name = "flowableHistoryService")
    public HistoryService flowableHistoryService(ProcessEngine processEngine) {
        if (processEngine != null) {
            return processEngine.getHistoryService();
        } else {
            return null;
        }
    }

    /**
     * Flowable management service bean
     *
     * @param processEngine Process engine
     * @return Management service
     */
    @Bean(name = "flowableManagementService")
    public ManagementService flowableManagementService(ProcessEngine processEngine) {
        if (processEngine != null) {
            return processEngine.getManagementService();
        } else {
            return null;
        }
    }

    /**
     * Flowable form service bean
     *
     * @param processEngine Process engine
     * @return Form service
     */
    @Bean(name = "flowableFormService")
    public FormService flowableFormService(ProcessEngine processEngine) {
        if (processEngine != null) {
            return processEngine.getFormService();
        } else {
            return null;
        }
    }

    /**
     * Flowable node converter
     *
     * @param descriptorRegistry Service registry
     * @return Flowable node converter
     */
    @Bean(name = "flowableNodeConverter")
    public FlowableNodeConverter flowableNodeConverter(ServiceDescriptorRegistry descriptorRegistry) {
        return new FlowableNodeConverter(descriptorRegistry);
    }

    /**
     * Flowable workflow property handler registry bean
     *
     * @param flowableNodeConverter Flowable node converter
     * @param messageService        Message service
     * @param namespaceService      Namespace service
     * @return Flowable workflow property handler registry
     */
    @Bean(name = "flowableWorkflowPropertyHandlerRegistry")
    public FlowableWorkflowPropertyHandlerRegistry flowableWorkflowPropertyHandlerRegistry(
            FlowableNodeConverter flowableNodeConverter,
            MessageService messageService,
            NamespaceService namespaceService) {
        /** Qname converter */
        WorkflowQNameConverter workflowQNameConverter = new WorkflowQNameConverter(namespaceService);
        /** Default workflow property handler */
        DefaultWorkflowPropertyHandler defaultWorkflowPropertyHandler = new DefaultWorkflowPropertyHandler();
        defaultWorkflowPropertyHandler.setMessageService(messageService);
        defaultWorkflowPropertyHandler.setNodeConverter(flowableNodeConverter);
        /** Workflow property register handler */
        return new FlowableWorkflowPropertyHandlerRegistry(defaultWorkflowPropertyHandler, workflowQNameConverter);
    }

    /**
     * Flowable task type manager bean
     *
     * @param formService       Form service
     * @param namespaceService  Namespace service
     * @param tenantService     Tenant service
     * @param messageService    Message service
     * @param dictionaryService Dictionary service
     * @return Flowable task type manager
     */
    @Bean(name = "flowableTaskTypeManager")
    public FlowableTaskTypeManager flowableTaskTypeManager(FormService formService,
                                                           NamespaceService namespaceService,
                                                           TenantService tenantService,
                                                           MessageService messageService,
                                                           DictionaryService dictionaryService
    ) {
        // QName converter
        WorkflowQNameConverter workflowQNameConverter = new WorkflowQNameConverter(namespaceService);
        WorkflowObjectFactory workflowObjectFactory = new WorkflowObjectFactory(workflowQNameConverter,
                tenantService, messageService, dictionaryService, FLOWABLE_ENGINE_NAME, null);
        return new FlowableTaskTypeManagerImpl(workflowObjectFactory, formService);
    }

}
