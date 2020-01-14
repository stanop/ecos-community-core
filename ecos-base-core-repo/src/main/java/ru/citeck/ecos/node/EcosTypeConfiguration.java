package ru.citeck.ecos.node;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import ru.citeck.ecos.records2.RecordRef;

import javax.annotation.PostConstruct;

@Configuration
public class EcosTypeConfiguration {

    @Autowired
    private EcosTypeService ecosTypeService;

    @PostConstruct
    public void init() {
        ecosTypeService.register(ContentModel.TYPE_CMOBJECT, ecosTypeService::evalDefaultEcosType);
    }

}
