package ru.citeck.ecos.eapps;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.apps.app.provider.ComputedMeta;
import ru.citeck.ecos.apps.app.provider.ComputedModule;
import ru.citeck.ecos.records2.RecordRef;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EcosTypesMigration implements ModuleMigration {

    private NodeRef TYPES_ROOT = new NodeRef("workspace://SpacesStore/category-document-type-root");

    @Autowired
    private NodeService nodeService;

    @Override
    public List<ComputedModule> getModulesSince(long time) {

        if (time > 0) {
            return Collections.emptyList();
        }

        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
            TYPES_ROOT,
            ContentModel.ASSOC_SUBCATEGORIES,
            RegexQNamePattern.MATCH_ALL
        );

        List<TypeModule> typeModules = new ArrayList<>();

        childAssocs.stream().map(ChildAssociationRef::getChildRef).forEach(typeRef -> {

            typeModules.add(getTypeModule(typeRef, null));

            nodeService.getChildAssocs(
                typeRef,
                ContentModel.ASSOC_SUBCATEGORIES,
                RegexQNamePattern.MATCH_ALL
            ).stream().map(ChildAssociationRef::getChildRef).forEach(kindRef -> {

                typeModules.add(getTypeModule(kindRef, typeRef));

                nodeService.getChildAssocs(
                    kindRef,
                    ContentModel.ASSOC_SUBCATEGORIES,
                    RegexQNamePattern.MATCH_ALL
                ).stream().map(ChildAssociationRef::getChildRef).forEach(kind2Ref ->
                    typeModules.add(getTypeModule(kind2Ref, kindRef))
                );
            });
        });

        log.info("Types found to export: " + typeModules.size());

        return typeModules.stream().map(t ->
            new ComputedModule(t, new ComputedMeta(t.getId(), 1L))
        ).collect(Collectors.toList());
    }

    private TypeModule getTypeModule(NodeRef typeRef, NodeRef parentRef) {

        Map<QName, Serializable> props = getTypeProps(typeRef);

        MLText title = (MLText) props.get(ContentModel.PROP_TITLE);
        if (title == null || title.isEmpty()) {
            title = new MLText();
            String name = (String) props.get(ContentModel.PROP_NAME);
            if (StringUtils.isNotBlank(name)) {
                title.put(Locale.ENGLISH, name);
            } else {
                title.put(Locale.ENGLISH, typeRef.getId());
            }
        }

        ru.citeck.ecos.commons.data.MLText name = new ru.citeck.ecos.commons.data.MLText();
        title.forEach((locale, value) -> name.set(new Locale(locale.getLanguage()), value));

        TypeModule module = new TypeModule();
        module.setName(name);

        if (parentRef == null) {
            module.setId(typeRef.getId());
            module.setParent(RecordRef.create("emodel", "type", "user-base"));
        } else {
            module.setId(parentRef.getId() + "/" + typeRef.getId());
            module.setParent(RecordRef.create("emodel", "type", parentRef.getId()));
        }

        return module;
    }

    private Map<QName, Serializable> getTypeProps(NodeRef typeRef) {

        boolean isMlAvareBefore = MLPropertyInterceptor.isMLAware();
        MLPropertyInterceptor.setMLAware(true);

        Map<QName, Serializable> props;
        try {
            props = nodeService.getProperties(typeRef);
        } finally {
            MLPropertyInterceptor.setMLAware(isMlAvareBefore);
        }
        return props;
    }

    @Data
    public static class TypeModule {
        private String id;
        private ru.citeck.ecos.commons.data.MLText name;
        private RecordRef parent;
    }

    @Override
    public String getModuleType() {
        return "model/tk_type";
    }
}
