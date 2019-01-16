package ru.citeck.ecos.graphql.meta.value.factory;

import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.meta.value.MetaValueTypeDef;

import javax.annotation.PostConstruct;

public abstract class AbstractMetaValueFactory<T> implements MetaValueFactory<T> {

    private MetaValueTypeDef typeDef;

    @PostConstruct
    public void register() {
        typeDef.register(this);
    }

    @Autowired
    public void setTypeDef(MetaValueTypeDef typeDef) {
        this.typeDef = typeDef;
    }
}
