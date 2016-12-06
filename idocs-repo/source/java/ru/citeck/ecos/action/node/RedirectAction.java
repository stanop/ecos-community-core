package ru.citeck.ecos.action.node;

/**
 * @author Pavel Simonov
 */
public class RedirectAction extends URLAction {

    private static final String REDIRECT_ACTION = "REDIRECT";
    private static final String PROP_TARGET = "target";

    public enum TargetType {
        BLANK("_blank"), SELF("_self");
        public final String value;
        TargetType(String value) {
            this.value = value;
        }
    }

    public RedirectAction() {
        setContext(URLContext.PAGECONTEXT);
        setProperty(PROP_TARGET, TargetType.SELF.value);
    }

    public void setTarget(TargetType target) {
        setProperty(PROP_TARGET, target.value);
    }

    public TargetType getTarget() {
        String target = getProperty(PROP_TARGET);
        return target != null ? TargetType.valueOf(target) : null;
    }

    @Override
    protected String getActionType() {
        return REDIRECT_ACTION;
    }

}
