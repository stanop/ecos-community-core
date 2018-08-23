package ru.citeck.ecos.share.template;

import freemarker.cache.TemplateCache;
import freemarker.cache.TemplateLoader;
import freemarker.core.Environment;
import freemarker.template.TemplateBooleanModel;
import org.alfresco.web.config.packaging.ModulePackage;
import org.alfresco.web.config.packaging.ModulePackageManager;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.extensions.webscripts.processor.BaseProcessorExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Pavel Simonov
 */
public class CiteckUtilsTemplate extends BaseProcessorExtension {

    private final static String CURRENT_MODULE_ID = "ecos-base-core-share";

    private final Map<String, TemplateBooleanModel> templateExistsCache = new ConcurrentHashMap<>();
    private final Map<String, ModulePackage> modulePackagesById = new ConcurrentHashMap<>();

    private ModulePackageManager modulePackageManager;

    private String cacheBust;
    private String aikauVersion;
    private ClassPathResource aikauModuleResource;

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

    public String getCacheBust() {
        if (cacheBust == null) {
            ModulePackage modulePackage = getModulePackage(CURRENT_MODULE_ID);
            if (modulePackage != null) {
                cacheBust = modulePackage.getVersion().toString();
            } else {
                cacheBust = String.valueOf(System.currentTimeMillis());
            }
        }
        return cacheBust;
    }

    public String getAikauVersion() {

        if (aikauVersion == null) {

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(aikauModuleResource.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (StringUtils.isNotBlank(line)) {
                        String[] keyValue = line.split("=");
                        if (keyValue.length == 2 && "version".equals(keyValue[0])) {
                            aikauVersion = keyValue[1];
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (StringUtils.isBlank(aikauVersion)) {
                aikauVersion = "1.0.63";
            }
        }
        return aikauVersion;
    }

    public void clearCache() {
        templateExistsCache.clear();
    }

    public void updateCacheBust() {
        cacheBust = String.valueOf(System.currentTimeMillis());
    }

    public void updateAikauVersion() {
        aikauVersion = null;
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

    public void setAikauModuleResource(ClassPathResource aikauModuleResource) {
        this.aikauModuleResource = aikauModuleResource;
    }
}
