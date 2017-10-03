package ru.citeck.ecos.patch;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.admin.registry.RegistryKey;
import org.alfresco.repo.admin.registry.RegistryService;
import org.alfresco.repo.module.ModuleComponentHelper;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author Pavel Simonov
 */
public class MoveComponentsPatch extends AbstractPatch {

    private static Log logger = LogFactory.getLog(MoveComponentsPatch.class);

    private static final String TO_KEY = "to";
    private static final String FROM_KEY = "from";

    private static final String REGISTRY_PATH_MODULES = "modules";
    private static final String REGISTRY_PATH_COMPONENTS = "components";

    private RegistryService registryService;
    private List<Properties> transitions;

    private String fromModuleId;
    private String toModuleId;

    @Override
    protected String applyInternal() throws Exception {

        checkParameters();

        for (Properties transitionProps : transitions) {

            RegistryKey fromKey = createKey(fromModuleId, transitionProps.getProperty(FROM_KEY));
            RegistryKey toKey = createKey(toModuleId, transitionProps.getProperty(TO_KEY));

            logger.debug("Move " + fromKey + " to " + toKey);

            registryService.copy(fromKey, toKey);
            //registryService.delete(fromKey);
        }

        return "Success";
    }

    private RegistryKey createKey(String moduleId, String componentId) {
        String[] path = new String[]{ REGISTRY_PATH_MODULES, moduleId, REGISTRY_PATH_COMPONENTS, componentId };
        return new RegistryKey(ModuleComponentHelper.URI_MODULES_1_0, path, null);
    }

    private void checkParameters() {
        ParameterCheck.mandatoryString("fromModuleId", fromModuleId);
        ParameterCheck.mandatoryString("toModuleId", toModuleId);
        ParameterCheck.mandatory("transitions", transitions);
        ParameterCheck.mandatory("registryService", registryService);
        for (Properties transitionProps : transitions) {
            ParameterCheck.mandatoryString(FROM_KEY, transitionProps.getProperty(FROM_KEY));
            ParameterCheck.mandatoryString(TO_KEY, transitionProps.getProperty(TO_KEY));
        }
    }

    public void setTransitions(List<Properties> transitions) {
        this.transitions = transitions;
    }

    public void setTransition(Properties transition) {
        transitions = Collections.singletonList(transition);
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public void setFromModuleId(String fromModuleId) {
        this.fromModuleId = fromModuleId;
    }

    public void setToModuleId(String toModuleId) {
        this.toModuleId = toModuleId;
    }
}
