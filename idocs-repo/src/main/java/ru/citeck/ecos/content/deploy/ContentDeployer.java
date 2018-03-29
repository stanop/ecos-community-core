package ru.citeck.ecos.content.deploy;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.content.ContentData;
import ru.citeck.ecos.content.RepoContentDAO;
import ru.citeck.ecos.content.metadata.MetadataExtractor;
import ru.citeck.ecos.model.EcosContentModel;
import ru.citeck.ecos.utils.AbstractDeployerBean;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;

public class ContentDeployer<T> extends AbstractDeployerBean {

    private static final Log logger = LogFactory.getLog(ContentDeployer.class);

    private RetryingTransactionHelper txnHelper;

    private Set<QName> metadataKeys = Collections.singleton(ContentModel.PROP_NAME);
    private RepoContentDAO<T> repoContentDAO;

    private NodeService nodeService;
    private ContentService contentService;
    private VersionService versionService;
    private MimetypeService mimetypeService;

    private MetadataExtractor<T> metadataExtractor;

    @PostConstruct
    public void init() {
        ParameterCheck.mandatory("repoContentDAO", repoContentDAO);
        if (metadataExtractor == null) {
            metadataExtractor = new MetadataExtractor<T>() {};
        }
    }

    @Override
    protected void load(String location, InputStream inputStream) {
        AuthenticationUtil.runAsSystem(() ->
            txnHelper.doInTransaction(() -> {
                deployImpl(location, inputStream);
                return null;
            }, false)
        );
    }

    private void deployImpl(String location, InputStream inputStream) throws Exception {

        byte[] inputBytes = IOUtils.toByteArray(inputStream);
        ByteArrayInputStream inputBytesStream = new ByteArrayInputStream(inputBytes);

        T parsedObject = repoContentDAO.getContentDAO().read(inputBytes);

        Map<QName, Serializable> metadata = new HashMap<>(metadataExtractor.getMetadata(parsedObject));

        if (!metadata.containsKey(ContentModel.PROP_NAME)) {
            metadata.put(ContentModel.PROP_NAME, FilenameUtils.getName(location));
        }

        Map<QName, Serializable> keys = new HashMap<>();
        metadataKeys.forEach(keyName -> {
            Serializable value = metadata.get(keyName);
            if (value instanceof NodeRef) {
                if (nodeService.exists((NodeRef) value)) {
                    keys.put(keyName, value);
                }
            } else {
                keys.put(keyName, value);
            }
        });
        if (keys.isEmpty() || keys.values().stream().allMatch(Objects::isNull)) {
            logger.warn("Content keys is empty. Ignore it. File: " + location);
            return;
        }

        Optional<? extends ContentData<?>> data = repoContentDAO.getFirstContentData(keys, false);
        NodeRef contentNode = data.map(ContentData::getNodeRef)
                                  .orElseGet(() -> repoContentDAO.createNode(metadata));

        String deployedChecksum = (String) nodeService.getProperty(contentNode,
                                                                   EcosContentModel.PROP_DEPLOYED_CHECKSUM);

        String checksum = DigestUtils.md5Hex(inputBytes);

        if (deployedChecksum == null || !deployedChecksum.equals(checksum)) {

            Map<String, Serializable> versProps = Collections.singletonMap(VersionModel.PROP_VERSION_TYPE,
                                                                           VersionType.MAJOR);

            versionService.createVersion(contentNode, versProps, false);

            inputBytesStream.reset();
            String mimetype = mimetypeService.guessMimetype(location, inputBytesStream);

            inputBytesStream.reset();
            ContentWriter writer = contentService.getWriter(contentNode, ContentModel.PROP_CONTENT, true);
            writer.setEncoding("UTF-8");
            writer.setMimetype(mimetype);
            writer.putContent(inputBytesStream);

            nodeService.setProperty(contentNode, EcosContentModel.PROP_DEPLOYED_CHECKSUM, checksum);
        }
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        txnHelper = serviceRegistry.getRetryingTransactionHelper();
        contentService = serviceRegistry.getContentService();
        nodeService = serviceRegistry.getNodeService();
        versionService = serviceRegistry.getVersionService();
        mimetypeService = serviceRegistry.getMimetypeService();
    }

    public void setMetadataKeys(Set<QName> metadataKeys) {
        this.metadataKeys = metadataKeys;
    }

    public void setRepoContentDAO(RepoContentDAO<T> repoContentDAO) {
        this.repoContentDAO = repoContentDAO;
    }

    public void setMetadataExtractor(MetadataExtractor<T> metadataExtractor) {
        this.metadataExtractor = metadataExtractor;
    }
}
