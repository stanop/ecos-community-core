package ru.citeck.ecos.action.group;

import ru.citeck.ecos.repo.RemoteRef;

import java.util.List;

/**
 * @author Pavel Simonov
 */
public interface GroupAction {

    void process(RemoteRef nodeRef);

    List<ActionResult> complete();

    List<ActionResult> cancel();

    boolean isAsync();

    long getTimeout();
}
