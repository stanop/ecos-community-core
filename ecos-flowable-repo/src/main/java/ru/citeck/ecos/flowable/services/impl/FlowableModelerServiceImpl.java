package ru.citeck.ecos.flowable.services.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowDeployer;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.lang3.StringUtils;
import org.flowable.app.domain.editor.AbstractModel;
import org.flowable.app.domain.editor.Model;
import org.flowable.app.domain.editor.ModelHistory;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.engine.ManagementService;
import org.flowable.engine.common.impl.cmd.CustomSqlExecution;
import org.flowable.engine.impl.cmd.AbstractCustomSqlExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.CollectionUtils;
import ru.citeck.ecos.config.EcosConfigService;
import ru.citeck.ecos.flowable.FlowableIntegrationException;
import ru.citeck.ecos.flowable.services.FlowableModelerService;

import javax.sql.DataSource;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.*;

/**
 * @author Roman Makarskiy
 */
public class FlowableModelerServiceImpl implements FlowableModelerService {

    private static final Logger logger = LoggerFactory.getLogger(FlowableModelerServiceImpl.class);

    private static final String CHANGELOG_PREFIX = "ACT_DE_";
    private static final String MODELER_APP_DB_CHANCHELOG = "META-INF/liquibase/flowable-modeler-app-db-changelog.xml";
    private static final String ENGINE_NAME = "flowable";
    private static final String IMPORT_CONFIG_KEY = "flowable-process-import-to-modeler-already-executed";
    private static final String FLOWABLE_ADMIN = "admin";
    private static final int DEFAULT_VERSION = 1;

    @Autowired
    @Qualifier("NodeService")
    private NodeService nodeService;

    @Autowired
    @Qualifier("ecosConfigService")
    private EcosConfigService ecosConfigService;

    @Autowired
    @Qualifier("flowableDataSource")
    private DataSource dataSource;

    @Autowired
    private ContentService contentService;

    @Autowired
    private ApplicationContext applicationContext;

    private ManagementService managementService;

    private List<String> locations;

    @Override
    public void importProcessModel(NodeRef nodeRef) {
        if (!importIsPossible()) {
            throw new FlowableIntegrationException("Cannot import process to modeler, because integration is " +
                    "not initialised or model table is not exist");
        }

        if (nodeRef == null || !nodeService.exists(nodeRef)) {
            throw new IllegalArgumentException("NodeRef does not exist");
        }

        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        if (reader == null) {
            throw new IllegalArgumentException("NodeRef do not have content");
        }

        InputStream inputStream = reader.getContentInputStream();
        logger.info("Start import process to Flowable Modeler from " + nodeRef);
        importProcessModel(inputStream);
        logger.info("Import finished");
    }

    @Override
    public void importProcessModel(InputStream inputStream) {
        if (!importIsPossible()) {
            throw new FlowableIntegrationException("Cannot import process to modeler, because integration is " +
                    "not initialised or model table is not exist");
        }

        /** Read process definition from input stream */
        BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
        XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader;

        try {
            xmlStreamReader = xmlFactory.createXMLStreamReader(inputStream);
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Could not create XML streamReader.", e);
        }

        /** Parse process definition */
        BpmnModel bpmnModel = xmlConverter.convertToBpmnModel(xmlStreamReader);
        if (CollectionUtils.isEmpty(bpmnModel.getProcesses())) {
            throw new IllegalArgumentException("No process found in metadata");
        }

        if (bpmnModel.getLocationMap().size() == 0) {
            BpmnAutoLayout bpmnLayout = new BpmnAutoLayout(bpmnModel);
            bpmnLayout.execute();
        }

        BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
        ObjectNode modelNode = bpmnJsonConverter.convertToJson(bpmnModel);

        org.flowable.bpmn.model.Process process = bpmnModel.getMainProcess();
        String name = process.getId();
        if (StringUtils.isNotEmpty(process.getName())) {
            name = process.getName();
        }
        String description = process.getDocumentation();
        String key = process.getId();

        /** Fill dto */
        Model model = new Model();
        model.setId(UUID.randomUUID().toString());
        model.setName(name);
        model.setKey(key);
        model.setDescription(description);
        model.setCreated(new Date());
        model.setCreatedBy(FLOWABLE_ADMIN);
        model.setLastUpdated(new Date());
        model.setLastUpdatedBy(FLOWABLE_ADMIN);
        model.setVersion(DEFAULT_VERSION);
        model.setModelEditorJson(modelNode.toString());
        model.setModelType(AbstractModel.MODEL_TYPE_BPMN);

        /** Insert or update model in flowable-modeler */
        if (existProcessModel(key)) {
            updateProcessModelToNewVersion(model);
        } else {
            createProcessModel(model);
        }

        logger.info("Process imported.");
    }

