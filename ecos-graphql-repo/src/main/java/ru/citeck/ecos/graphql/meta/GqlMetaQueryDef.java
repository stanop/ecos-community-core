package ru.citeck.ecos.graphql.meta;

import graphql.schema.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GqlTypeDefinition;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.graphql.meta.value.MetaValueTypeDef;

import java.util.List;

@Component
public class GqlMetaQueryDef implements GqlTypeDefinition {

    private MetaValueTypeDef metaValueTypeDef;

    @Override
    public GraphQLObjectType getType() {
        return GraphQLObjectType.newObject()
                .name(GraphQLService.QUERY_TYPE)
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("gqlMeta")
                        .dataFetcher(this::values)
                        .type(GraphQLList.list(MetaValueTypeDef.typeRef()))
                        .build())
                .build();
    }

    private List<MetaValue> values(DataFetchingEnvironment env) {
        GqlContext context = env.getContext();
        return metaValueTypeDef.getAsMetaValues(context.getMetaValues(), context, true);
    }

    @Autowired
    public void setMetaValueTypeDef(MetaValueTypeDef metaValueTypeDef) {
        this.metaValueTypeDef = metaValueTypeDef;
    }
}
