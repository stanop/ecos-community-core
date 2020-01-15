package ru.citeck.ecos.node;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import ru.citeck.ecos.model.ClassificationModel;
import ru.citeck.ecos.model.ContractsModel;
import ru.citeck.ecos.records2.RecordRef;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Map;

@Configuration
public class EcosTypeSupplementaryAgreementConfiguration {

    @Autowired
    private EcosTypeService ecosTypeService;

    private final static String SLASH_DELIMITER = "/";
    private final static String SUPPLEMENTARY_AGREEMENT_TYPE = "supplementary-agreement";

    @PostConstruct
    public void init() {
        ecosTypeService.register(ContractsModel.CONTRACTS_SUPPLEMENTARY_TYPE, this::evalSupAgreementType);
    }

    private RecordRef evalSupAgreementType(AlfNodeInfo info) {

        Map<QName, Serializable> props = info.getProperties();

        NodeRef kind = (NodeRef) props.get(ClassificationModel.PROP_DOCUMENT_KIND);
        if (kind == null) {
            return RecordRef.create("emodel", "type", SUPPLEMENTARY_AGREEMENT_TYPE);
        }

        String ecosType = SUPPLEMENTARY_AGREEMENT_TYPE + SLASH_DELIMITER + kind.getId();

        return RecordRef.create("emodel", "type", ecosType);
    }

}
