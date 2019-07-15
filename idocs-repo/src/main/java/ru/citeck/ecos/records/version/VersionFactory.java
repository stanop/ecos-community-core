package ru.citeck.ecos.records.version;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.records.models.UserDTO;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.RecordsService;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Roman Makarskiy
 */
@Component
public class VersionFactory {

    private static final String DOWNLOAD_URL_TEMPLATE = "/proxy/alfresco/citeck/print/content?nodeRef=%s";

    private final NodeService nodeService;
    private final VersionService versionService;
    private final AuthorityService authorityService;
    private final RecordsService recordsService;

    @Autowired
    public VersionFactory(NodeService nodeService, VersionService versionService, AuthorityService authorityService,
                          RecordsService recordsService) {
        this.nodeService = nodeService;
        this.versionService = versionService;
        this.authorityService = authorityService;
        this.recordsService = recordsService;
    }

    public VersionDTO fromVersionRef(NodeRef versionRef) {
        Map<QName, Serializable> properties = nodeService.getProperties(versionRef);
        String uuid = (String) properties.get(ContentModel.PROP_NODE_UUID);
        String versionLabel = (String) properties.get(ContentModel.PROP_VERSION_LABEL);

        NodeRef docRef = new NodeRef(VersionRecordsConstants.SPACES_STORE_PREFIX + uuid);

        Version version = versionService.getVersionHistory(docRef).getVersion(versionLabel);
        return fromVersion(version);
    }

    public VersionDTO fromVersion(Version version) {
        VersionDTO dto = new VersionDTO();

        dto.setVersion(version.getVersionLabel());
        dto.setComment(version.getDescription());
        dto.setId(version.getFrozenStateNodeRef().toString());
        dto.setDownloadUrl(generateDownloadUrl(version.getFrozenStateNodeRef().toString()));
        dto.setModified(version.getFrozenModifiedDate());

        String frozenModifier = version.getFrozenModifier();
        UserDTO modifier = recordsService.getMeta(RecordRef.create("",
                authorityService.getAuthorityNodeRef(frozenModifier).toString()), UserDTO.class);
        dto.setModifier(modifier);

        return dto;
    }

    private String generateDownloadUrl(String nodeRef) {
        return String.format(DOWNLOAD_URL_TEMPLATE, nodeRef);
    }

}