    private void updateProcessModelToNewVersion(Model model) {
        Model lastVersion = getLastModelByModelKey(model.getKey());
        if (lastVersion != null) {
            model.setId(lastVersion.getId());
            updateProcessModel(model);
        }
    }

    private void createProcessModel(Model model) {
        CustomSqlExecution<ModelMapper, String> insertModelSqlExecution =
                new AbstractCustomSqlExecution<ModelMapper, String>(ModelMapper.class) {
                    @Override
                    public String execute(ModelMapper modelMapper) {
                        modelMapper.insertModel(model);
                        return null;
                    }
                };

        managementService.executeCustomSql(insertModelSqlExecution);
    }

    private void updateProcessModel(Model model) {
        CustomSqlExecution<ModelMapper, String> updateModelSqlExecution =
                new AbstractCustomSqlExecution<ModelMapper, String>(ModelMapper.class) {
                    @Override
                    public String execute(ModelMapper modelMapper) {
                        modelMapper.updateModel(model);
                        return null;
                    }
                };

        managementService.executeCustomSql(updateModelSqlExecution);
    }

    private Model getLastModelByModelKey(String modelKey) {
        CustomSqlExecution<ModelMapper, Model> sqlExecution =
                new AbstractCustomSqlExecution<ModelMapper, Model>(ModelMapper.class) {
                    @Override
                    public Model execute(ModelMapper modelMapper) {
                        return modelMapper.getLastProcessModelByModelKey(modelKey);
                    }
                };
        return managementService.executeCustomSql(sqlExecution);
    }

    private void createProcessHistoryModel(ModelHistory model) {
        CustomSqlExecution<ModelMapper, String> insertModelSqlExecution =
                new AbstractCustomSqlExecution<ModelMapper, String>(ModelMapper.class) {
                    @Override
                    public String execute(ModelMapper modelMapper) {
                        modelMapper.insertHistoryModel(model);
                        return null;
                    }
                };

        managementService.executeCustomSql(insertModelSqlExecution);
    }

    private List<Model> getProcessModelsByModelKey(String modelKey) {
        CustomSqlExecution<ModelMapper, List<Model>> sqlExecution =
                new AbstractCustomSqlExecution<ModelMapper, List<Model>>(ModelMapper.class) {
                    @Override
                    public List<Model> execute(ModelMapper modelMapper) {
                        return modelMapper.getProcessModelsByModelKey(modelKey);
                    }
                };
        return managementService.executeCustomSql(sqlExecution);
    }

    private boolean existProcessModel(String modelKey) {
        /** Load process models by model key */
        CustomSqlExecution<ModelMapper, Long> countExecution =
                new AbstractCustomSqlExecution<ModelMapper, Long>(ModelMapper.class) {
                    @Override
                    public Long execute(ModelMapper modelMapper) {
                        return modelMapper.getProcessModelsCountByModelKey(modelKey);
                    }
                };
       Long result = managementService.executeCustomSql(countExecution);
       return result > 0;
    }

    private Integer getLastProcessModelVersion(String modelKey) {
        CustomSqlExecution<ModelMapper, Integer> versionExecution =
                new AbstractCustomSqlExecution<ModelMapper, Integer>(ModelMapper.class) {
                    @Override
                    public Integer execute(ModelMapper modelMapper) {
                        return modelMapper.getLastProcessModelVersionByModelKey(modelKey);
                    }
                };
        return managementService.executeCustomSql(versionExecution);
    }

    private void deleteProcessModelsByIds(String modelId) {
        CustomSqlExecution<ModelMapper, String> deleteModelSqlExecution =
                new AbstractCustomSqlExecution<ModelMapper, String>(ModelMapper.class) {
                    @Override
                    public String execute(ModelMapper modelMapper) {
                        modelMapper.deleteProcessModelsById(modelId);
                        return null;
                    }
                };

        managementService.executeCustomSql(deleteModelSqlExecution);
    }

