package ru.citeck.ecos.webscripts;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.extensions.surf.ModelObjectService;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.WebFrameworkConstants;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.surf.types.Theme;
import org.springframework.extensions.webscripts.*;

import java.util.HashMap;
import java.util.Map;

public class SurfRegionGet extends DeclarativeWebScript {

    private static final String ARG_PAGE_ID = "pageid";
    private static final String ARG_THEME = "theme";
    private static final String ARG_TEMPLATE_ID = "templateId";
    private static final String ARG_CACHE_AGE = "cacheAge";

    private ModelObjectService modelObjectService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        RequestContext context = ThreadLocalRequestContext.getRequestContext();
        context.setTheme(getTheme(req.getParameter(ARG_THEME)));

        Map<String, Object> model = new HashMap<>(context.getModel());
        model.put("url", new DefaultURLHelper(context));
        context.setModel(model);

        String pageId = req.getParameter(ARG_PAGE_ID);
        if (StringUtils.isNotBlank(pageId)) {
            context.setPage(modelObjectService.getPage(pageId));
        }

        String templateId = req.getParameter(ARG_TEMPLATE_ID);
        if (StringUtils.isNotBlank(templateId)) {
            context.setTemplate(modelObjectService.getTemplate(templateId));
        }

        long cacheAge = NumberUtils.toLong(req.getParameter(ARG_CACHE_AGE));
        if (cacheAge > 0) {
            cache.setMaxAge(cacheAge);
        } else {
            cache.setNeverCache(true);
        }

        return new HashMap<>();
    }

    private Theme getTheme(String themeId) {
        Theme result = null;
        if (themeId != null) {
            result = modelObjectService.getTheme(themeId);
        }
        if (result == null) {
            result = modelObjectService.getTheme(WebFrameworkConstants.DEFAULT_THEME_ID);
        }
        return result;
    }

    public void setModelObjectService(ModelObjectService modelObjectService) {
        this.modelObjectService = modelObjectService;
    }
}
