package ru.citeck.ecos.share.template;

import freemarker.cache.TemplateCache;
import freemarker.cache.TemplateLoader;
import freemarker.core.Environment;
import freemarker.template.TemplateBooleanModel;
import org.apache.commons.io.FilenameUtils;
import org.springframework.extensions.webscripts.processor.BaseProcessorExtension;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Pavel Simonov
 */
public class TemplateUtils extends BaseProcessorExtension {

    private final Map<String, TemplateBooleanModel> templateExistsCache = new ConcurrentHashMap<>();

    public TemplateBooleanModel templateExists(String path) {

        TemplateBooleanModel result = templateExistsCache.get(path);

        if (result == null) {
            result = templateExistsImpl(path);
            templateExistsCache.put(path, result);
        }

        return result;
    }

    public void clearCache() {
        templateExistsCache.clear();
    }

    private TemplateBooleanModel templateExistsImpl(String path) {

        Environment environment = Environment.getCurrentEnvironment();

        // get the current template's parent directory to use when searching for relative paths
        String currentTemplateName = environment.getTemplate().getName();
        String currentTemplateDir = FilenameUtils.getPath(currentTemplateName);

        // look up the path relative to the current working directory (this also works for absolute paths)
        String fullTemplatePath = TemplateCache.getFullTemplatePath(environment, currentTemplateDir, path);

        TemplateLoader templateLoader = environment.getConfiguration().getTemplateLoader();
        boolean exists;
        try {
            exists = templateLoader.findTemplateSource(fullTemplatePath) != null;
        } catch (IOException e) {
            exists = false;
        }

        return exists ? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE;
    }
}
