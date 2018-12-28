package ru.citeck.ecos.records.attribute.accessor;

import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.records.attribute.RecordAttributes;

import javax.annotation.PostConstruct;

public abstract class AbstractAttAccessorProvider implements AccessorProvider {

    protected RecordAttributes recordAttributes;

    @PostConstruct
    void registerAccessor() {
        recordAttributes.register(this);
    }

    @Autowired
    public void setRecordAttributes(RecordAttributes recordAttributes) {
        this.recordAttributes = recordAttributes;
    }
}
