package ru.citeck.ecos.share.template;

import freemarker.cache.TemplateCache;
import freemarker.cache.TemplateLoader;
import freemarker.core.Environment;
import freemarker.template.TemplateBooleanModel;
import org.alfresco.web.config.packaging.ModulePackage;
import org.alfresco.web.config.packaging.ModulePackageManager;
import org.apache.commons.io.FilenameUtils;
import org.springframework.extensions.webscripts.processor.BaseProcessorExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Pavel Simonov
 */
public class CiteckUtilsTemplate extends BaseProcessorExtension {

    private final Map<String, TemplateBooleanModel> templateExistsCache = new ConcurrentHashMap<>();
    private final Map<String, ModulePackage> modulePackagesById = new ConcurrentHashMap<>();

    private ModulePackageManager modulePackageManager;

    public TemplateBooleanModel templateExists(String path) {
        return templateExistsCache.computeIfAbsent(path, this::templateExistsImpl);
    }

    public ModulePackage getModulePackage(String moduleId) {
        return modulePackagesById.computeIfAbsent(moduleId, id -> {
            List<ModulePackage> packages = modulePackageManager.getModulePackages();
            return packages.stream()
                           .filter(module -> id.equals(module.getId()))
                           .findFirst().orElse(null);
        });
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

    public void setModulePackageManager(ModulePackageManager modulePackageManager) {
        this.modulePackageManager = modulePackageManager;
    }
}
