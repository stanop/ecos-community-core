package ru.citeck.ecos.records.attributes.accessor;

import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.records.attributes.RecordAttributesDAO;

import javax.annotation.PostConstruct;

public abstract class AbstractAttAccessorProvider implements AccessorProvider {

    protected RecordAttributesDAO attributesDAO;

    @PostConstruct
    void registerAccessor() {
        attributesDAO.register(this);
    }

    @Autowired
    public void setAttributesDAO(RecordAttributesDAO attributesDAO) {
        this.attributesDAO = attributesDAO;
    }
}
