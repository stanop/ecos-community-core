package ru.citeck.ecos.webscripts;

import org.springframework.extensions.surf.ModelObjectService;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.surf.types.Page;
import org.springframework.extensions.webscripts.*;

import java.util.HashMap;
import java.util.Map;

public class CardletGet extends DeclarativeWebScript {

    private ModelObjectService modelObjectService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Page page = modelObjectService.getPage("card-details");

        RequestContext context = ThreadLocalRequestContext.getRequestContext();

        context.setPage(page);
        context.getModel().put("url", new DefaultURLHelper(context));

        Map<String, Object> model = new HashMap<>();
        //model.put("url", new DefaultURLHelper(context));

        return model;
    }

    public void setModelObjectService(ModelObjectService modelObjectService) {
        this.modelObjectService = modelObjectService;
    }
}