    private ModelHistory createHistoryModelFromModel(Model model, String modelId) {
        ModelHistory result = new ModelHistory();
        result.setModelId(modelId);

        result.setId(UUID.randomUUID().toString());
        result.setName(model.getName());
        result.setKey(model.getKey());
        result.setDescription(model.getDescription());
        result.setCreated(model.getCreated());
        result.setCreatedBy(model.getCreatedBy());
        result.setLastUpdated(model.getLastUpdated());
        result.setLastUpdatedBy(model.getLastUpdatedBy());
        result.setVersion(model.getVersion());
        result.setModelEditorJson(model.getModelEditorJson());
        result.setModelType(model.getModelType());

        return result;
    }

    @Override
    public void importProcessModel() {
        if (!importIsPossible()) {
            throw new FlowableIntegrationException("Cannot import process to modeler, because integration is " +
                    "not initialised or model table is not exist");
        }

        logger.info("Start import process to Flowable Modeler from locations");

        /** Get all locations */
        List<String> bootstrapLocations = getBootstrapDefinitions();
        Set<String> allLocations = new HashSet<>(bootstrapLocations);
        allLocations.addAll(locations);

        if (CollectionUtils.isEmpty(allLocations)) {
            logger.info("Nothing import to Flowable Modeler");
            return;
        }

        /** Import process definitions */
        allLocations.forEach(location -> {
            try {
                Resource resource = location.contains(":") ? new UrlResource(location) : new ClassPathResource(location);
                String path = resource.getURL().toString();
                logger.info("Import: " + path);

                importProcessModel(resource.getInputStream());
                ecosConfigService.setValue(IMPORT_CONFIG_KEY, Boolean.TRUE.toString(), null);
            } catch (Exception e) {
                ecosConfigService.setValue(IMPORT_CONFIG_KEY, Boolean.FALSE.toString(), null);
                throw new IllegalStateException("Could not import process to Flowable Modeler. Location: " + location, e);
            }
        });
    }

    private List<String> getBootstrapDefinitions() {
        List<String> bootstrapLocations = new ArrayList<>();
        Map<String, WorkflowDeployer> deployerMap = applicationContext.getBeansOfType(WorkflowDeployer.class);
        for (String key : deployerMap.keySet()) {
            WorkflowDeployer deployer = deployerMap.get(key);
            if (deployer != null && !CollectionUtils.isEmpty(deployer.getWorkflowDefinitions())) {
                for (Properties definitionProperties : deployer.getWorkflowDefinitions()) {
                    String engineId = definitionProperties.getProperty("engineId");
                    if (ENGINE_NAME.equals(engineId)) {
                        String definitionLocation = definitionProperties.getProperty("location");
                        if (definitionLocation != null) {
                            boolean partOfLocations = false;
                            if (!CollectionUtils.isEmpty(locations)) {
                                for (String tempLocation : locations) {
                                    if (tempLocation.endsWith(definitionLocation)) {
                                        partOfLocations = true;
                                        break;
                                    }
                                }
                            }
                            if (!partOfLocations) {
                                bootstrapLocations.add(definitionLocation);
                            }
                        }
                    }
                }
            }
        }
        return bootstrapLocations;
    }

    public boolean importIsPossible() {
        return this.managementService != null && checkInitModelTable();
    }

    private boolean checkInitModelTable() {
        if (modelTableIsExist()) {
            return true;
        }
        initModelerChangeLog();
        return modelTableIsExist();
    }

    private boolean modelTableIsExist() {
        CustomSqlExecution<ModelMapper, String> sqlExecution =
                new AbstractCustomSqlExecution<ModelMapper, String>(ModelMapper.class) {
                    @Override
                    public String execute(ModelMapper modelMapper) {
                        return modelMapper.getTableSchema();
                    }
                };

        String tableSchema = managementService.executeCustomSql(sqlExecution);

        return StringUtils.isNotBlank(tableSchema);
    }

    private void initModelerChangeLog() {
        DatabaseConnection connection = null;
        try {
            connection = new JdbcConnection(dataSource.getConnection());
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
            database.setDatabaseChangeLogTableName(CHANGELOG_PREFIX + database.getDatabaseChangeLogTableName());
            database.setDatabaseChangeLogLockTableName(CHANGELOG_PREFIX + database.getDatabaseChangeLogLockTableName());

            Liquibase liquibase = new Liquibase(MODELER_APP_DB_CHANCHELOG,
                    new ClassLoaderResourceAccessor(),
                    database);
            liquibase.update(ENGINE_NAME);
        } catch (Exception e) {
            throw new IllegalStateException("Error creating flowable modeler change log", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (DatabaseException e) {
                    logger.error("Failed close connection", e);
                }
            }
        }
    }

    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }
}
