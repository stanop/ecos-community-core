package ru.citeck.ecos.icase.completeness.resolver;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.icase.completeness.CaseCompletenessService;
import ru.citeck.ecos.icase.completeness.dto.CaseDocumentDto;
import ru.citeck.ecos.journals.CreateVariant;
import ru.citeck.ecos.journals.variants.resolver.CreateVariantsResolver;
import ru.citeck.ecos.records.RecordConstants;
import ru.citeck.ecos.records2.RecordRef;

import java.util.*;

@Component
public class CaseDocumentsCreateVariantsResolver implements CreateVariantsResolver<CaseDocumentDto> {

    private static final String WORKSPACE_PREFIX = "workspace://SpacesStore/";
    private static final String SLASH_DELIMITER = "/";
    private static final String ICASE_DOCUMENTS_ATTRIBUTE = "icase:documents";

    private final CaseCompletenessService caseCompletenessService;
    private final NodeService nodeService;

    @Autowired
    public CaseDocumentsCreateVariantsResolver(CaseCompletenessService caseCompletenessService,
                                               NodeService nodeService) {
        this.caseCompletenessService = caseCompletenessService;
        this.nodeService = nodeService;
    }

    @Override
    public Class<CaseDocumentDto> getConfigType() {
        return CaseDocumentDto.class;
    }

    @Override
    public String getId() {
        return CaseDocumentsCreateVariantsResolver.class.getSimpleName();
    }

    @Override
    public List<CreateVariant> getCreateVariants(RecordRef recordRef, CaseDocumentDto config) {

        List<CreateVariant> resultVariants = new ArrayList<>();

        NodeRef nodeRef = new NodeRef(recordRef.getId());

        Set<CaseDocumentDto> documentDtos = caseCompletenessService.getCaseDocuments(nodeRef);

        for (CaseDocumentDto documentDto : documentDtos) {
            String documentType = documentDto.getType();
            CreateVariant createVariant = new CreateVariant();

            String title = convertTypeToTitle(documentType);
            createVariant.setTitle(title);

            Map<String, String> attributes = new HashMap<>();
            attributes.put(RecordConstants.ATT_PARENT, recordRef.toString());
            attributes.put(RecordConstants.ATT_PARENT_ATT, ICASE_DOCUMENTS_ATTRIBUTE);
            attributes.put(RecordConstants.ATT_TK_TYPE, convertTypeToTKType(documentType));
            attributes.put(RecordConstants.ATT_TK_KIND, convertTypeToTKKind(documentType));
            createVariant.setAttributes(attributes);

            resultVariants.add(createVariant);
        }

        return resultVariants;
    }

    private String convertTypeToTKType(String type) {
        if (type.contains(SLASH_DELIMITER)) {
            type = WORKSPACE_PREFIX + type.split("/")[0];
        } else {
            type = WORKSPACE_PREFIX + type;
        }
        return type;
    }

    private String convertTypeToTKKind(String type) {
        if (type.contains(SLASH_DELIMITER)) {
            String[] strings = type.split(SLASH_DELIMITER);
            type = WORKSPACE_PREFIX + type.replace(strings[0] + SLASH_DELIMITER, "");
        } else {
            type = WORKSPACE_PREFIX + "null";
        }
        return type;
    }

    private String convertTypeToTitle(String type) {
        String nodeRefStr;
        if (type.contains(SLASH_DELIMITER)) {
            String[] strings = type.split(SLASH_DELIMITER);
            nodeRefStr = WORKSPACE_PREFIX + strings[strings.length - 1];
        } else {
            nodeRefStr = WORKSPACE_PREFIX + type;
        }
        MLPropertyInterceptor.setMLAware(true);
        return (String) nodeService.getProperty(new NodeRef(nodeRefStr), ContentModel.PROP_TITLE);
    }
}
